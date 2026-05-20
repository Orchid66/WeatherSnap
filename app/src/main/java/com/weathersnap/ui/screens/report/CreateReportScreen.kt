package com.weathersnap.ui.screens.report

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import coil.compose.AsyncImage
import com.weathersnap.ui.screens.weather.WeatherMetric
import kotlin.math.roundToInt

@Composable
fun CreateReportScreen(
    cityName: String,
    temperature: Double,
    condition: String,
    humidity: Int,
    windSpeed: Double,
    pressure: Double,
    navBackStackEntry: NavBackStackEntry? = null,
    onNavigateToCamera: () -> Unit,
    onReportSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cityName) {
        viewModel.initWithWeather(cityName, temperature, condition, humidity, windSpeed, pressure)
    }

    // Listen for image path returned from camera via savedStateHandle
    val capturedImagePath = navBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("captured_image_path", null)
        ?.collectAsStateWithLifecycle()

    LaunchedEffect(capturedImagePath?.value) {
        capturedImagePath?.value?.let { path ->
            viewModel.onImageCaptured(
                path, cityName, temperature, condition, humidity, windSpeed, pressure
            )
            navBackStackEntry?.savedStateHandle?.remove<String>("captured_image_path")
        }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) onReportSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Create Report",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Capture, compress, annotate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Back", fontSize = 12.sp)
            }
        }

        // Weather snapshot
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = cityName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = condition,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${temperature.roundToInt()}°C",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherMetric("Humidity", "${humidity}%")
                    WeatherMetric("Wind", "${windSpeed} km/h")
                    WeatherMetric("Pressure", "${pressure.roundToInt()} hPa")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = state.imagePath != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                AsyncImage(
                    model = state.imagePath,
                    contentDescription = "Captured photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            AnimatedVisibility(
                visible = state.imagePath == null,
                exit = fadeOut()
            ) {
                Text(
                    text = "Photo preview",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Size chips shown after capture
        AnimatedVisibility(visible = state.imagePath != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SizeChip(label = "Original", sizeKb = state.originalSizeKb)
                SizeChip(label = "Compressed", sizeKb = state.compressedSizeKb)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Capture button
        Button(
            onClick = onNavigateToCamera,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Capture Photo", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Field Notes",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text("Notes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(visible = state.error != null) {
            Text(
                text = state.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save button
        Button(
            onClick = {
                viewModel.saveReport(
                    cityName, temperature, condition, humidity, windSpeed, pressure
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            enabled = !state.isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (state.isSaving) "Saving..." else "Save Report",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SizeChip(label: String, sizeKb: Long) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = "${sizeKb} KB",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
