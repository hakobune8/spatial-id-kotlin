package com.hakobune8.spatial_id_kotlin

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin

data class SpatialId(val z: Int, val f: Int, val x: Int, val y: Int) {
  companion object {
    private const val ZFXY_1M_ZOOM_BASE: Int = 25

    fun from(lat: Double, lng: Double, alt: Double? = null, zoom: Int = ZFXY_1M_ZOOM_BASE): SpatialId {
      val meters = alt ?: 0.0
      if (meters <= -(2.0.pow(ZFXY_1M_ZOOM_BASE.toDouble())) || meters >= (2.0.pow(ZFXY_1M_ZOOM_BASE.toDouble()))) {
        throw Error("ZFXY only supports altitude between -2^${ZFXY_1M_ZOOM_BASE} and +2^${ZFXY_1M_ZOOM_BASE}.")
      }
      val f = floor(2.0.pow(zoom.toDouble()) * meters / 2.0.pow(ZFXY_1M_ZOOM_BASE.toDouble()))

      val d2r = Math.PI / 180
      val sin = sin(lat * d2r)
      val z2 = 2.0.pow(zoom.toDouble())
      var x = z2 * (lng / 360 + 0.5)
      val y = z2 * (0.5 - 0.25 * ln((1 + sin) / (1 - sin)) / Math.PI)

      x %= z2
      if (x < 0) {
        x += z2
      }

      return SpatialId(
        zoom,
        f.toInt(),
        x.toInt(),
        y.toInt(),
      )
    }

    private fun getParent(tile: SpatialId, steps: Int = 1): SpatialId {
      val (z, f, x, y) = tile
      if (steps <= 0) {
        throw Exception("steps must be greater than 0")
      }
      if (steps > z) {
        throw Exception("Getting parent tile of $tile, $steps steps is not possible because it would go beyond the root tile (z=0)")
      }
      return SpatialId(z - steps, f shr steps, x shr steps, y shr steps)
    }

    private fun getChildren(tile: SpatialId): List<SpatialId> {
      val (z, f, x, y) = tile
      return listOf(
        SpatialId(z + 1, f * 2, x * 2, y * 2),  // f +0, x +0, y +0
        SpatialId(z + 1, f * 2, x * 2 + 1, y * 2),  // f +0, x +1, y +0
        SpatialId(z + 1, f * 2, x * 2, y * 2 + 1),  // f +0, x +0, y +1
        SpatialId(z + 1, f * 2, x * 2 + 1, y * 2 + 1),  // f +0, x +1, y +1
        SpatialId(z + 1, f * 2 + 1, x * 2, y * 2),  // f +1, x +0, y +0
        SpatialId(z + 1, f * 2 + 1, x * 2 + 1, y * 2),  // f +1, x +1, y +0
        SpatialId(z + 1, f * 2 + 1, x * 2, y * 2 + 1),  // f +1, x +0, y +1
        SpatialId(z + 1, f * 2 + 1, x * 2 + 1, y * 2 + 1)  // f +1, x +1, y +1
      )
    }
  }

  val zfxyStr: String = "$z/$f/$x/$y"

  val tilehash: String
    get() {
      var out = ""
      var z = this.z
      var f = this.f
      var x = this.x
      var y = this.y
      while (z > 0) {
        val thisTile = SpatialId(z, abs(f), x, y)
        val parent = getParent(thisTile)
        val childrenOfParent = getChildren(parent)
        val positionInParent = childrenOfParent.indexOfFirst { child ->
          child.f == abs(f) && child.x == x && child.y == y && child.z == z
        }
        out = (positionInParent + 1).toString() + out
        f = parent.f
        x = parent.x
        y = parent.y
        z = parent.z
      }
      return (if (this.f < 0) "-" else "") + out
    }

  override fun toString(): String = zfxyStr
}