package com.weathersnap.data.repository

import com.weathersnap.data.local.dao.ReportDao
import com.weathersnap.data.local.entity.ReportDraftEntity
import com.weathersnap.data.local.entity.ReportEntity
import com.weathersnap.data.remote.api.GeocodingApi
import com.weathersnap.data.remote.api.WeatherApi
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Weather
import com.weathersnap.domain.model.WeatherReport
import com.weathersnap.utils.WeatherConditionMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi,
    private val reportDao: ReportDao
) {

    // Simple in-memory cache to avoid repeated API calls for the same city query
    private val citySearchCache = mutableMapOf<String, List<City>>()

    suspend fun searchCities(query: String): List<City> {
        // Return cached result if available
        citySearchCache[query]?.let { return it }

        val response = geocodingApi.searchCities(query)
        val cities = response.results?.map { result ->
            City(
                name = result.name,
                country = result.country,
                latitude = result.latitude,
                longitude = result.longitude,
                displayName = if (result.admin1 != null) {
                    "${result.name}, ${result.admin1}, ${result.country}"
                } else {
                    "${result.name}, ${result.country}"
                }
            )
        } ?: emptyList()

        citySearchCache[query] = cities
        return cities
    }

    suspend fun getWeather(city: City): Weather {
        val response = weatherApi.getWeather(city.latitude, city.longitude)
        return Weather(
            cityName = city.displayName,
            temperature = response.current.temperature,
            condition = WeatherConditionMapper.getCondition(response.current.weatherCode),
            humidity = response.current.humidity,
            windSpeed = response.current.windSpeed,
            pressure = response.current.pressure
        )
    }

    fun getAllReports(): Flow<List<WeatherReport>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { entity ->
                WeatherReport(
                    id = entity.id,
                    cityName = entity.cityName,
                    temperature = entity.temperature,
                    condition = entity.condition,
                    humidity = entity.humidity,
                    windSpeed = entity.windSpeed,
                    pressure = entity.pressure,
                    imagePath = entity.imagePath,
                    originalSizeKb = entity.originalSizeKb,
                    compressedSizeKb = entity.compressedSizeKb,
                    notes = entity.notes,
                    savedAt = entity.savedAt
                )
            }
        }
    }

    suspend fun saveReport(report: WeatherReport) {
        reportDao.insertReport(
            ReportEntity(
                cityName = report.cityName,
                temperature = report.temperature,
                condition = report.condition,
                humidity = report.humidity,
                windSpeed = report.windSpeed,
                pressure = report.pressure,
                imagePath = report.imagePath,
                originalSizeKb = report.originalSizeKb,
                compressedSizeKb = report.compressedSizeKb,
                notes = report.notes,
                savedAt = report.savedAt
            )
        )
        // Clean up the draft once saved
        reportDao.clearDraft()
    }

    // Draft methods for the developer judgment challenge
    suspend fun saveDraft(draft: ReportDraftEntity) {
        reportDao.saveDraft(draft)
    }

    suspend fun getDraft(): ReportDraftEntity? {
        return reportDao.getDraft()
    }

    suspend fun clearDraft() {
        reportDao.clearDraft()
    }
}
