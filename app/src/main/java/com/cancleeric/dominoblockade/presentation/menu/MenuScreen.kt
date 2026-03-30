package com.cancleeric.dominoblockade.presentation.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.R

@Composable
fun MenuScreen(
    onStartGame: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Domino Blockade",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        Button(
            onClick = onStartGame,
            modifier = Modifier.widthIn(min = 200.dp).fillMaxWidth(0.6f)
        ) {
            Text(text = "Start Game")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onOpenHistory,
            modifier = Modifier.widthIn(min = 200.dp).fillMaxWidth(0.6f)
        ) {
            Text(text = stringResource(R.string.menu_history))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onOpenStats,
            modifier = Modifier.widthIn(min = 200.dp).fillMaxWidth(0.6f)
        ) {
            Text(text = stringResource(R.string.menu_stats))
        }
    }
}
