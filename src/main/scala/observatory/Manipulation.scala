package observatory

import com.github.plokhotnyuk.rtree2d.core.{RTree, RTreeEntry}
import com.github.plokhotnyuk.rtree2d.core.SphericalEarth.entry

/**
  * 4th milestone: value-added information
  */
object Manipulation extends ManipulationInterface {


  class Grid(temperatures: Iterable[(Location, Temperature)]) {
    val height = 180
    val width = 360
    val entries: Iterable[RTreeEntry[(Location, Temperature)]] = temperatures.map(e => entry(e._1.lat.toFloat, e._1.lon.toFloat, e))
    val temperaturesRTree: RTree[(Location, Temperature)] = RTree(entries)

    val values: Array[Temperature] = {
      Range(0, height * width).par.map(
        idx => {
          val x = idx % width
          val y = idx / width
          val lon = x - 180;
          val lat = y - 89;
          val temp = Visualization.predictTemperature(temperaturesRTree, Location(lat,lon));
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


  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    new Grid(temperatures).apply
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val n = temperaturess.size
    val averageTemperatures = temperaturess.par.reduce({
      (data1, data2) => {
        data1.zip(data2).map({
          d => (d._1._1, d._1._2 + d._2._2)
        })
      }
    }).map(x => (x._1, x._2 / n))

    makeGrid(averageTemperatures)
//    loc => {
//      val tempsAtLoc = grids.par.map(_(loc))
//      tempsAtLoc.par.sum / tempsAtLoc.size
//    }
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val temperatureGrid = makeGrid(temperatures)
    return (loc => {
      temperatureGrid(loc) - normals(loc)
    })
  }


}

