package com.example.myapplication.utils

data class RoadReport(
    var id: Int, // The unique identifier for the RoadReport
    val categoryName: String,
    val description: String,
    val email: String,
    val name: String,
    val imgID: String,
    val latitude: Double,
    val longitude: Double,
    val unixTime: Long
)