package com.weathersnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// This table holds the in-progress report so it survives rotation and process death.
// There is only ever one draft at a time (id = 1 always).
// It gets deleted when the user saves or explicitly discards the report.
@Entity(tableName = "report_draft")
data class ReportDraftEntity(
    @PrimaryKey
    val id: Int = 1,
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val imagePath: String?,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)
