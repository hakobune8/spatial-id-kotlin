package com.hakobune8.spatial_id_kotlin

import kotlin.math.*

data class ZFXYTile(val z: Int, val f: Int, val x: Int, val y: Int)

internal const val ZFXY_1M_ZOOM_BASE = 25
internal val ZFXY_ROOT_TILE = ZFXYTile(f = 0, x = 0, y = 0, z = 0)
private const val RAD2DEG = 180 / Math.PI

internal fun isZFXYTile(tile: Any?): Boolean {
  return tile is ZFXYTile
}

internal fun getParent(tile: ZFXYTile, steps: Int = 1): ZFXYTile {
  val (f, x, y, z) = tile
  require(steps > 0) { "steps must be greater than 0" }
  require(steps <= z) {
    "Getting parent tile of $tile, $steps steps is not possible because it would go beyond the root tile (z=0)"
  }
  return ZFXYTile(
    f = f shr steps,
    x = x shr steps,
    y = y shr steps,
    z = z - steps
  )
}

internal fun getChildren(tile: ZFXYTile = ZFXY_ROOT_TILE): List<ZFXYTile> {
  val (f, x, y, z) = tile
  return listOf(
    ZFXYTile(f * 2,     x * 2,     y * 2,     z + 1),
    ZFXYTile(f * 2,     x * 2 + 1, y * 2,     z + 1),
    ZFXYTile(f * 2,     x * 2,     y * 2 + 1, z + 1),
    ZFXYTile(f * 2,     x * 2 + 1, y * 2 + 1, z + 1),
    ZFXYTile(f * 2 + 1, x * 2,     y * 2,     z + 1),
    ZFXYTile(f * 2 + 1, x * 2 + 1, y * 2,     z + 1),
    ZFXYTile(f * 2 + 1, x * 2,     y * 2 + 1, z + 1),
    ZFXYTile(f * 2 + 1, x * 2 + 1, y * 2 + 1, z + 1)
  )
}

internal fun getSurrounding(tile: ZFXYTile = ZFXY_ROOT_TILE): List<ZFXYTile> {
  val (f, x, y, z) = tile
  return listOf(
    zfxyWraparound(ZFXYTile(f, x,     y,     z)),
    zfxyWraparound(ZFXYTile(f, x + 1, y,     z)),
    zfxyWraparound(ZFXYTile(f, x,     y + 1, z)),
    zfxyWraparound(ZFXYTile(f, x + 1, y + 1, z)),
    zfxyWraparound(ZFXYTile(f, x - 1, y,     z)),
    zfxyWraparound(ZFXYTile(f, x,     y - 1, z)),
    zfxyWraparound(ZFXYTile(f, x - 1, y - 1, z)),
    zfxyWraparound(ZFXYTile(f, x + 1, y - 1, z)),
    zfxyWraparound(ZFXYTile(f, x - 1, y + 1, z))
  )
}

internal fun parseZFXYString(str: String): ZFXYTile? {
  val regex = Regex("^/?(\\d+)/(?:((?:\\d+))/)?(\\d+)/(\\d+)$")
  val match = regex.matchEntire(str) ?: return null
  val (z, f, x, y) = match.destructured
  return ZFXYTile(
    z = z.toInt(),
    f = f.ifEmpty { "0" }.toInt(),
    x = x.toInt(),
    y = y.toInt()
  )
}

internal fun getLngLat(tile: ZFXYTile): LngLat {
  val n = Math.PI - 2 * Math.PI * tile.y / (1 shl tile.z)
  return LngLat(
    lng = tile.x.toDouble() / (1 shl tile.z) * 360 - 180,
    lat = RAD2DEG * atan(0.5 * (exp(n) - exp(-n)))
  )
}

internal fun getCenterLngLat(tile: ZFXYTile): LngLat {
  val x = tile.x * 2 + 1
  val y = tile.y * 2 + 1
  val z = tile.z + 1
  return getLngLat(ZFXYTile(x = x, y = y, z = z, f = 0))
}

internal fun getCenterLngLatAlt(tile: ZFXYTile): LngLatWithAltitude {
  val base = getCenterLngLat(tile)
  val alt = getFloor(tile) + (2.0.pow(ZFXY_1M_ZOOM_BASE) / 2.0.pow(tile.z + 1))
  return LngLatWithAltitude(base.lng, base.lat, alt)
}

internal fun getBBox(tile: ZFXYTile): Pair<LngLat, LngLat> {
  val nw = getLngLat(tile)
  val se = getLngLat(tile.copy(x = tile.x + 1, y = tile.y + 1))
  return Pair(nw, se)
}

internal fun getFloor(tile: ZFXYTile): Double {
  return tile.f * (2.0.pow(ZFXY_1M_ZOOM_BASE) / 2.0.pow(tile.z))
}

internal data class CalculateZFXYInput(
  val lat: Double,
  val lng: Double,
  val alt: Double? = null,
  val zoom: Int
)

internal fun calculateZFXY(input: CalculateZFXYInput): ZFXYTile {
  val meters = input.alt ?: 0.0
  if (meters <= -2.0.pow(ZFXY_1M_ZOOM_BASE) || meters >= 2.0.pow(ZFXY_1M_ZOOM_BASE)) {
    throw IllegalArgumentException("ZFXY only supports altitude between -2^$ZFXY_1M_ZOOM_BASE and +2^$ZFXY_1M_ZOOM_BASE.")
  }
  val f = floor((2.0.pow(input.zoom) * meters) / 2.0.pow(ZFXY_1M_ZOOM_BASE)).toInt()

  val d2r = Math.PI / 180
  val sin = sin(input.lat * d2r)
  val z2 = 2.0.pow(input.zoom)
  var x = z2 * (input.lng / 360 + 0.5)
  val y = z2 * (0.5 - 0.25 * ln((1 + sin) / (1 - sin)) / Math.PI)

  x %= z2
  if (x < 0) x += z2

  return ZFXYTile(
    f = f,
    x = floor(x).toInt(),
    y = floor(y).toInt(),
    z = input.zoom
  )
}

internal fun zfxyWraparound(tile: ZFXYTile): ZFXYTile {
  val z = tile.z
  val limit = 2.0.pow(z).toInt()
  return ZFXYTile(
    z = z,
    f = tile.f.coerceIn(-limit, limit),
    x = if (tile.x < 0) tile.x + limit else tile.x % limit,
    y = if (tile.y < 0) tile.y + limit else tile.y % limit
  )
}
