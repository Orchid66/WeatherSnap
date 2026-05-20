package com.weathersnap.ui.screens.savedreports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.repository.WeatherRepository
import com.weathersnap.domain.model.WeatherReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    repository: WeatherRepository
) : ViewModel() {

    val reports: StateFlow<List<WeatherReport>> = repository.getAllReports()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
