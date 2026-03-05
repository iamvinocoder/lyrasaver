package com.lyrasaver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val autoSaveEnabled = remember { mutableStateOf(false) }
    val notificationsEnabled = remember { mutableStateOf(true) }
    val deleteOldMessages = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Auto-Save Toggle
        SettingItemWithToggle(
            title = "Auto-Save Statuses",
            description = "Automatically save new statuses",
            checked = autoSaveEnabled.value,
            onCheckedChange = { autoSaveEnabled.value = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Toggle
        SettingItemWithToggle(
            title = "Notifications",
            description = "Receive notifications for new statuses",
            checked = notificationsEnabled.value,
            onCheckedChange = { notificationsEnabled.value = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Auto-Delete Old Messages
        SettingItemWithToggle(
            title = "Auto-Delete Messages",
            description = "Delete messages older than 30 days",
            checked = deleteOldMessages.value,
            onCheckedChange = { deleteOldMessages.value = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Clear all data */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear All Data")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Export data */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export Data")
        }
    }
}

@Composable
fun SettingItemWithToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
