package com.cancleeric.dominoblockade.presentation.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// ---------------------------------------------------------------------------
// Domino tile animations
// ---------------------------------------------------------------------------

/**
 * Wraps [content] in a 3-D Y-axis flip animation.
 *
 * [isFlipped] toggles the target rotation between 0° (face-up) and 180°
 * (face-down).  The lambda receives `isFaceUp` so callers can swap the
 * front/back artwork at the mid-point of the rotation.
 */
@Composable
fun DominoFlipAnimation(
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (isFaceUp: Boolean) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "dominoFlip"
    )

    Box(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }
    ) {
        content(rotation < 90f)
    }
}

/**
 * Returns a [Modifier] that slides a domino into view from below when
 * [visible] becomes true, using a spring animation for a natural feel.
 */
@Composable
fun dominoSlideInModifier(visible: Boolean): Modifier {
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 300f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dominoSlideIn"
    )
    return Modifier.graphicsLayer { translationY = offsetY }
}

// ---------------------------------------------------------------------------
// Game-state animations
// ---------------------------------------------------------------------------

/**
 * Returns a continuously animating alpha value suitable for a red blockade
 * flash overlay.  Oscillates between 1f and 0f at 300 ms per cycle.
 */
@Composable
fun rememberBlockadeFlashAlpha(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "blockadeFlash")
    return infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blockadeAlpha"
    )
}

/**
 * Returns a continuously animating scale factor (1f → 1.05f → 1f) used to
 * highlight the current player's UI element.
 */
@Composable
fun rememberTurnHighlightScale(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "turnHighlight")
    return infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "turnScale"
    )
}

/**
 * Returns an animating alpha value (0f → 1f) that drives a confetti/fireworks
 * win overlay fading in over [durationMs] milliseconds.
 */
@Composable
fun rememberWinOverlayAlpha(durationMs: Int = 600): State<Float> =
    animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "winOverlay"
    )
