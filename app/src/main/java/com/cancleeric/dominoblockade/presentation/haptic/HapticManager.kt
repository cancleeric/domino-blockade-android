package com.cancleeric.dominoblockade.presentation.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.cancleeric.dominoblockade.data.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides haptic feedback for key game events.
 *
 * Requires the VIBRATE permission declared in AndroidManifest.xml.
 * The vibration enabled/disabled preference is persisted via [SettingsRepository].
 */
@Singleton
class HapticManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var isEnabled = true

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    init {
        scope.launch {
            settingsRepository.vibrationEnabled.collect { enabled ->
                isEnabled = enabled
            }
        }
    }

    /** Light vibration when a domino is placed. */
    fun vibrateOnPlace() {
        if (!isEnabled) return
        vibrate(VibrationPattern.LIGHT)
    }

    /** Strong double pulse when the game is blocked. */
    fun vibrateOnBlocked() {
        if (!isEnabled) return
        vibrate(VibrationPattern.STRONG)
    }

    /** Celebratory wave vibration on a win. */
    fun vibrateOnWin() {
        if (!isEnabled) return
        vibrate(VibrationPattern.WIN)
    }

    private fun vibrate(pattern: VibrationPattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern.timings, pattern.amplitudes, NO_REPEAT)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern.timings, NO_REPEAT)
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        scope.launch {
            settingsRepository.setVibrationEnabled(enabled)
        }
    }

    private enum class VibrationPattern(
        val timings: LongArray,
        val amplitudes: IntArray
    ) {
        /** Short 50 ms tap at medium amplitude. */
        LIGHT(
            timings = longArrayOf(0L, 50L),
            amplitudes = intArrayOf(0, 80)
        ),
        /** Two strong 100 ms pulses separated by a 50 ms pause. */
        STRONG(
            timings = longArrayOf(0L, 100L, 50L, 100L),
            amplitudes = intArrayOf(0, 255, 0, 255)
        ),
        /** Rising three-pulse pattern celebrating a win. */
        WIN(
            timings = longArrayOf(0L, 100L, 100L, 100L, 100L, 200L),
            amplitudes = intArrayOf(0, 128, 0, 200, 0, 255)
        )
    }

    private companion object {
        const val NO_REPEAT = -1
    }
}
