package observatory

import com.github.plokhotnyuk.rtree2d.core.RTree
import com.sksamuel.scrimage.{Image, Pixel}
import com.github.plokhotnyuk.rtree2d.core.SphericalEarth._

import scala.math.Numeric.BigIntIsIntegral.mkNumericOps
/**
  * 2nd milestone: basic visualization
  */
object Visualization{


  def degreeToRadian(theta : Double) = theta * Math.PI / 180.0

  val RADIUS_EARTH_KM = 6371.137
  val INTERPOLATION_CUTOFF = 1.0f
  val P_VAL = 5
  val EPS = 1e-7
  val REQ_NEAREST = 1000;

  val ALPHA = 255
  val WIDTH = 360
  val HEIGHT = 180



  val colorLegend : Array[(Temperature, Color)] = Map[Temperature, Color](
    +60.0 -> Color(255, 255, 255),
    +32.0 -> Color(255, 0, 0),
    +12.0 -> Color(255, 255, 0),
    0.0 -> Color(0, 255, 255),
    -15.0 -> Color(0, 0, 255),
    -27.0 -> Color(255, 0, 255),
    -50.0 -> Color(33, 0, 107),
    -60.0 -> Color(0, 0, 0),
  ).toArray.sortWith(_._1 < _._1)

  //points.toList.sortWith(_._1 < _._1).toArray

  /** @return Distance b/w points in KM */

  def distance(p1 : Location, p2 : Location) = {
    val l1 = Math.sin(degreeToRadian(p1.lat)) * Math.sin(degreeToRadian(p2.lat))
    val l2 = Math.cos(degreeToRadian(p1.lat)) * Math.cos(degreeToRadian(p2.lat))
    val l3 = Math.cos(degreeToRadian(p1.lon) - degreeToRadian(p2.lon))
    val centralAngle = Math.acos(l1 + l2 * l3)
    RADIUS_EARTH_KM *  centralAngle
  }



  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */


  def predictTemperature(temperatures : RTree[(Location, Temperature)], location: Location): Temperature = {
    val nearest = temperatures.nearestK(location.lat.toFloat, location.lon.toFloat, 1,  maxDist = INTERPOLATION_CUTOFF);
    if (nearest.nonEmpty) nearest(0).value._2
    else {
      val nearest = temperatures.nearestK(location.lat.toFloat, location.lon.toFloat, REQ_NEAREST).map(_.value)
      val weights = nearest.par.map(dt => (Math.pow(distance(dt._1, location), -P_VAL),dt._2))
      val num = weights.par.foldLeft(0.0)((accum, curr) => accum + curr._1 * curr._2)
      val denom = weights.par.foldLeft(0.0)((accum, curr) => accum + curr._1)
      num / denom
    }
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(sortedPoints: Array[(Double, Color)], value: Double): Color = {

    for (i <- 0 until sortedPoints.length - 1) {
      (sortedPoints(i), sortedPoints(i + 1)) match {
        case ((v1, Color(r1, g1, b1)), (v2, Color(r2, g2, b2))) => {
          if (v1 > value) {
            return Color(r1, g1, b1)
          }
          else if (v2 > value) {
            val ratio = (value - v1) / (v2 - v1)
            return Color(
              math.round(r1 + (r2 - r1) * ratio).toInt,
              math.round(g1 + (g2 - g1) * ratio).toInt,
              math.round(b1 + (b2 - b1) * ratio).toInt
            )
          }
        }
      }
    }
    sortedPoints(sortedPoints.length - 1)._2
  }


    def colorToPixel(c: Color): Pixel = {
      Pixel.apply(c.red, c.green, c.blue, ALPHA)
    }

    def xyToLocation(x: Int, y: Int): Location = Location(90 - y, x - 180)


    def visualize(temperatures: Iterable[(Location, Double)]): Image = {

      val entries = temperatures.map(e => entry(e._1.lat.toFloat, e._1.lon.toFloat, e))
      val temperaturesRTree = RTree(entries)

//      val buffer = new Array[Pixel](WIDTH * HEIGHT)
//      for (y <- 0 until HEIGHT) {
//        for (x <- 0 until WIDTH) {
//          val predictedTemp = predictTemperature(temperaturesRTree, xyToLocation(x, y))
//          buffer(y * WIDTH + x) = colorToPixel(Visualization.interpolateColor(colors, predictedTemp))
//        }
//      }

      val pixels = Range(0, WIDTH * HEIGHT).par.map(
        {
          idx => {
            val x = idx % WIDTH
            val y = idx / WIDTH
            val loc = xyToLocation(x, y)
            val temp = predictTemperature(temperaturesRTree, loc)
            colorToPixel(interpolateColor(colorLegend, temp))
          }
        }
      ).toArray

      Image(WIDTH, HEIGHT, pixels)
    }

}




