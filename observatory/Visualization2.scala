package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Interaction.tileLocation

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 extends Visualization2Interface {


  val colorLegend : Map[Temperature, Color] = Map[Temperature, Color](
    +7.0 -> Color(0, 0, 0),   /* Black */
    +4.0 -> Color(255, 0, 0),        /* Red */
    +2.0 -> Color(255, 255, 0),      /* Yellow */
     0.0 -> Color(255, 255, 255),    /* White */
    -2.0 -> Color(0, 255, 255),      /* Light blue */
    -7.0 -> Color(0, 0, 255)         /* Deep Blue */
  )



  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    val x = point.x
    val y = point.y
    d00 * (1 - x) * (1 - y) + d10 * x * (1- y) + d01 * (1- x) * y + d11 * x * y
  }





  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile
  ): Image = {
    val tileLen = 256
    val BIT_SHIFT = 8
    val ALPHA = 127
    val tilePixLocs = Range(0, tileLen * tileLen).par.map(
      idx => {
        val x = idx % tileLen
        val y = idx / tileLen
        val px = (tile.x << BIT_SHIFT) + x
        val py = (tile.y << BIT_SHIFT) + y
        val pz = tile.zoom + BIT_SHIFT
        tileLocation(Tile(px, py, pz))
      }
    )
    val locTemps = tilePixLocs.par.map(
      loc => {
        var plat = loc.lat.floor.toInt
        var nlat = loc.lat.ceil.toInt
        var plon = loc.lon.floor.toInt
        var nlon = loc.lon.ceil.toInt
        if (plat < -89 )   plat = 90 + (plat + 90)
        if (nlat > 90  )   nlat = -89 + (nlat - 91)
        if (plon < -180)   plon = 179 + (plon + 181)
        if (nlon > 179 )   nlon = -180 + (nlon - 180)
        val d00 = grid(GridLocation(plat, plon))
        val d10 = grid(GridLocation(nlat, plon))
        val d01 = grid(GridLocation(plat, nlon))
        val d11 = grid(GridLocation(nlat, nlon))
        val x = loc.lon - plon
        val y = loc.lat - plat
        bilinearInterpolation(CellPoint(x, y), d00, d01, d10, d11)
      }
    )
    val pixColors = locTemps.par.map(Visualization.interpolateColor(colorLegend,_))
    val pixels = pixColors.par.map(col => Pixel(col.red, col.green, col.blue,ALPHA)).toArray
    Image(tileLen, tileLen, pixels)

  }

}
