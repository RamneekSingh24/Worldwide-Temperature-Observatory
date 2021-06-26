package observatory

import org.apache.spark
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}
import org.apache.log4j.{Level, Logger}
import java.time.LocalDate
import scala.Console.println
import scala.io.Source

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface {

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  case class StationID(stnID : Int, wbanID : Int)
  val conf = new SparkConf().setMaster("local").setAppName("My app")
  val sc = new SparkContext(conf)

  def toCelcius(temp : Temperature): Temperature = (temp - 32.0)/1.8

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)


  def readStationsFile(year : Year, stationsFile: String): RDD[(StationID, Location)] = {

    val totalpath = getClass.getResourceAsStream(stationsFile)
    val input = Source.fromInputStream(totalpath, "utf-8")
    val rdd = sc.makeRDD(input.getLines().toList)
    val filterdRdd = rdd.filter(s => {
      val args = s.split(",", -1)
      (args(0).trim.nonEmpty || args(1).trim.nonEmpty) && (args(2).trim.nonEmpty && args(3).nonEmpty)
    })
    val dataRdd = filterdRdd.map(
      text => {
        val args = text.split(",",-1)
        val stdID = if(args(0).trim().isEmpty) -1 else args(0).toInt
        val wbanID = if(args(1).trim().isEmpty) -1 else args(1).toInt
        val key = StationID(stdID, wbanID)
        val lat =  args(2).toDouble
        val long = args(3).toDouble
        val value = Location(lat, long)
        (key, value)
      }
    )
    dataRdd
  }

  def readTempFile(year : Year, tempFile: String): RDD[(StationID, (LocalDate,Temperature))] = {

    val totalpath = getClass.getResourceAsStream(tempFile)
    val input = Source.fromInputStream(totalpath, "utf-8")
    val rdd = sc.makeRDD(input.getLines().toList)
    val filterdRdd = rdd.filter(s => {
      val args = s.split(",", -1)
      args(4).toDouble < 9999.0
    })
    val dataRdd = filterdRdd.map(
      text => {
        val args = text.split(",",-1)
        val stdID = if(args(0).trim().isEmpty) -1 else args(0).toInt
        val wbanID = if(args(1).trim().isEmpty) -1 else args(1).toInt
        val key = StationID(stdID, wbanID)
        val valueDate = LocalDate.of(year,args(2).toInt,args(3).toInt)
        val valueTemp = toCelcius(args(4).toDouble)
        (key, (valueDate, valueTemp))
      }
    )
    dataRdd
  }

  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    val stationsRDD = readStationsFile(year, stationsFile)
    val tempRDD = readTempFile(year, temperaturesFile)
    val joined = stationsRDD.join(tempRDD)
    val rtn = joined.map(elem => (elem._2._2._1, elem._2._1,elem._2._2._2))
    //rtn.foreach(r => println(r._1, r._2, r._3))
    rtn.collect()
  }



  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    val dataRdd = sc.parallelize(records.toSeq)
    val groupedRdd = dataRdd.groupBy(_._2)
    val rtn = groupedRdd.mapValues(
      vals => {
        val(sum, len) = vals.foldLeft(0.0, 0)((accum, elem) => (accum._1 + elem._3, accum._2 + 1) )
        sum/len
      }
    )
    rtn.collect()
  }

}