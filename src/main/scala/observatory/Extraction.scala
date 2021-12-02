package observatory

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import java.time.LocalDate

/**
  * 1st milestone: data extraction
  */


object Extraction {

  val RESOURCE_DIR = "/Users/ramneeksingh/Desktop/projects/observatory/src/main/resources/";
  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("Wikipidia programming language rank")
  val sc = new SparkContext(conf);
  val rdd: RDD[String] = sc.textFile(RESOURCE_DIR + "1975.csv");

  case class StationID(stnID : Int, wbanID : Int)
  case class TempData(date: LocalDate, temperature: Temperature)

  def getStationsRDD(path : String): RDD[(StationID, Location)] = {
    val textRDD = sc.textFile(path)
    val filteredTxtRDD = textRDD.filter(line => {
      val data = line.trim().split(",").map(t => t.trim());
      (data.length >= 4) && (data(0).nonEmpty || data(1).nonEmpty) && (data(2).nonEmpty && data(3).nonEmpty)
    });
    val stationsRDD = filteredTxtRDD.map(line => {
      val data = line.trim().split(",").map(t => t.trim())
      val stnID = if (data(0).nonEmpty) data(0).toInt else -1
      val wbanID = if (data(1).nonEmpty) data(1).toInt else -1
      val key = StationID(stnID, wbanID)
      val value = Location(data(2).toDouble, data(3).toDouble)
      (key, value)
    })
    stationsRDD

  }

  def getTemperaturesRDD(year : Int, path : String): RDD[(StationID, TempData)] = {
    val textRDD = sc.textFile(path)
    val filteredTxtRDD = textRDD.filter(line => {
      val data = line.trim().split(",").map(t => t.trim());
      (data.length >= 5) && (data(0).nonEmpty || data(1).nonEmpty) && (data(2).nonEmpty && data(3).nonEmpty && data(4).nonEmpty)
    });
    def toCelcius(temp : Temperature): Temperature = (temp - 32.0)/1.8
    val tempRDD = filteredTxtRDD.map(line => {
      val data = line.trim().split(",").map(t => t.trim())
      val stnID = if (data(0).nonEmpty) data(0).toInt else -1
      val wbanID = if (data(1).nonEmpty) data(1).toInt else -1
      val key = StationID(stnID, wbanID)
      val valueDate = LocalDate.of(year,data(2).toInt,data(3).toInt)
      val valueTemp = toCelcius(data(4).toDouble)
      val value = TempData(valueDate, valueTemp)
      (key, value)
    })
    tempRDD
  }


  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): RDD[(LocalDate, Location, Temperature)] = {
    val tempRDD = getTemperaturesRDD(year, RESOURCE_DIR + temperaturesFile)
    val stationsRDD = getStationsRDD(RESOURCE_DIR + stationsFile)
    stationsRDD.join(tempRDD).map(data => (data._2._2.date, data._2._1, data._2._2.temperature))
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: RDD[(LocalDate, Location, Temperature)]): RDD[(Location, Temperature)] = {

    records.groupBy(_._2).mapValues(vals =>
    {
      val (sum, len) = vals.foldLeft(0.0, 0)((accum, elem) => (accum._1 + elem._3, accum._2 + 1) )
      sum / len
    }
    )
  }

  def getYearlyData(year: Year, stationsFile: String, temperaturesFile: String): RDD[(Location, Temperature)] =
    locationYearlyAverageRecords(locateTemperatures(year, stationsFile, temperaturesFile))


}
