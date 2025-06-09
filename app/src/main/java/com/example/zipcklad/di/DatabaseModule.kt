package com.example.zipcklad.di

import android.content.Context
import com.example.zipcklad.data.local.AppDatabase
import com.example.zipcklad.data.local.ZIPItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideZIPItemDao(database: AppDatabase): ZIPItemDao {
        return database.zipItemDao()
    }
}