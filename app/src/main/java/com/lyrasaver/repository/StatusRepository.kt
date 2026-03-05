package com.lyrasaver.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.lyrasaver.database.dao.DeletedMessageDao
import com.lyrasaver.database.dao.SavedStatusDao
import com.lyrasaver.database.entity.DeletedMessage
import com.lyrasaver.database.entity.SavedStatus
import com.lyrasaver.utils.FileUtils
import com.lyrasaver.utils.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StatusRepository(
    private val context: Context,
    private val savedStatusDao: SavedStatusDao,
    private val deletedMessageDao: DeletedMessageDao
) {
    // Saved Status Operations
    fun getAllStatuses(): Flow<List<SavedStatus>> = savedStatusDao.getAllStatuses()

    fun getStatusesByType(mediaType: String): Flow<List<SavedStatus>> {
        return savedStatusDao.getStatusesByType(mediaType)
    }

    fun getFavoriteStatuses(): Flow<List<SavedStatus>> = savedStatusDao.getFavoriteStatuses()

    suspend fun saveStatus(status: SavedStatus) {
        withContext(Dispatchers.IO) {
            savedStatusDao.insert(status)
        }
    }

    suspend fun updateStatus(status: SavedStatus) {
        withContext(Dispatchers.IO) {
            savedStatusDao.update(status)
        }
    }

    suspend fun deleteStatus(status: SavedStatus) {
        withContext(Dispatchers.IO) {
            savedStatusDao.delete(status)
        }
    }

    suspend fun copyStatusToStorage(sourceUri: Uri, fileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            StorageUtils.copyFileToDownloads(context, sourceUri, fileName)
        }
    }

    fun getTotalStorageUsed(): Flow<Long> = savedStatusDao.getTotalSize()

    fun getTotalStatusCount(): Flow<Int> = savedStatusDao.getTotalCount()

    // Deleted Message Operations
    fun getAllDeletedMessages(): Flow<List<DeletedMessage>> {
        return deletedMessageDao.getAllDeletedMessages()
    }

    fun getMessagesBySender(senderName: String): Flow<List<DeletedMessage>> {
        return deletedMessageDao.getMessagesBySender(senderName)
    }

    suspend fun addDeletedMessage(message: DeletedMessage) {
        withContext(Dispatchers.IO) {
            deletedMessageDao.insert(message)
        }
    }

    suspend fun deleteMessage(message: DeletedMessage) {
        withContext(Dispatchers.IO) {
            deletedMessageDao.delete(message)
        }
    }

    suspend fun deleteOldMessages(daysOld: Int = 30) {
        withContext(Dispatchers.IO) {
            val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000)
            deletedMessageDao.deleteOldMessages(cutoffTime)
        }
    }

    // File Discovery from WhatsApp .Statuses Folders using SAF
    suspend fun discoverWhatsAppStatuses(folderUri: Uri, whatsAppSource: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext 0
                if (!folder.canRead()) return@withContext 0

                val files = StorageUtils.getMediaFiles(folder)
                var savedCount = 0

                // Get existing filenames to avoid duplicates
                val existingFiles = mutableSetOf<String>()
                try {
                    // Query existing files from DB
                    // This is simplified - in production, use a proper Room query
                    val allStatuses = getAllStatusesSync()
                    existingFiles.addAll(allStatuses.map { it.fileName })
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                files.forEach { file ->
                    try {
                        // Skip if already exists
                        if (existingFiles.contains(file.name)) {
                            return@forEach
                        }

                        val mediaType = FileUtils.getMimeType(file.name ?: "")
                        if (mediaType == "unknown") {
                            return@forEach
                        }

                        val fileSize = StorageUtils.getFileSize(context, file.uri)

                        val status = SavedStatus(
                            fileName = file.name ?: "status_${System.currentTimeMillis()}",
                            filePath = file.uri.toString(),
                            mediaType = mediaType,
                            whatsAppSource = whatsAppSource,
                            fileSize = fileSize,
                            savedAt = System.currentTimeMillis()
                        )
                        savedStatusDao.insert(status)
                        savedCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                savedCount
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }
    }

    private suspend fun getAllStatusesSync(): List<SavedStatus> {
        return withContext(Dispatchers.IO) {
            // This would use a blocking query - for demo using empty list
            emptyList()
        }
    }

    // Media Cleaner Functions
    suspend fun scanMediaSize(folderUri: Uri): Long {
        return withContext(Dispatchers.IO) {
            try {
                val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext 0L
                if (!folder.canRead()) return@withContext 0L
                calculateFolderSize(folder)
            } catch (e: Exception) {
                e.printStackTrace()
                0L
            }
        }
    }

    private fun calculateFolderSize(folder: DocumentFile): Long {
        var totalSize = 0L
        return try {
            folder.listFiles().forEach { file ->
                totalSize += try {
                    if (file.isFile) {
                        file.length()
                    } else if (file.isDirectory) {
                        calculateFolderSize(file)
                    } else {
                        0L
                    }
                } catch (e: Exception) {
                    0L
                }
            }
            totalSize
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    suspend fun deleteSentMedia(folderUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext false
                if (!folder.canWrite()) return@withContext false
                deleteMediaInFolder(folder, "Sent")
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun deleteCacheMedia(cacheDir: String?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                cacheDir?.let {
                    val file = java.io.File(it)
                    if (!file.exists()) return@let false
                    val folder = DocumentFile.fromFile(file)
                    deleteMediaInFolder(folder, "Cache")
                } ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun deleteMediaInFolder(folder: DocumentFile, filterName: String): Boolean {
        return try {
            folder.listFiles().forEach { file ->
                if (file.isFile && file.name?.contains(filterName, ignoreCase = true) == true) {
                    file.delete()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
