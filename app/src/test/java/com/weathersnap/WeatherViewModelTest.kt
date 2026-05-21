package com.weathersnap

import com.weathersnap.data.repository.WeatherRepository
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Weather
import com.weathersnap.ui.screens.weather.WeatherViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: WeatherRepository
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = WeatherViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `query shorter than 3 chars does not trigger search`() = runTest {
        viewModel.onQueryChanged("Lo")
        assertFalse(viewModel.uiState.value.isLoadingSuggestions)
        assertTrue(viewModel.uiState.value.suggestions.isEmpty())
    }

    @Test
    fun `query longer than 2 chars updates query state`() = runTest {
        viewModel.onQueryChanged("Lon")
        assertEquals("Lon", viewModel.uiState.value.query)
    }

    @Test
    fun `selecting city clears suggestions and fetches weather`() = runTest {
        val city = City("London", "GB", 51.5, -0.1)
        val weather = Weather("London, GB", 15.0, "Partly cloudy", 70, 20.0, 1013.0)

        coEvery { repository.getWeather(city) } returns weather

        viewModel.onCitySelected(city)

        assertEquals(city.displayName, viewModel.uiState.value.query)
        assertTrue(viewModel.uiState.value.suggestions.isEmpty())
        assertFalse(viewModel.uiState.value.showSuggestions)
    }

    @Test
    fun `weather fetch error sets error state`() = runTest {
        val city = City("London", "GB", 51.5, -0.1)
        coEvery { repository.getWeather(city) } throws Exception("Network error")

        viewModel.onCitySelected(city)

        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoadingWeather)
    }
}
