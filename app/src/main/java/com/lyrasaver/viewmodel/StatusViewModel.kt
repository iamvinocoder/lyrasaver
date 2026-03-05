package com.lyrasaver.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyrasaver.database.LyraSaverDatabase
import com.lyrasaver.database.entity.DeletedMessage
import com.lyrasaver.database.entity.SavedStatus
import com.lyrasaver.repository.StatusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatusViewModel(
    private val context: Context,
    private val repository: StatusRepository
) : ViewModel() {

    // UI States
    private val _allStatuses = MutableStateFlow<List<SavedStatus>>(emptyList())
    val allStatuses: StateFlow<List<SavedStatus>> = _allStatuses.asStateFlow()

    private val _photoStatuses = MutableStateFlow<List<SavedStatus>>(emptyList())
    val photoStatuses: StateFlow<List<SavedStatus>> = _photoStatuses.asStateFlow()

    private val _videoStatuses = MutableStateFlow<List<SavedStatus>>(emptyList())
    val videoStatuses: StateFlow<List<SavedStatus>> = _videoStatuses.asStateFlow()

    private val _favoriteStatuses = MutableStateFlow<List<SavedStatus>>(emptyList())
    val favoriteStatuses: StateFlow<List<SavedStatus>> = _favoriteStatuses.asStateFlow()

    private val _deletedMessages = MutableStateFlow<List<DeletedMessage>>(emptyList())
    val deletedMessages: StateFlow<List<DeletedMessage>> = _deletedMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _totalStorageUsed = MutableStateFlow(0L)
    val totalStorageUsed: StateFlow<Long> = _totalStorageUsed.asStateFlow()

    private val _totalStatusCount = MutableStateFlow(0)
    val totalStatusCount: StateFlow<Int> = _totalStatusCount.asStateFlow()

    private val _autoSaveEnabled = MutableStateFlow(false)
    val autoSaveEnabled: StateFlow<Boolean> = _autoSaveEnabled.asStateFlow()

    init {
        observeAllStatuses()
        observeStatusesByType()
        observeFaves()
        deleteDeletedMessages()
        observeStorageAndCount()
    }

    private fun observeAllStatuses() {
        viewModelScope.launch {
            repository.getAllStatuses().collect {
                _allStatuses.value = it
            }
        }
    }

    private fun observeStatusesByType() {
        viewModelScope.launch {
            repository.getStatusesByType("image").collect {
                _photoStatuses.value = it
            }
        }

        viewModelScope.launch {
            repository.getStatusesByType("video").collect {
                _videoStatuses.value = it
            }
        }
    }

    private fun observeFaves() {
        viewModelScope.launch {
            repository.getFavoriteStatuses().collect {
                _favoriteStatuses.value = it
            }
        }
    }

    private fun deleteDeletedMessages() {
        viewModelScope.launch {
            repository.getAllDeletedMessages().collect {
                _deletedMessages.value = it
            }
        }
    }

    private fun observeStorageAndCount() {
        viewModelScope.launch {
            repository.getTotalStorageUsed().collect {
                _totalStorageUsed.value = it ?: 0
            }
        }

        viewModelScope.launch {
            repository.getTotalStatusCount().collect {
                _totalStatusCount.value = it ?: 0
            }
        }
    }

    fun saveStatus(folderUri: Uri, fileName: String, whatsAppSource: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val status = SavedStatus(
                    fileName = fileName,
                    filePath = folderUri.toString(),
                    mediaType = getMediaType(fileName),
                    whatsAppSource = whatsAppSource,
                    savedAt = System.currentTimeMillis()
                )
                repository.saveStatus(status)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(status: SavedStatus) {
        viewModelScope.launch {
            val updated = status.copy(isFavorite = !status.isFavorite)
            repository.updateStatus(updated)
        }
    }

    fun deleteStatus(status: SavedStatus) {
        viewModelScope.launch {
            repository.deleteStatus(status)
        }
    }

    fun addDeletedMessage(message: DeletedMessage) {
        viewModelScope.launch {
            repository.addDeletedMessage(message)
        }
    }

    fun deleteMessage(message: DeletedMessage) {
        viewModelScope.launch {
            repository.deleteMessage(message)
        }
    }

    fun setAutoSaveEnabled(enabled: Boolean) {
        _autoSaveEnabled.value = enabled
    }

    fun loadStatusesFromFolder(folderUri: Uri, whatsAppSource: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.loadStatusesFromFolder(folderUri, whatsAppSource)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getMediaType(fileName: String): String {
        return when {
            fileName.lowercase().endsWith(".jpg") ||
            fileName.lowercase().endsWith(".jpeg") ||
            fileName.lowercase().endsWith(".png") -> "image"
            fileName.lowercase().endsWith(".mp4") ||
            fileName.lowercase().endsWith(".3gp") ||
            fileName.lowercase().endsWith(".webm") -> "video"
            else -> "unknown"
        }
    }
}
