package com.example.zipcklad.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [ZIPItemEntity::class], version = 2,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun zipItemDao(): ZIPItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zip_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
