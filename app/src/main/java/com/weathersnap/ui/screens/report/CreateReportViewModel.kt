package com.weathersnap.ui.screens.report

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.local.entity.ReportDraftEntity
import com.weathersnap.data.repository.WeatherRepository
import com.weathersnap.domain.model.WeatherReport
import com.weathersnap.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class CreateReportUiState(
    val imagePath: String? = null,
    val originalSizeKb: Long = 0L,
    val compressedSizeKb: Long = 0L,
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    // On init, check if there's a draft to restore from a previous interrupted session
    fun initWithWeather(cityName: String, temperature: Double, condition: String,
                        humidity: Int, windSpeed: Double, pressure: Double) {
        viewModelScope.launch {
            val draft = repository.getDraft()
            // Restore draft only if it matches the current weather context
            if (draft != null && draft.cityName == cityName) {
                _uiState.value = _uiState.value.copy(
                    imagePath = draft.imagePath,
                    notes = draft.notes
                )
                // Also restore image sizes if image exists
                if (draft.imagePath != null && File(draft.imagePath).exists()) {
                    val originalSize = savedStateHandle.get<Long>("original_size") ?: 0L
                    val compressedSize = savedStateHandle.get<Long>("compressed_size") ?: 0L
                    _uiState.value = _uiState.value.copy(
                        originalSizeKb = originalSize,
                        compressedSizeKb = compressedSize
                    )
                }
            } else {
                // Different city, clear old draft
                repository.clearDraft()
            }

            // Save initial draft so rotation/process death is covered from the start
            saveDraft(cityName, temperature, condition, humidity, windSpeed, pressure)
        }
    }

    fun onImageCaptured(rawImagePath: String, cityName: String, temperature: Double,
                        condition: String, humidity: Int, windSpeed: Double, pressure: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = ImageUtils.compressImage(context, rawImagePath)

                // Delete the raw uncompressed file to avoid temp file leak
                File(rawImagePath).delete()

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        imagePath = result.compressedFilePath,
                        originalSizeKb = result.originalSizeKb,
                        compressedSizeKb = result.compressedSizeKb
                    )
                    savedStateHandle["original_size"] = result.originalSizeKb
                    savedStateHandle["compressed_size"] = result.compressedSizeKb

                    // Update draft with new image path
                    saveDraft(cityName, temperature, condition, humidity, windSpeed, pressure)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(error = "Image compression failed")
                }
            }
        }
    }

    fun onNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveReport(cityName: String, temperature: Double, condition: String,
                   humidity: Int, windSpeed: Double, pressure: Double) {
        val imagePath = _uiState.value.imagePath
        if (imagePath == null) {
            _uiState.value = _uiState.value.copy(error = "Please capture a photo first")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                repository.saveReport(
                    WeatherReport(
                        cityName = cityName,
                        temperature = temperature,
                        condition = condition,
                        humidity = humidity,
                        windSpeed = windSpeed,
                        pressure = pressure,
                        imagePath = imagePath,
                        originalSizeKb = _uiState.value.originalSizeKb,
                        compressedSizeKb = _uiState.value.compressedSizeKb,
                        notes = _uiState.value.notes,
                        savedAt = System.currentTimeMillis()
                    )
                )
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                // Draft is cleared in repository.saveReport()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save report. Please try again."
                )
            }
        }
    }

    private fun saveDraft(cityName: String, temperature: Double, condition: String,
                          humidity: Int, windSpeed: Double, pressure: Double) {
        viewModelScope.launch {
            repository.saveDraft(
                ReportDraftEntity(
                    cityName = cityName,
                    temperature = temperature,
                    condition = condition,
                    humidity = humidity,
                    windSpeed = windSpeed,
                    pressure = pressure,
                    imagePath = _uiState.value.imagePath,
                    notes = _uiState.value.notes
                )
            )
        }
    }
}
