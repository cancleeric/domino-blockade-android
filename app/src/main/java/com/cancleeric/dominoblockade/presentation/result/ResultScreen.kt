package com.cancleeric.dominoblockade.presentation.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.ui.theme.LocalWindowSizeClass
import com.cancleeric.dominoblockade.ui.theme.widthIsMediumOrExpanded

private const val TITLE_PADDING_DP = 16
private const val SUBTITLE_PADDING_DP = 32
private const val BUTTON_PADDING_DP = 8
private const val SCREEN_PADDING_DP = 24
private const val ENTER_ANIM_DELAY_MS = 100
private const val ENTER_ANIM_DURATION_MS = 400

@Composable
fun ResultScreen(
    winnerName: String = "",
    isBlocked: Boolean = false,
    newAchievements: List<AchievementType> = emptyList(),
    onPlayAgain: () -> Unit = {},
    onMenu: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val isMediumOrExpanded = LocalWindowSizeClass.current.widthIsMediumOrExpanded

    if (isMediumOrExpanded) {
        ResultContentWide(
            visible = visible,
            isBlocked = isBlocked,
            winnerName = winnerName,
            newAchievements = newAchievements,
            onPlayAgain = onPlayAgain,
            onMenu = onMenu,
            modifier = modifier
        )
    } else {
        ResultContentCompact(
            visible = visible,
            isBlocked = isBlocked,
            winnerName = winnerName,
            newAchievements = newAchievements,
            onPlayAgain = onPlayAgain,
            onMenu = onMenu,
            modifier = modifier
        )
    }
}

@Composable
private fun ResultContentCompact(
    visible: Boolean,
    isBlocked: Boolean,
    winnerName: String,
    newAchievements: List<AchievementType>,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SCREEN_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(animationSpec = tween(ENTER_ANIM_DURATION_MS, ENTER_ANIM_DELAY_MS)) +
                fadeIn(animationSpec = tween(ENTER_ANIM_DURATION_MS, ENTER_ANIM_DELAY_MS))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ResultTitle(isBlocked = isBlocked)
                ResultSubtitle(isBlocked = isBlocked, winnerName = winnerName)
                if (newAchievements.isNotEmpty()) {
                    NewAchievementsBanner(achievements = newAchievements)
                }
            }
        }
        ResultActionButtons(
            onPlayAgain = onPlayAgain,
            onMenu = onMenu,
            modifier = Modifier.padding(top = SUBTITLE_PADDING_DP.dp)
        )
    }
}

@Composable
private fun ResultContentWide(
    visible: Boolean,
    isBlocked: Boolean,
    winnerName: String,
    newAchievements: List<AchievementType>,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(SCREEN_PADDING_DP.dp),
        horizontalArrangement = Arrangement.spacedBy(SUBTITLE_PADDING_DP.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(animationSpec = tween(ENTER_ANIM_DURATION_MS, ENTER_ANIM_DELAY_MS)) +
                fadeIn(animationSpec = tween(ENTER_ANIM_DURATION_MS, ENTER_ANIM_DELAY_MS)),
            modifier = Modifier.weight(1f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ResultTitle(isBlocked = isBlocked)
                ResultSubtitle(isBlocked = isBlocked, winnerName = winnerName)
                if (newAchievements.isNotEmpty()) {
                    NewAchievementsBanner(achievements = newAchievements)
                }
            }
        }
        ResultActionButtons(
            onPlayAgain = onPlayAgain,
            onMenu = onMenu,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ResultActionButtons(
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BUTTON_PADDING_DP.dp)
    ) {
        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Play Again")
        }
        OutlinedButton(
            onClick = onMenu,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Main Menu")
        }
    }
}

@Composable
private fun NewAchievementsBanner(achievements: List<AchievementType>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = TITLE_PADDING_DP.dp)
    ) {
        Text(
            text = "Achievement Unlocked!",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        achievements.forEach { type ->
            Text(
                text = "${type.badge} ${type.title}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ResultTitle(isBlocked: Boolean) {
    val title = if (isBlocked) "Game Blocked!" else "Game Over"
    Text(
        text = title,
        style = MaterialTheme.typography.displaySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = TITLE_PADDING_DP.dp)
    )
}

@Composable
private fun ResultSubtitle(isBlocked: Boolean, winnerName: String) {
    val subtitle = if (isBlocked) {
        "No player can make a move."
    } else {
        "$winnerName wins!"
    }
    Text(
        text = subtitle,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = TITLE_PADDING_DP.dp)
    )
}
