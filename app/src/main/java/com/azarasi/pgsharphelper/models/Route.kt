package com.azarasi.pgsharphelper.models

data class Route(
    val name: String,
    val points: List<LatLng>
) {
    data class LatLng(
        val latitude: Double,
        val longitude: Double
    )
}