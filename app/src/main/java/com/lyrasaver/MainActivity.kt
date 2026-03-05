package com.lyrasaver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.lyrasaver.database.LyraSaverDatabase
import com.lyrasaver.repository.StatusRepository
import com.lyrasaver.services.AutoSaveWorker
import com.lyrasaver.services.DeletedMessageListenerService
import com.lyrasaver.ui.screens.AboutScreen
import com.lyrasaver.ui.screens.DeletedMessagesScreen
import com.lyrasaver.ui.screens.HomeScreen
import com.lyrasaver.ui.screens.MediaCleanerScreen
import com.lyrasaver.ui.screens.SettingsScreen
import com.lyrasaver.viewmodel.MediaCleanerViewModel
import com.lyrasaver.viewmodel.StatusViewModel

class MainActivity : ComponentActivity() {

    private lateinit var database: LyraSaverDatabase
    private lateinit var repository: StatusRepository
    private lateinit var statusViewModel: StatusViewModel
    private lateinit var mediaCleanerViewModel: MediaCleanerViewModel

    // Permissions request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeApp()
        }
    }

    // Document tree launcher for folder selection (SAF)
    private val documentTreeLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Persist the URI permission
            contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            // Save URI to SharedPreferences
            val prefs = getSharedPreferences("lyra_saver_prefs", MODE_PRIVATE)
            prefs.edit().putString("whatsapp_folder_uri", uri.toString()).apply()

            // Load statuses from the selected folder
            statusViewModel.loadStatusesFromFolder(uri, "com.whatsapp")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database
        database = Room.databaseBuilder(
            applicationContext,
            LyraSaverDatabase::class.java,
            "lyra_saver_db"
        ).build()

        // Initialize repository
        repository = StatusRepository(
            context = applicationContext,
            savedStatusDao = database.savedStatusDao(),
            deletedMessageDao = database.deletedMessageDao()
        )

        // Initialize ViewModels
        statusViewModel = StatusViewModel(
            context = applicationContext,
            repository = repository
        )

        mediaCleanerViewModel = MediaCleanerViewModel(
            context = applicationContext,
            repository = repository
        )

        // Request permissions
        requestPermissions()

        // Set content
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        statusViewModel = statusViewModel,
                        mediaCleanerViewModel = mediaCleanerViewModel,
                        onSelectFolder = { launchDocumentTree() }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Media permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Storage permission for Android 11-12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        }

        // Notification listener permission (requires manual setup in Settings)
        // This cannot be requested programmatically

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            initializeApp()
        }
    }

    private fun initializeApp() {
        // Start auto-save worker
        AutoSaveWorker.scheduleAutoSave(this)

        // Request notification listener permission setup (user must go to Settings)
        checkNotificationListenerPermission()
    }

    private fun checkNotificationListenerPermission() {
        val componentName = "${packageName}/${DeletedMessageListenerService::class.java.name}"
        val enabledNotificationListeners = android.provider.Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: ""

        if (!enabledNotificationListeners.contains(componentName)) {
            // Notification listener is not enabled
            // You can optionally show a prompt to user
        }
    }

    private fun launchDocumentTree() {
        documentTreeLauncher.launch(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }
}

@Composable
fun MainNavigation(
    statusViewModel: StatusViewModel,
    mediaCleanerViewModel: MediaCleanerViewModel,
    onSelectFolder: () -> Unit
) {
    val currentScreen = remember { mutableStateOf("home") }

    when (currentScreen.value) {
        "home" -> {
            HomeScreen(
                statusViewModel = statusViewModel,
                onSelectFolder = onSelectFolder
            )
        }
        "media_cleaner" -> {
            MediaCleanerScreen(viewModel = mediaCleanerViewModel)
        }
        "deleted_messages" -> {
            DeletedMessagesScreen(viewModel = statusViewModel)
        }
        "about" -> {
            AboutScreen()
        }
        "settings" -> {
            SettingsScreen()
        }
    }
}
