package com.weathersnap.domain.model

data class City(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val displayName: String = "$name, $country"
)

data class Weather(
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double
)

data class WeatherReport(
    val id: Long = 0,
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val imagePath: String,
    val originalSizeKb: Long,
    val compressedSizeKb: Long,
    val notes: String,
    val savedAt: Long
)
