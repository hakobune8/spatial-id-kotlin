package com.hakobune8.spatial_id_kotlin

internal fun parseZFXYTilehash(thInput: String): ZFXYTile {
  var th = thInput
  var negativeF = false
  if (th.startsWith("-")) {
    negativeF = true
    th = th.substring(1)
  }

  var children = getChildren()
  var lastChild: ZFXYTile? = null

  for (c in th) {
    val index = c.digitToInt() - 1
    lastChild = children[index].copy()
    children = getChildren(lastChild)
  }

  if (lastChild == null) {
    throw IllegalArgumentException("Invalid tilehash: $thInput")
  }

  return if (negativeF) lastChild.copy(f = -lastChild.f) else lastChild
}

internal fun generateTilehash(tile: ZFXYTile): String {
  var (f, x, y, z) = tile
  val originalF = f
  var out = ""

  while (z > 0) {
    val thisTile = ZFXYTile(f = kotlin.math.abs(f), x = x, y = y, z = z)
    val parent = getParent(thisTile)
    val childrenOfParent = getChildren(parent)

    val positionInParent = childrenOfParent.indexOfFirst {
      it.f == kotlin.math.abs(f) && it.x == x && it.y == y && it.z == z
    }

    if (positionInParent == -1) {
      throw IllegalStateException("Tile not found among parent's children")
    }

    out = (positionInParent + 1).toString() + out
    f = parent.f
    x = parent.x
    y = parent.y
    z = parent.z
  }

  return (if (originalF < 0) "-" else "") + out
}
