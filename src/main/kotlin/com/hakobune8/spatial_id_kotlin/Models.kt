package com.hakobune8.spatial_id_kotlin

data class LngLatWithAltitude(
  val lng: Double,
  val lat: Double,
  val alt: Double?
)

data class LngLat(
  val lng: Double,
  val lat: Double
)
