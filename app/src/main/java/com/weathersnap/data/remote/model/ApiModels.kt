package com.weathersnap.data.remote.model

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("results") val results: List<CityResult>?
)

data class CityResult(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("admin1") val admin1: String?
)

data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeather,
    @SerializedName("current_units") val units: CurrentUnits?
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("surface_pressure") val pressure: Double,
    @SerializedName("weather_code") val weatherCode: Int
)

data class CurrentUnits(
    @SerializedName("temperature_2m") val temperatureUnit: String?
)
