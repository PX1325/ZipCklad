package com.example.zipcklad

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.zipcklad.network.NetworkMappers
import com.example.zipcklad.network.ZipApiService
import com.example.zipcklad.network.ZipItemResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

// Добавим статусы синхронизации
 sealed class SyncStatus {
    object IDLE : SyncStatus()
    object LOADING : SyncStatus()
    object SUCCESS : SyncStatus()
    data class ERROR(val message: String) : SyncStatus()
}

@HiltViewModel
class ZIPViewModel @Inject constructor(
    application: Application,
    private val apiService: ZipApiService,
    private val zipItemDao: ZIPItemDao
) : AndroidViewModel(application) {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: MutableStateFlow<SyncStatus> = _syncStatus

    val searchQuery = MutableStateFlow("")
    private val DEBOUNCE_DELAY = 300L

    val items: Flow<List<ZIPItemEntity>> = searchQuery
        .debounce(DEBOUNCE_DELAY)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                zipItemDao.getAll()
            } else {
                zipItemDao.search(query)
            }
        }

    // Дополнительные методы для DAO
    private fun List<ZIPItemEntity>.toSyncRequest() = map {
        NetworkMappers.toSyncRequest(it)
    }

    fun addItem(item: ZIPItemEntity) {
        viewModelScope.launch {
            zipItemDao.insert(item.copy(isDirty = true))
        }
    }

    fun updateItem(item: ZIPItemEntity) {
        viewModelScope.launch {
            zipItemDao.update(item.copy(isDirty = true))
        }
    }

    fun importExcel(inputStream: InputStream) = viewModelScope.launch {
        val items = ExcelImporter(getApplication()).importFromStream(inputStream)
        zipItemDao.insertAll(items.map { it.copy(isDirty = true) })
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.IDLE
    }

    fun syncWithServer() = viewModelScope.launch {
        _syncStatus.value = SyncStatus.LOADING
        try {
            // 1. Отправляем локальные изменения
            val unsyncedItems = zipItemDao.getUnsyncedItems()
            if (unsyncedItems.isNotEmpty()) {
                val response = apiService.syncItems(unsyncedItems.toSyncRequest())
                if (response.isSuccessful) {
                    zipItemDao.markAsSynced(unsyncedItems.map { it.id })
                } else {
                    throw Exception("Failed to sync changes: ${response.message()}")
                }
            }

            // 2. Получаем свежие данные с сервера
            val serverResponse = apiService.getAllItems()
            if (serverResponse.isSuccessful) {
                serverResponse.body()?.let { items ->
                    val validItems = items
                        .filterNotNull()
                        .mapNotNull { it.toEntity() }

                    zipItemDao.syncWithServer(validItems)
                    _syncStatus.value = SyncStatus.SUCCESS
                } ?: throw Exception("Server returned empty response")
            } else {
                throw Exception("Failed to fetch data: ${serverResponse.message()}")
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR(e.message ?: "Unknown error")
            Log.e("SYNC_ERROR", "Sync failed", e)
        }
    }

    private fun ZipItemResponse.toEntity() = ZIPItemEntity(
        name = name ?: "Без названия",
        partNumber = partNumber ?: "Без номера",
        quantity = quantity ?: 0,
        location = location ?: "Не указано"
    )
}
