package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

  def degreeToRadian(theta : Double) = theta * Math.PI / 180.0

  val radiusInKm = 6371.137
  val cutOffInKm = 1.0
  val pVal = 3
  val eps = 1e-7

  /** @return Distance b/w points in KM */

  def distance(p1 : Location, p2 : Location) = {
    val l1 = Math.sin(degreeToRadian(p1.lat)) * Math.sin(degreeToRadian(p2.lat))
    val l2 = Math.cos(degreeToRadian(p1.lat)) * Math.cos(degreeToRadian(p2.lat))
    val l3 = Math.cos(degreeToRadian(p1.lon) - degreeToRadian(p2.lon))
    val centralAngle = Math.acos(l1 + l2 * l3)
    radiusInKm *  centralAngle
  }

  def interpolateTemp(temperatures : Iterable[(Location, Temperature)], location: Location) : Temperature =  {
    val weights = temperatures.par.map(dt => (Math.pow(distance(dt._1, location), -pVal),dt._2))
    val num = weights.par.foldLeft(0.0)((accum, curr) => accum + curr._1 * curr._2)
    val denom = weights.par.foldLeft(0.0)((accum, curr) => accum + curr._1)
    num / denom
  }

  def ~= (d1 : Double, d2 : Double) = {
    Math.abs(d1 - d2) < eps
  }


  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    val (dis, temp) = temperatures.par.foldLeft((Double.MaxValue, Double.NaN))( (sofar, curr) => {
      if(distance(location, curr._1) < sofar._1) (distance(location, curr._1),curr._2)
      else sofar
    })
    if(dis < cutOffInKm) temp
    else interpolateTemp(temperatures, location)
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    val sortedPoints = points.toArray.sortWith(_._1 < _._1)
    for (i <- 0 until sortedPoints.length - 1) {
        val p1 = sortedPoints(i)
        val p2 = sortedPoints(i+1)
          if (value < p1._1 || ~=(value, p1._1)) return p1._2
          else if (p2._1 > value && value >= p1._1) {
            val disRatio = (value-p1._1)/(p2._1 - p1._1)
            return Color(
              math.round(p1._2.red + (p2._2.red - p1._2.red)*disRatio).toInt,
              math.round(p1._2.green + (p2._2.green - p1._2.green)*disRatio).toInt,
              math.round(p1._2.blue + (p2._2.blue - p1._2.blue)*disRatio).toInt
            )
      }
    }
    return sortedPoints.last._2
  }




  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    val width = 360
    val height = 180
    //val pixels = new Array[Pixel](width * height)

    val pixels = Range(0, width * height).par.map(
      {
        idx => {
          val x = idx % width
          val y = idx / width
          val loc = Location(90-y, x-180)
          val temp = predictTemperature(temperatures, loc)
          val col = interpolateColor(colors, temp)
          Pixel(col.red, col.green, col.blue, 255)
        }
      }
    ).toArray



  Image(width,height, pixels)

  }

}

