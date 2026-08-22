package com.lenshrv.app.di

import com.lenshrv.app.domain.repository.HrvMetricsRepository
import com.lenshrv.app.domain.repository.HrvMetricsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHrvMetricsRepository(
        hrvMetricsRepositoryImpl: HrvMetricsRepositoryImpl
    ): HrvMetricsRepository
}
