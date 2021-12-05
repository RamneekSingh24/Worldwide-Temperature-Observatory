package observatory

import com.github.plokhotnyuk.rtree2d.core.RTree
import com.github.plokhotnyuk.rtree2d.core.SphericalEarth.entry
import com.sksamuel.scrimage.{Image, Pixel, writer}
import observatory.Visualization.{interpolateColor, predictTemperature}
import java.nio.file.{Paths, Files}
import scala.reflect.io.File

/**
  * 3rd milestone: interactive visualization
  * */


object Interaction {

  val OUTPUT_DIR = "target/temperatures/<year>/<zoom>/<x>-<y>.png";

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
  def tile(temperatures: RTree[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
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
          val col = interpolateColor(Visualization.colorLegend, temp)
          Pixel(col.red, col.green, col.blue, ALPHA)
        }
      }
    ).toArray
    Image(gridLen, gridLen, pixels)
  }

  def generateTiles(temperatures: Iterable[(Location, Temperature)], year : Int): Unit = {
    val entries = temperatures.map(e => entry(e._1.lat.toFloat, e._1.lon.toFloat, e))
    val temperaturesRTree = RTree(entries)

    Range(0, 4).foreach(zoom => {
        val dirName = f"target/temperatures/${year}/${zoom}";
        Files.createDirectories(Paths.get(dirName))
        Range(0, (1 << zoom)).foreach( x => {
            for(y <- 0 until(1 << zoom)) {
              println(f"Generating tile for ${x}, ${y}, ${zoom}, ${year}")
              val tileXYZ = Tile(x,y,zoom)
              val img = tile(temperaturesRTree, Visualization.colorLegend, tileXYZ)
              img.output(f"target/temperatures/${year}/${zoom}/${x}-${y}.png")
              println(f"Generated tile for ${x}, ${y}, ${zoom}, ${year}")
            }
          }
        )
      }
    )


    //    Range(0, 1).par.foreach(zoom => {
    //      val dirName = f"target/temperatures/${year}/${zoom}";
    //      Files.createDirectories(Paths.get(dirName))
    //      Range(0, 1).par.foreach( x => {
    //          for(y <- 0 until(1)) {
    //            println(f"Generating tile for ${x}, ${y}, ${zoom}, ${year}")
    //            val tileXYZ = Tile(x,y,zoom)
    //            val img = tile(temperaturesRTree, Visualization.colorLegend, tileXYZ)
    //            img.output(f"target/temperatures/${year}/${zoom}/${x}-${y}.png")
    //            println(f"Generated tile for ${x}, ${y}, ${zoom}, ${year}")
    //          }
    //        }
    //      )
    //    }

  }


}
