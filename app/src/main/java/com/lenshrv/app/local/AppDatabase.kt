package com.lenshrv.app.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lenshrv.app.local.dao.ChannelValuesDao
import com.lenshrv.app.local.dao.HrvMetricsDao
import com.lenshrv.app.local.entities.ChannelValuesEntity
import com.lenshrv.app.local.entities.HrvMetricsEntity

@Database(
    entities = [
        HrvMetricsEntity::class,
        ChannelValuesEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hrvMetricsDao(): HrvMetricsDao
    abstract fun channelValuesDao(): ChannelValuesDao
}
