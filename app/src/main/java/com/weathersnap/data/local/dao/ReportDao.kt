package com.weathersnap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weathersnap.data.local.entity.ReportEntity
import com.weathersnap.data.local.entity.ReportDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    // --- Saved reports ---

    @Insert
    suspend fun insertReport(report: ReportEntity): Long

    @Query("SELECT * FROM reports ORDER BY savedAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    // --- Draft ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: ReportDraftEntity)

    @Query("SELECT * FROM report_draft WHERE id = 1")
    suspend fun getDraft(): ReportDraftEntity?

    @Query("DELETE FROM report_draft")
    suspend fun clearDraft()
}
