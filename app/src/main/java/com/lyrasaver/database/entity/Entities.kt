package com.lyrasaver.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "saved_status")
data class SavedStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,
    val filePath: String,
    val mediaType: String, // "image" or "video"
    val whatsAppSource: String, // "com.whatsapp", "com.whatsapp.w4b", "com.gbwhatsapp"
    val savedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val fileSize: Long = 0
)

@Entity(tableName = "deleted_message")
data class DeletedMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senderName: String,
    val senderNumber: String = "",
    val messageContent: String,
    val deletedAt: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val chatType: String = "individual" // "individual", "group"
)
