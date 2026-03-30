package com.cancleeric.dominoblockade.presentation.tutorial

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.R
import com.cancleeric.dominoblockade.domain.model.HighlightTarget

@Composable
fun TutorialScreen(
    onTutorialFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TutorialViewModel = hiltViewModel()
) {
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val currentStep = viewModel.currentStep
    val totalSteps = viewModel.totalSteps

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
    ) {
        // Spotlight / Coach Mark layer
        SpotlightOverlay(target = currentStep.highlightTarget)

        // Tutorial card at bottom
        AnimatedContent(
            targetState = currentStepIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "tutorial_step_transition",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { _ ->
            TutorialCard(
                title = stringResource(currentStep.titleRes),
                message = stringResource(currentStep.messageRes),
                currentStep = currentStepIndex + 1,
                totalSteps = totalSteps,
                isLastStep = viewModel.isLastStep(),
                onNext = {
                    if (viewModel.isLastStep()) {
                        viewModel.completeTutorial()
                        onTutorialFinished()
                    } else {
                        viewModel.nextStep()
                    }
                },
                onPrevious = viewModel::previousStep,
                onSkip = {
                    viewModel.skipTutorial()
                    onTutorialFinished()
                },
                showPrevious = currentStepIndex > 0
            )
        }
    }
}

@Composable
private fun SpotlightOverlay(target: HighlightTarget) {
    if (target == HighlightTarget.NONE) return

    val label = when (target) {
        HighlightTarget.BOARD -> "Board Area"
        HighlightTarget.PLAYER_HAND -> "Your Hand"
        HighlightTarget.BONEYARD -> "Boneyard"
        HighlightTarget.SCORE -> "Score"
        HighlightTarget.NONE -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 120.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White.copy(alpha = 0.15f))
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun TutorialCard(
    title: String,
    message: String,
    currentStep: Int,
    totalSteps: Int,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    showPrevious: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step indicator dots
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(totalSteps) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentStep - 1) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentStep - 1)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = stringResource(R.string.tutorial_skip),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    if (showPrevious) {
                        OutlinedButton(
                            onClick = onPrevious,
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(text = stringResource(R.string.tutorial_previous))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(onClick = onNext) {
                        Text(
                            text = if (isLastStep)
                                stringResource(R.string.tutorial_finish)
                            else
                                stringResource(R.string.tutorial_next)
                        )
                    }
                }
            }
        }
    }
}
