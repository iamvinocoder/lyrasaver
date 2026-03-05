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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lyrasaver.ui.composables.LoadingStateIndicator
import com.lyrasaver.ui.composables.ScanningProgressAnimation
import com.lyrasaver.ui.composables.SuccessAnimation
import com.lyrasaver.utils.FileUtils
import com.lyrasaver.viewmodel.MediaCleanerViewModel

@Composable
fun MediaCleanerScreen(viewModel: MediaCleanerViewModel) {
    val scanResults = viewModel.scanResults.collectAsState()
    val isScanning = viewModel.isScanning.collectAsState()
    val isCleaning = viewModel.isCleaning.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Media Cleaner",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Scan results card
        if (scanResults.value.scannerComplete) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Size",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = scanResults.value.formattedSize,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        }

        // Scan button
        if (!isScanning.value && !isCleaning.value) {
            Button(
                onClick = { /* Scan */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan Media")
            }
        } else if (isScanning.value) {
            ScanningProgressAnimation(
                progress = 45f, // Placeholder - integrate with actual progress state
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete options
        Button(
            onClick = { /* Delete sent */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCleaning.value
        ) {
            Text("Delete Sent Media")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Delete cache */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCleaning.value
        ) {
            Text("Delete Cache")
        }

        if (isCleaning.value) {
            Spacer(modifier = Modifier.height(16.dp))
            LoadingStateIndicator(
                isLoading = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Success state
        if (scanResults.value.sentMediaDeleted) {
            Spacer(modifier = Modifier.height(16.dp))
            SuccessAnimation(message = "Sent media deleted successfully")
        }
    }
}
