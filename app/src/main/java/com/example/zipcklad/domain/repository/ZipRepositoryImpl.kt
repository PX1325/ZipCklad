package com.example.zipcklad.data.repository

import com.example.zipcklad.data.local.ZIPItemDao
import com.example.zipcklad.data.local.ZIPItemEntity
import com.example.zipcklad.data.remote.api.ZipApiService
import com.example.zipcklad.data.remote.model.ZipItemResponse
import com.example.zipcklad.data.remote.model.ZipItemSyncRequest
import com.example.zipcklad.domain.repository.ZipRepository
import com.example.zipcklad.util.ExcelImporter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZipRepositoryImpl @Inject constructor(
    private val zipItemDao: ZIPItemDao,
    private val apiService: ZipApiService,
    private val excelImporter: ExcelImporter
) : ZipRepository {

    override fun getAllItems() = zipItemDao.getAll()

    override fun search(query: String) = zipItemDao.search(query)

    override suspend fun insert(item: ZIPItemEntity) = zipItemDao.insert(item)

    override suspend fun update(item: ZIPItemEntity) = zipItemDao.update(item)

    override suspend fun delete(item: ZIPItemEntity) = zipItemDao.delete(item)

    override suspend fun insertAll(items: List<ZIPItemEntity>) = zipItemDao.insertAll(items)

    override suspend fun getUnsyncedItems() = zipItemDao.getUnsyncedItems()

    override suspend fun markAsSynced(ids: List<Int>) = zipItemDao.markAsSynced(ids)

    override suspend fun syncWithServer() {
        // 1. Отправка локальных изменений
        val unsyncedItems = getUnsyncedItems()
        if (unsyncedItems.isNotEmpty()) {
            val response = apiService.syncItems(unsyncedItems.map { it.toSyncRequest() })
            if (response.isSuccessful) {
                markAsSynced(unsyncedItems.map { it.id })
            } else {
                throw Exception("Sync failed: ${response.message()}")
            }
        }

        // 2. Получение свежих данных
        val serverResponse = apiService.getAllItems()
        if (serverResponse.isSuccessful) {
            serverResponse.body()?.let { items ->
                zipItemDao.syncWithServer(items.map { it.toEntity() })
            }
        } else {
            throw Exception("Fetch failed: ${serverResponse.message()}")
        }
    }

    override suspend fun importFromExcel(stream: java.io.InputStream) {
        val items = excelImporter.importFromStream(stream)
        insertAll(items.map { it.copy(isDirty = true) })
    }

    private fun ZIPItemEntity.toSyncRequest() = ZipItemSyncRequest(
        name = name ?: "",
        partNumber = partNumber,
        quantity = quantity,
        location = location,
        isDirty = isDirty
    )

    private fun ZipItemResponse.toEntity() = ZIPItemEntity(
        name = name,
        partNumber = partNumber,
        quantity = quantity,
        location = location,
        isDirty = false
    )
}