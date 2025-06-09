package com.example.zipcklad.di

import android.content.Context
import com.example.zipcklad.util.ExcelImporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideExcelImporter(@ApplicationContext context: Context): ExcelImporter {
        return ExcelImporter(context)
    }
}