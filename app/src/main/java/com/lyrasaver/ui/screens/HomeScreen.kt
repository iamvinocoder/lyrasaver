package com.lyrasaver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lyrasaver.viewmodel.StatusViewModel

@Composable
fun HomeScreen(
    statusViewModel: StatusViewModel,
    onSelectFolder: () -> Unit
) {
    val tabIndex = remember { mutableStateOf(0) }
    val photoStatuses = statusViewModel.photoStatuses.collectAsState()
    val videoStatuses = statusViewModel.videoStatuses.collectAsState()
    val isLoading = statusViewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(selectedTabIndex = tabIndex.value) {
            Tab(
                selected = tabIndex.value == 0,
                onClick = { tabIndex.value = 0 },
                text = {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Photos", modifier = Modifier.padding(end = 4.dp))
                        Text("Photos")
                    }
                }
            )
            Tab(
                selected = tabIndex.value == 1,
                onClick = { tabIndex.value = 1 },
                text = {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Videos", modifier = Modifier.padding(end = 4.dp))
                        Text("Videos")
                    }
                }
            )
            Tab(
                selected = tabIndex.value == 2,
                onClick = { tabIndex.value = 2 },
                text = {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Tools", modifier = Modifier.padding(end = 4.dp))
                        Text("Tools")
                    }
                }
            )
        }

        // Tab Content
        when (tabIndex.value) {
            0 -> {
                if (photoStatuses.value.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Text(
                            text = "No photos found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onSelectFolder) {
                            Text("Select WhatsApp Folder")
                        }
                    }
                } else {
                    StatusGalleryGrid(
                        statuses = photoStatuses.value,
                        isLoading = isLoading.value,
                        onStatusClick = { /* Preview */ },
                        onFavoriteClick = { statusViewModel.toggleFavorite(it) },
                        onDeleteClick = { statusViewModel.deleteStatus(it) }
                    )
                }
            }
            1 -> {
                if (videoStatuses.value.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Text(
                            text = "No videos found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onSelectFolder) {
                            Text("Select WhatsApp Folder")
                        }
                    }
                } else {
                    StatusGalleryGrid(
                        statuses = videoStatuses.value,
                        isLoading = isLoading.value,
                        onStatusClick = { /* Preview */ },
                        onFavoriteClick = { statusViewModel.toggleFavorite(it) },
                        onDeleteClick = { statusViewModel.deleteStatus(it) }
                    )
                }
            }
            2 -> {
                ToolsScreen()
            }
        }
    }
}

@Composable
fun ToolsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tools",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = { /* Navigate to Media Cleaner */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 8.dp)
        ) {
            Text("Media Cleaner")
        }

        Button(
            onClick = { /* Navigate to Deleted Messages */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Deleted Messages")
        }
    }
}
