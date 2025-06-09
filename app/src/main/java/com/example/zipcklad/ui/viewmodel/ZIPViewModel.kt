package com.example.zipcklad.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zipcklad.data.local.ZIPItemEntity
import com.example.zipcklad.domain.repository.ZipRepository
import com.example.zipcklad.ui.components.SyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ZIPViewModel @Inject constructor(
    application: Application,
    private val repository: ZipRepository
) : AndroidViewModel(application) {

    // Состояние синхронизации
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Поисковый запрос
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val DEBOUNCE_DELAY = 300L

    // Состояние авторизации
    private val _isMaster = MutableStateFlow(false)
    val isMaster: StateFlow<Boolean> = _isMaster.asStateFlow()
    private val MASTER_PASSWORD = "master123" // В реальном приложении хранить безопасно!

    // Элементы для отображения
    val items = _searchQuery
        .debounce(DEBOUNCE_DELAY)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllItems()
            } else {
                repository.search(query)
            }
        }

    // Авторизация
    fun login(password: String): Boolean {
        if (password == MASTER_PASSWORD) {
            _isMaster.value = true
            return true
        }
        return false
    }

    fun logout() {
        _isMaster.value = false
    }

    // Работа с элементами
    fun addNewItem(name: String, partNumber: String, quantity: Int, location: String) {
        viewModelScope.launch {
            repository.insert(
                ZIPItemEntity(
                    name = name,
                    partNumber = partNumber,
                    quantity = quantity,
                    location = location,
                    isDirty = true
                )
            )
        }
    }

    fun updateItem(item: ZIPItemEntity) {
        viewModelScope.launch {
            repository.update(item.copy(isDirty = true))
        }
    }

    fun deleteItem(item: ZIPItemEntity) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    // Импорт данных
    fun importExcel(stream: java.io.InputStream) {
        viewModelScope.launch {
            repository.importFromExcel(stream)
        }
    }

    // Обработка поиска
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Синхронизация
    fun syncWithServer() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.LOADING
            try {
                repository.syncWithServer()
                _syncStatus.value = SyncStatus.SUCCESS
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    // Сброс статуса синхронизации
    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.IDLE
    }
}