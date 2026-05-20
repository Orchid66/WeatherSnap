package com.weathersnap.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.repository.WeatherRepository
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val query: String = "",
    val suggestions: List<City> = emptyList(),
    val showSuggestions: Boolean = false,
    val selectedCity: City? = null,
    val weather: Weather? = null,
    val isLoadingSuggestions: Boolean = false,
    val isLoadingWeather: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            error = null
        )

        // Only search after more than 2 letters
        if (query.length > 2) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(300) // small debounce
                fetchCitySuggestions(query)
            }
        } else {
            _uiState.value = _uiState.value.copy(
                suggestions = emptyList(),
                showSuggestions = false
            )
        }
    }

    private suspend fun fetchCitySuggestions(query: String) {
        _uiState.value = _uiState.value.copy(isLoadingSuggestions = true)
        try {
            val cities = repository.searchCities(query)
            _uiState.value = _uiState.value.copy(
                suggestions = cities,
                showSuggestions = cities.isNotEmpty(),
                isLoadingSuggestions = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoadingSuggestions = false,
                error = "Could not fetch city suggestions. Check your connection."
            )
        }
    }

    fun onCitySelected(city: City) {
        _uiState.value = _uiState.value.copy(
            query = city.displayName,
            selectedCity = city,
            suggestions = emptyList(),
            showSuggestions = false
        )
        fetchWeather(city)
    }

    private fun fetchWeather(city: City) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingWeather = true,
                weather = null,
                error = null
            )
            try {
                val weather = repository.getWeather(city)
                _uiState.value = _uiState.value.copy(
                    weather = weather,
                    isLoadingWeather = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingWeather = false,
                    error = "Could not fetch weather. Please try again."
                )
            }
        }
    }

    fun dismissSuggestions() {
        _uiState.value = _uiState.value.copy(showSuggestions = false)
    }
}
