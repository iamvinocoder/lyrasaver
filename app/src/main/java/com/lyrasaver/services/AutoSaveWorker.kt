package com.lyrasaver.services

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lyrasaver.database.LyraSaverDatabase
import com.lyrasaver.database.entity.SavedStatus
import com.lyrasaver.repository.StatusRepository
import com.lyrasaver.utils.FileUtils
import com.lyrasaver.utils.StorageUtils
import com.lyrasaver.utils.WhatsAppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AutoSaveWorker(
    context: Context,
    params: androidx.work.WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = Room.databaseBuilder(
        context,
        LyraSaverDatabase::class.java,
        "lyra_saver_db"
    ).build()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Get saved status folder URIs from shared preferences
                val prefs = applicationContext.getSharedPreferences("lyra_saver_prefs", Context.MODE_PRIVATE)
                val folderUri = prefs.getString("whatsapp_folder_uri", "") ?: ""

                if (folderUri.isEmpty()) {
                    return@withContext Result.retry()
                }

                // Load statuses from the folder
                val folder = DocumentFile.fromTreeUri(applicationContext, Uri.parse(folderUri))
                    ?: return@withContext Result.retry()

                val files = StorageUtils.getMediaFiles(folder)
                var savedCount = 0

                files.forEach { file ->
                    try {
                        val mediaType = FileUtils.getMimeType(file.name ?: "")
                        val fileSize = StorageUtils.getFileSize(applicationContext, file.uri)

                        val status = SavedStatus(
                            fileName = file.name ?: "unknown",
                            filePath = file.uri.toString(),
                            mediaType = mediaType,
                            whatsAppSource = "com.whatsapp",
                            fileSize = fileSize,
                            savedAt = System.currentTimeMillis()
                        )

                        // Check if already exists
                        val exists = database.savedStatusDao().getAllStatuses()
                            .collect { list ->
                                list.any { it.fileName == status.fileName }
                            }

                        if (!exists) {
                            database.savedStatusDao().insert(status)
                            savedCount++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                return@withContext Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            } finally {
                database.close()
            }
        }
    }

    companion object {
        private const val AUTO_SAVE_WORK_TAG = "auto_save_status"

        fun scheduleAutoSave(context: Context) {
            val autoSaveRequest = PeriodicWorkRequestBuilder<AutoSaveWorker>(
                5, TimeUnit.MINUTES
            ).addTag(AUTO_SAVE_WORK_TAG).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                AUTO_SAVE_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                autoSaveRequest
            )
        }

        fun cancelAutoSave(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(AUTO_SAVE_WORK_TAG)
        }

        fun isAutoSaveRunning(context: Context): Boolean {
            return WorkManager.getInstance(context)
                .getWorkInfosByTag(AUTO_SAVE_WORK_TAG)
                .get()
                .any { !it.state.isFinished }
        }
    }
}
