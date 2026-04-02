package com.cancleeric.dominoblockade.presentation.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.R

private const val OVERLAY_ALPHA = 0.7f
private const val CARD_CORNER_DP = 16
private const val CARD_CONTENT_PADDING_DP = 24
private const val HORIZONTAL_PADDING_DP = 24
private const val SECTION_SPACING_DP = 16
private const val BUTTON_SPACING_DP = 8
private const val PROGRESS_BOTTOM_PADDING_DP = 8

private data class TutorialStep(val titleRes: Int, val descRes: Int)

private val tutorialSteps = listOf(
    TutorialStep(R.string.tutorial_step_0_title, R.string.tutorial_step_0_desc),
    TutorialStep(R.string.tutorial_step_1_title, R.string.tutorial_step_1_desc),
    TutorialStep(R.string.tutorial_step_2_title, R.string.tutorial_step_2_desc),
    TutorialStep(R.string.tutorial_step_3_title, R.string.tutorial_step_3_desc),
    TutorialStep(R.string.tutorial_step_4_title, R.string.tutorial_step_4_desc),
)

@Composable
fun TutorialOverlay(
    uiState: TutorialUiState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!uiState.isVisible) return
    val step = tutorialSteps.getOrNull(uiState.currentStep) ?: return
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = OVERLAY_ALPHA)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HORIZONTAL_PADDING_DP.dp),
            shape = RoundedCornerShape(CARD_CORNER_DP.dp)
        ) {
            TutorialCardContent(
                uiState = uiState,
                step = step,
                onNext = onNext,
                onSkip = onSkip
            )
        }
    }
}

@Composable
private fun TutorialCardContent(
    uiState: TutorialUiState,
    step: TutorialStep,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val progress = (uiState.currentStep + 1).toFloat() / uiState.totalSteps
    val isLastStep = uiState.currentStep == uiState.totalSteps - 1
    Column(
        modifier = Modifier.padding(CARD_CONTENT_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = PROGRESS_BOTTOM_PADDING_DP.dp)
        )
        Text(
            text = stringResource(id = step.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = step.descRes),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.tutorial_step_of, uiState.currentStep + 1, uiState.totalSteps),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TutorialButtons(isLastStep = isLastStep, onNext = onNext, onSkip = onSkip)
    }
}

@Composable
private fun TutorialButtons(
    isLastStep: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BUTTON_SPACING_DP.dp)
    ) {
        if (!isLastStep) {
            OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(id = R.string.tutorial_skip))
            }
        }
        Button(onClick = onNext, modifier = Modifier.weight(1f)) {
            val label = if (isLastStep) {
                stringResource(id = R.string.tutorial_finish)
            } else {
                stringResource(id = R.string.tutorial_next)
            }
            Text(text = label)
        }
    }
}
