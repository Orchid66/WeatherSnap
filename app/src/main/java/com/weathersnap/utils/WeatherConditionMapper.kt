package com.weathersnap.utils

// Maps WMO weather codes from Open-Meteo to human-readable conditions
object WeatherConditionMapper {

    fun getCondition(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }
}
