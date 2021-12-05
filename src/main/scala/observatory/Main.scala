

package observatory
import org.apache.spark
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.log4j.{Level, Logger}

import java.time.LocalDate
import scala.Console.println
import scala.io.Source
import observatory.Extraction
import observatory.Extraction.getYearlyData
import com.github.plokhotnyuk.rtree2d.core._
import SphericalEarth._
import com.sksamuel.scrimage.writer
import observatory.Visualization

import java.nio.file.{Files, Paths}


object Main extends App {


    val startTimeMillis = System.currentTimeMillis()

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN);



    // Done(completed) till 1980
    // Start from 1981

    val startYear = 1981; // Done until 1980

    for (year <- 1975 to 2015) {
        try {
            println(f"Starting ${year} ........")
            val dataRDD = getYearlyData(year: Year, "stations.csv": String, f"${year}.csv": String): RDD[(Location, Temperature)]
            val data = dataRDD.collect()
            Interaction.generateTiles(data, year)
            println(f"Done ${year} ........")
        }
        catch {
            case x: Object => {
                Console.err.println(f"Failed for ${year}")
            }
        }
    }

        val endTimeMillis = System.currentTimeMillis()
        val durationSeconds = (endTimeMillis - startTimeMillis) / 1000

        println(durationSeconds)








//    println("Starting! ")
//    val year = 2015
//    val dataRDD = getYearlyData(year: Year, "stations.csv": String, f"${year}.csv": String): RDD[(Location, Temperature)]
////    dataRDD.collect().foreach(println)
//    val data = dataRDD.collect()
//    Interaction.generateTiles(data, 2015)
//
////    val img = Visualization.visualize(data)
////    img.output("img.png")
//

//










    //  val box1 = entry(1.0f, 1.0f, 2.0f, 2.0f, "Box 1")
//  val box2 = entry(2.0f, 2.0f, 3.0f, 3.0f, "Box 2")
//  val entries = Seq(box1, box2)
//
//  val rtree = RTree(entries)
//
//  assert(rtree.entries == entries)
//  assert(rtree.nearestOption(0.0f, 0.0f) == Some(box1))
//  assert(rtree.nearestOption(0.0f, 0.0f, maxDist = 1.0f) == None)
//  assert(rtree.nearestK(0.0f, 0.0f, k = 1) == Seq(box1))
//  assert(rtree.nearestK(0.0f, 0.0f, k = 2, maxDist = 10f) == Seq(box2, box1))
//  assert(rtree.searchAll(0.0f, 0.0f) == Nil)
//  assert(rtree.searchAll(1.5f, 1.5f) == Seq(box1))
//  assert(rtree.searchAll(2.5f, 2.5f) == Seq(box2))
//  assert(rtree.searchAll(2.0f, 2.0f) == Seq(box1, box2))
//  assert(rtree.searchAll(2.5f, 2.5f, 3.5f, 3.5f) == Seq(box2))
//  assert(rtree.searchAll(1.5f, 1.5f, 2.5f, 2.5f).forall(entries.contains))


//
//  val city1 = entry(50.0614f, 19.9383f, "Kraków")
//  val city2 = entry(50.4500f, 30.5233f, "Kyiv")
  //val entries = Seq(city1, city2)
//
//  val rtree = RTree(entries, nodeCapacity = 4/* the best capacity for nearest queries for spherical geometry */)
//
//  assert(rtree.entries == entries)
//  assert(rtree.nearestOption(0.0f, 0.0f).contains(city1))
//  assert(rtree.nearestOption(50f, 20f, maxDist = 1.0f).isEmpty)
//  assert(rtree.nearestK(50f, 20f, k = 1) == Seq(city1))
//  assert(rtree.nearestK(50f, 20f, k = 3, maxDist = 1000f) == Seq(city2, city1))
//  assert(rtree.searchAll(50f, 30f, 51f, 31f) == Seq(city2))
//  assert(rtree.searchAll(0f, -180f, 90f, 180f).forall(entries.contains))
//
//  def distance(p1 : Location, p2 : Location) = {
//    val l1 = Math.sin(degreeToRadian(p1.lat)) * Math.sin(degreeToRadian(p2.lat))
//    val l2 = Math.cos(degreeToRadian(p1.lat)) * Math.cos(degreeToRadian(p2.lat))
//    val l3 = Math.cos(degreeToRadian(p1.lon) - degreeToRadian(p2.lon))
//    val centralAngle = Math.acos(l1 + l2 * l3)
//    RADIUS_EARTH_KM *  centralAngle
//
//  }
//
//  val l1 = Location(23f, 50f)
//  val l2 = Location(40f, 69f);
//  val entry1 = entry(l1.lat.toFloat, l1.lon.toFloat, "olabb")
//  val entry2 = entry(l1.lat.toFloat, l1.lon.toFloat, "bbstudy")
//  val entries = Seq(entry1, entry2)
//  val rtree = RTree(entries)
//
//  val d1 = SphericalEarth.distanceCalculator.distance(l2.lat.toFloat, l2.lon.toFloat, rtree)
//  val d2 = distance(l1, l2)
//
//  println(d1, d2)
  //(2599.8354,2599.887880367632)


}






