package com.hakobune8.spatial_id_kotlin

class Space {
  lateinit var center: LngLatWithAltitude
  var alt: Double = 0.0
  var zoom: Int = 0

  var zfxy: ZFXYTile

  lateinit var id: String
  lateinit var zfxyStr: String
  lateinit var tilehash: String

  companion object {
    private const val DEFAULT_ZOOM = 25

    fun from(id: String, zoom: Int? = null): Space {
      return Space(id, zoom)
    }

    fun from(loc: LngLatWithAltitude, zoom: Int? = null): Space {
      return Space(loc, zoom)
    }

    fun from(zfxyStr: String): Space {
      return Space(zfxyStr)
    }
  }

  constructor(input: Any, zoom: Int? = null) {
    this.zfxy = when (input) {
      is String -> try {
        parseZFXYString(input) ?: parseZFXYTilehash(input)
      } catch (_: Exception) {
        throw IllegalArgumentException("parse ZFXY failed with input: $input")
      }
      is ZFXYTile -> {
        input
      }
      is LngLatWithAltitude -> {
        calculateZFXY(CalculateZFXYInput(
          lat = input.lat,
          lng = input.lng,
          alt = input.alt,
          zoom = zoom ?: DEFAULT_ZOOM
        ))
      }
      else -> throw IllegalArgumentException("Unsupported input type: ${input::class.simpleName}")
    }
    regenerateAttributesFromZFXY()
  }

  fun up(by: Int = 1): Space = move(f = by)
  fun down(by: Int = 1): Space = move(f = -by)
  fun north(by: Int = 1): Space = move(y = by)
  fun south(by: Int = 1): Space = move(y = -by)
  fun east(by: Int = 1): Space = move(x = by)
  fun west(by: Int = 1): Space = move(x = -by)

  fun move(f: Int = 0, x: Int = 0, y: Int = 0): Space {
    val newTile = zfxyWraparound(ZFXYTile(
      z = zfxy.z,
      f = zfxy.f + f,
      x = zfxy.x + x,
      y = zfxy.y + y
    ))
    return Space(newTile)
  }

  fun parent(atZoom: Int? = null): Space {
    val steps = atZoom?.let { zfxy.z - it } ?: 1
    return Space(getParent(zfxy, steps))
  }

  fun children(): List<Space> {
    return getChildren(zfxy).map { Space(it) }
  }

  fun surroundings(): List<Space> {
    val baseStr = "/${zfxy.z}/${zfxy.f}/${zfxy.x}/${zfxy.y}"
    val surroundingTiles = getSurrounding(zfxy).filter {
      "/${it.z}/${it.f}/${it.x}/${it.y}" != baseStr
    }.map { Space(it) }

    val upper = getSurrounding(this.up().zfxy).map { Space(it) }
    val lower = getSurrounding(this.down().zfxy).map { Space(it) }

    return surroundingTiles + upper + lower
  }

  fun vertices3d(): List<Triple<Double, Double, Double>> {
    val (nw, se) = getBBox(zfxy)
    val floor = getFloor(zfxy)
    val ceil = getFloor(zfxy.copy(f = zfxy.f + 1))

    return listOf(
      Triple(nw.lng, nw.lat, floor),
      Triple(nw.lng, se.lat, floor),
      Triple(se.lng, se.lat, floor),
      Triple(se.lng, nw.lat, floor),
      Triple(nw.lng, nw.lat, ceil),
      Triple(nw.lng, se.lat, ceil),
      Triple(se.lng, se.lat, ceil),
      Triple(se.lng, nw.lat, ceil),
    )
  }

  private fun regenerateAttributesFromZFXY() {
    alt = getFloor(zfxy)
    center = getCenterLngLatAlt(zfxy)
    zoom = zfxy.z
    tilehash = generateTilehash(zfxy)
    id = tilehash
    zfxyStr = "/${zfxy.z}/${zfxy.f}/${zfxy.x}/${zfxy.y}"
  }
}
