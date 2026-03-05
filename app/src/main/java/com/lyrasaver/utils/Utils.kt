package com.lyrasaver.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.documentfile.provider.DocumentFile

object WhatsAppUtils {
    // WhatsApp package names
    const val WHATSAPP_PACKAGE = "com.whatsapp"
    const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    const val GB_WHATSAPP_PACKAGE = "com.gbwhatsapp"

    // Legacy paths (for reference - using SAF instead)
    const val WHATSAPP_LEGACY_PATH = "WhatsApp/Media/.Statuses"
    const val WHATSAPP_BUSINESS_LEGACY_PATH = "WhatsApp Business/Media/.Statuses"
    const val GB_WHATSAPP_LEGACY_PATH = "GBWhatsApp/Media/.Statuses"

    fun getWhatsAppPaths(): List<String> {
        return listOf(
            "WhatsApp/Media/.Statuses",
            "WhatsApp Business/Media/.Statuses",
            "GBWhatsApp/Media/.Statuses",
            "WhatsApp Aero/Media/.Statuses",
            "Fouad WhatsApp/Media/.Statuses"
        )
    }

    fun isWhatsAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getInstalledWhatsAppVersions(context: Context): List<String> {
        val versions = mutableListOf<String>()
        
        if (isWhatsAppInstalled(context, WHATSAPP_PACKAGE)) {
            versions.add(WHATSAPP_PACKAGE)
        }
        if (isWhatsAppInstalled(context, WHATSAPP_BUSINESS_PACKAGE)) {
            versions.add(WHATSAPP_BUSINESS_PACKAGE)
        }
        if (isWhatsAppInstalled(context, GB_WHATSAPP_PACKAGE)) {
            versions.add(GB_WHATSAPP_PACKAGE)
        }
        
        return versions
    }

    fun getDisplayName(packageName: String): String {
        return when (packageName) {
            WHATSAPP_PACKAGE -> "WhatsApp"
            WHATSAPP_BUSINESS_PACKAGE -> "WhatsApp Business"
            GB_WHATSAPP_PACKAGE -> "GB WhatsApp"
            else -> "Unknown"
        }
    }
}

object StorageUtils {
    fun copyFileToDownloads(context: Context, sourceUri: Uri, fileName: String): Boolean {
        return try {
            val sourceFile = DocumentFile.fromSingleUri(context, sourceUri) ?: return false
            
            val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            }
            
            downloadsDir?.let {
                val sourceInputStream = context.contentResolver.openInputStream(sourceUri)
                sourceInputStream?.use { input ->
                    val outputFile = java.io.File(it, fileName)
                    val outputStream = outputFile.outputStream()
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                    return true
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.MediaStore.MediaColumns.SIZE)
                    if (index >= 0) {
                        it.getLong(index)
                    } else {
                        0
                    }
                } else {
                    0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getMediaFiles(documentFile: DocumentFile): List<DocumentFile> {
        val files = mutableListOf<DocumentFile>()
        try {
            documentFile.listFiles().forEach { file ->
                if (file.isFile && (file.type?.startsWith("image/") == true || file.type?.startsWith("video/") == true)) {
                    files.add(file)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return files
    }
}

object FileUtils {
    fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") -> "image"
            fileName.endsWith(".mp4") || fileName.endsWith(".3gp") || fileName.endsWith(".webm") -> "video"
            else -> "unknown"
        }
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes <= 0 -> "0 B"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
