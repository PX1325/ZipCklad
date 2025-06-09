package com.example.zipcklad.di

import com.example.zipcklad.data.repository.ZipRepositoryImpl
import com.example.zipcklad.domain.repository.ZipRepository
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
    abstract fun bindZipRepository(
        repositoryImpl: ZipRepositoryImpl
    ): ZipRepository
}