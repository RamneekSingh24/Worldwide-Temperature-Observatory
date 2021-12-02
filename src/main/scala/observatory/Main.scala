

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
import EuclideanPlane._

object Main extends App {

//  Logger.getLogger("org.apache.spark").setLevel(Level.WARN);
//  println("Starting! ")
//  //  val RESOURCE_DIR = "/Users/ramneeksingh/Desktop/projects/observatory/src/main/resources/";
//  //  val conf = new SparkConf().setMaster("local[*]").setAppName("Wikipidia programming language rank")
//  //  val sc = new SparkContext(conf);
//  //  val rdd = sc.textFile(RESOURCE_DIR + "1975.csv");
//
//  val year = 1975
//  val dataRDD = getYearlyData(1975: Year, "stations.csv": String, f"${year}.csv": String): RDD[(Location, Temperature)]
//  dataRDD.collect().foreach(println)
//
//  println("Done! ")

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


}






