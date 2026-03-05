package com.lyrasaver.services

import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.room.Room
import com.lyrasaver.database.LyraSaverDatabase
import com.lyrasaver.database.entity.DeletedMessage
import com.lyrasaver.utils.WhatsAppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DeletedMessageListenerService : NotificationListenerService() {

    private lateinit var database: LyraSaverDatabase
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        try {
            database = Room.databaseBuilder(
                applicationContext,
                LyraSaverDatabase::class.java,
                "lyra_saver_db"
            ).fallbackToDestructiveMigration().build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        val notification = sbn.notification ?: return

        // Check if notification is from WhatsApp variants
        if (isWhatsAppPackage(packageName)) {
            try {
                // Extract message content from notification extras
                val contentText = when {
                    notification.extras.containsKey("android.text") -> {
                        notification.extras.getCharSequence("android.text")?.toString() ?: ""
                    }
                    notification.extras.containsKey("android.big_text") -> {
                        notification.extras.getCharSequence("android.big_text")?.toString() ?: ""
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                        notification.extras.getString("android.text") ?: ""
                    }
                    else -> ""
                }

                if (contentText.isNotEmpty()) {
                    val senderName = extractSenderName(contentText, packageName)
                    
                    // Verify this looks like a message (not a status notification)
                    if (shouldLogMessage(contentText)) {
                        val deletedMessage = DeletedMessage(
                            senderName = senderName,
                            messageContent = contentText,
                            deletedAt = System.currentTimeMillis(),
                            chatType = extractChatType(contentText)
                        )

                        scope.launch {
                            try {
                                database.deletedMessageDao().insert(deletedMessage)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        if (isWhatsAppPackage(packageName)) {
            // Optionally log removed notifications as deleted
            // This provides additional insight into what was deleted
        }
    }

    private fun isWhatsAppPackage(packageName: String): Boolean {
        return packageName in listOf(
            WhatsAppUtils.WHATSAPP_PACKAGE,
            WhatsAppUtils.WHATSAPP_BUSINESS_PACKAGE,
            WhatsAppUtils.GB_WHATSAPP_PACKAGE
        )
    }

    private fun extractSenderName(text: String, packageName: String): String {
        return try {
            // Try to extract name from common patterns
            when {
                text.contains(": ") -> text.substringBefore(": ")
                text.contains(" - ") -> text.substringBefore(" - ")
                text.length > 50 -> text.substring(0, 30)
                else -> text
            }
        } catch (e: Exception) {
            "Unknown Contact"
        }
    }

    private fun shouldLogMessage(text: String): Boolean {
        // Filter out system messages and status updates
        val disclaimers = listOf(
            "notification",
            "status",
            "settings changed",
            "disappearing",
            "encrypted",
            "message deleted",
            "read receipt",
            "typing",
            "recording",
            "online"
        )
        
        val lowerText = text.lowercase()
        return !disclaimers.any { lowerText.contains(it) } && text.length > 3
    }

    private fun extractChatType(text: String): String {
        // Detect group messages by keywords
        return if (text.contains("Group") || 
                   text.contains("group") || 
                   text.contains("@") ||
                   text.contains("You:") ||
                   text.contains(" in ")) {
            "group"
        } else {
            "individual"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::database.isInitialized) {
                database.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        scope.launch {
            // Clean old messages (older than 30 days)
            try {
                val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
                database.deletedMessageDao().deleteOldMessages(cutoffTime)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        /**
         * Checks if notification listener is enabled for this app
         */
        fun isNotificationListenerEnabled(context: Context): Boolean {
            val componentName = "${context.packageName}/${DeletedMessageListenerService::class.java.name}"
            val enabledNotificationListeners = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: ""

            return enabledNotificationListeners.contains(componentName)
        }

        /**
         * Opens notification access settings for user
         */
        fun openNotificationSettings(context: Context) {
            context.startActivity(
                android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            )
        }
    }
}
