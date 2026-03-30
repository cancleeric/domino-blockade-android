package com.cancleeric.dominoblockade.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.R
import com.cancleeric.dominoblockade.data.local.GameRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameDetailViewModel = hiltViewModel()
) {
    val record by viewModel.record.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (record == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.detail_loading))
            }
        } else {
            GameDetailContent(
                record = record!!,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun GameDetailContent(
    record: GameRecord,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val resultColor = if (record.isWin) Color(0xFF4CAF50) else Color(0xFFF44336)
    val resultText = if (record.isWin) "WIN" else "LOSS"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = resultColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = stringResource(R.string.detail_opponent), value = record.aiName)
                DetailRow(label = stringResource(R.string.detail_difficulty), value = record.difficulty)
                DetailRow(
                    label = stringResource(R.string.detail_score),
                    value = "${record.playerScore} - ${record.opponentScore}"
                )
                DetailRow(
                    label = stringResource(R.string.detail_duration),
                    value = formatDuration(record.durationSeconds)
                )
                DetailRow(
                    label = stringResource(R.string.detail_date),
                    value = dateFormat.format(Date(record.timestamp))
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(minutes, secs)
}
