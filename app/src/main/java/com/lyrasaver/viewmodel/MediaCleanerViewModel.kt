package com.lyrasaver.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyrasaver.repository.StatusRepository
import com.lyrasaver.utils.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaCleanerViewModel(
    private val context: Context,
    private val repository: StatusRepository
) : ViewModel() {

    private val _scanResults = MutableStateFlow(MediaScanResult())
    val scanResults: StateFlow<MediaScanResult> = _scanResults.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isCleaning = MutableStateFlow(false)
    val isCleaning: StateFlow<Boolean> = _isCleaning.asStateFlow()

    fun scanMedia(folderUri: Uri) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val totalSize = repository.scanMediaSize(folderUri)
                _scanResults.value = MediaScanResult(
                    totalSize = totalSize,
                    formattedSize = FileUtils.formatSize(totalSize),
                    scannerComplete = true
                )
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun deleteSentMedia(folderUri: Uri) {
        viewModelScope.launch {
            _isCleaning.value = true
            try {
                val success = repository.deleteSentMedia(folderUri)
                _scanResults.value = _scanResults.value.copy(sentMediaDeleted = success)
            } finally {
                _isCleaning.value = false
            }
        }
    }

    fun deleteCache(cacheDir: String?) {
        viewModelScope.launch {
            _isCleaning.value = true
            try {
                val success = repository.deleteCacheMedia(cacheDir)
                _scanResults.value = _scanResults.value.copy(cacheDeleted = success)
            } finally {
                _isCleaning.value = false
            }
        }
    }

    fun resetScan() {
        _scanResults.value = MediaScanResult()
    }
}

data class MediaScanResult(
    val totalSize: Long = 0,
    val formattedSize: String = "0 B",
    val scannerComplete: Boolean = false,
    val sentMediaDeleted: Boolean = false,
    val cacheDeleted: Boolean = false
)
