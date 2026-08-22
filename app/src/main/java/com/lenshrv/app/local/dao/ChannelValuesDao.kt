package com.lenshrv.app.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lenshrv.app.local.entities.ChannelValuesEntity

@Dao
interface ChannelValuesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(samples: List<ChannelValuesEntity>)

    @Query("SELECT * FROM channel_values ORDER BY timestamp ASC")
    suspend fun getAllChannelValues(): List<ChannelValuesEntity>
    @Query("SELECT * FROM channel_values WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getWaveformForSession(sessionId: String): List<ChannelValuesEntity>
}
