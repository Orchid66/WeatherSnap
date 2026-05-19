package com.weathersnap.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.weathersnap.data.local.dao.ReportDao
import com.weathersnap.data.local.entity.ReportDraftEntity
import com.weathersnap.data.local.entity.ReportEntity

@Database(
    entities = [ReportEntity::class, ReportDraftEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}
