package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Visualization.{interpolateColor, predictTemperature}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction extends InteractionInterface {

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {
    val lat = Math.atan(Math.sinh(Math.PI - (tile.y * 2.0 * Math.PI/(1 << tile.zoom)))) * 180.0/(Math.PI)
    val lon = (tile.x.toDouble * 360.0 / (1 << tile.zoom) - 180.0)
    Location(lat, lon)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    val gridLen = 256
    val BIT_SHIFT = 8
    val ALPHA = 127
    val pixels = Range(0,gridLen*gridLen).par.map(
      {
        idx => {
          val x = idx % gridLen
          val y = idx / gridLen
          val px = (tile.x << BIT_SHIFT) + x
          val py = (tile.y << BIT_SHIFT) + y
          val pz = tile.zoom + BIT_SHIFT
          val loc = tileLocation(Tile(px, py, pz))
          val temp = predictTemperature(temperatures, loc)
          val col = interpolateColor(colors, temp)
          Pixel(col.red, col.green, col.blue, ALPHA)
        }
      }
    ).toArray
    
    Image(gridLen, gridLen, pixels)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
    yearlyData: Iterable[(Year, Data)],
    generateImage: (Year, Tile, Data) => Unit
  ): Unit = {
    for (zoom <- 0 until 4) {
      for(x <- 0 until (1 << zoom)) {
        for(y <- 0 until(1 << zoom)) {
          yearlyData.foreach(elem => generateImage(elem._1, Tile(x,y,zoom), elem._2))
        }
      }
    }
  }

}

object generator {

}