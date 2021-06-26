package observatory

import scala.collection.generic.MutableMapFactory
import scala.collection.mutable
import scala.collection.mutable.Map

/**
  * 4th milestone: value-added information
  */
object Manipulation extends ManipulationInterface {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */





    class Grid(temperatures: Iterable[(Location, Temperature)]) {
      val height = 180
      val width = 360
      val values: Array[Temperature] = {
        Range(0, height * width).par.map(
          idx => {
            val x = idx % width
            val y = idx / width
            val lon = x - 180;
            val lat = y - 89;
            val temp = Visualization.predictTemperature(temperatures, Location(lat,lon));
            temp
          }
        )
      }.toArray
    /* x = lon, y = Lat */
      def apply(loc : GridLocation): Temperature = {
        val x = loc.lon + 180
        val y = loc.lat + 89
        values(y * width + x)
      }

    }



  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    new Grid(temperatures).apply
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val grids = temperaturess.par.map(
      temperatures => {
        makeGrid(temperatures)
      }
    )
    loc => {
      val tempsAtLoc = grids.par.map(_(loc))
      tempsAtLoc.par.sum / tempsAtLoc.size
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val grid = makeGrid(temperatures)
    loc => {
      grid(loc) - normals(loc)
    }
  }


}

