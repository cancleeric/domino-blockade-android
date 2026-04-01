package com.cancleeric.dominoblockade.presentation.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val LIGHT_DURATION_MS = 50L
private const val STRONG_DURATION_MS = 200L

/**
 * Manages vibration/haptic feedback.
 *
 * All feedback is silently no-op on devices without a vibrator or when disabled.
 */
@Singleton
class HapticFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        context.getSystemService(Vibrator::class.java)
    }

    private var enabled = true

    /** Short, gentle pulse — used when placing a tile or tapping a button. */
    fun lightFeedback() {
        if (!enabled || vibrator?.hasVibrator() != true) return
        vibrate(VibrationEffect.createOneShot(LIGHT_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    /** Strong pulse — used when the game is blocked. */
    fun strongFeedback() {
        if (!enabled || vibrator?.hasVibrator() != true) return
        vibrate(VibrationEffect.createOneShot(STRONG_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    /** Victory pattern — a rising triple pulse. */
    fun victoryFeedback() {
        if (!enabled || vibrator?.hasVibrator() != true) return
        val timings = longArrayOf(0, 100, 50, 100, 50, 200)
        val amplitudes = intArrayOf(0, 128, 0, 200, 0, 255)
        vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    /**
     * Enables or disables haptic feedback.
     * When disabled all feedback calls are silently ignored.
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    private fun vibrate(effect: VibrationEffect) {
        vibrator?.vibrate(effect)
    }
}
