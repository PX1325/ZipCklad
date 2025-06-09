package com.example.zipcklad.di

import com.example.zipcklad.data.remote.api.ZipApiService
import com.example.zipcklad.network.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(): ZipApiService {
        return RetrofitClient.instance
    }
}