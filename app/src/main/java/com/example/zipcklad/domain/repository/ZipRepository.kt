package com.example.zipcklad.domain.repository

import com.example.zipcklad.data.local.ZIPItemEntity
import kotlinx.coroutines.flow.Flow

interface ZipRepository {
    // Потоковые операции
    fun getAllItems(): Flow<List<ZIPItemEntity>>
    fun search(query: String): Flow<List<ZIPItemEntity>>

    // Операции с БД
    suspend fun insert(item: ZIPItemEntity)
    suspend fun update(item: ZIPItemEntity)
    suspend fun delete(item: ZIPItemEntity)
    suspend fun insertAll(items: List<ZIPItemEntity>)

    // Синхронизация
    suspend fun syncWithServer()
    suspend fun getUnsyncedItems(): List<ZIPItemEntity>
    suspend fun markAsSynced(ids: List<Int>)

    // Импорт
    suspend fun importFromExcel(stream: java.io.InputStream)
}