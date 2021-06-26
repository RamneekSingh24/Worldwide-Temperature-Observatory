package observatory
import org.junit.Assert._
import org.junit.{Assert, Test}
import Extraction._
import com.sksamuel.scrimage.{Image, Pixel}

trait VisualizationTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("raw data display", 2) _

  // Implement tests for the methods of the `Visualization` object

  @Test def `mytest`: Unit = {

    Assert.assertTrue(
      1 == 1
    )
    //println(new java.io.File("/Users/ramneeksingh/Desktop/fcp scala/capstone/observatory 2/src/main/resources/stations.csv").exists)
    locationYearlyAverageRecords(locateTemperatures(1975 ,"/stations.csv", "/1975.csv"))
    println("DONE")
  }

}
