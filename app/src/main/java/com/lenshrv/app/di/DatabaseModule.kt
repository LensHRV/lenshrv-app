package com.lenshrv.app.di

import android.content.Context
import androidx.room.Room
import com.lenshrv.app.local.AppDatabase
import com.lenshrv.app.local.dao.ChannelValuesDao
import com.lenshrv.app.local.dao.HrvMetricsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.lenshrv.app.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val dbName = "lens_hrv_${BuildConfig.FLAVOR}_db"
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            dbName,
        )
            .build()
    }

    @Provides
    @Singleton
    fun provideHrvMetricsDao(appDatabase: AppDatabase): HrvMetricsDao {
        return appDatabase.hrvMetricsDao()
    }

    @Provides
    @Singleton
    fun provideChannelValuesDao(appDatabase: AppDatabase): ChannelValuesDao {
        return appDatabase.channelValuesDao()
    }
}
