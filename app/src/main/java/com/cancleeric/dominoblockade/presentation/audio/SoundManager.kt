package com.cancleeric.dominoblockade.presentation.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.cancleeric.dominoblockade.data.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages short sound effects using SoundPool.
 *
 * Sound files should be placed in res/raw/ with the following names:
 *   place_domino.ogg, draw_domino.ogg, blocked.ogg, win.ogg, lose.ogg, button_click.ogg
 *
 * When a sound file is absent the corresponding play call is silently ignored.
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var isEnabled = true

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<SoundType, Int>()

    init {
        scope.launch {
            settingsRepository.soundEnabled.collect { enabled ->
                isEnabled = enabled
            }
        }
        loadSounds()
    }

    private fun loadSounds() {
        // Load each sound from res/raw/ — failures are handled gracefully via the null-safe play()
        SoundType.entries.forEach { type ->
            val resId = type.resName.toRawResId(context)
            if (resId != 0) {
                soundIds[type] = soundPool.load(context, resId, PRIORITY)
            }
        }
    }

    fun playPlaceDomino() = play(SoundType.PLACE_DOMINO)

    fun playDrawDomino() = play(SoundType.DRAW_DOMINO)

    fun playBlocked() = play(SoundType.BLOCKED)

    fun playWin() = play(SoundType.WIN)

    fun playLose() = play(SoundType.LOSE)

    fun playButtonClick() = play(SoundType.BUTTON_CLICK)

    private fun play(type: SoundType) {
        if (!isEnabled) return
        val soundId = soundIds[type] ?: return
        if (soundId > 0) {
            soundPool.play(soundId, FULL_VOLUME, FULL_VOLUME, PRIORITY, NO_LOOP, NORMAL_RATE)
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        scope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }

    fun release() {
        soundPool.release()
    }

    enum class SoundType(val resName: String) {
        PLACE_DOMINO("place_domino"),
        DRAW_DOMINO("draw_domino"),
        BLOCKED("blocked"),
        WIN("win"),
        LOSE("lose"),
        BUTTON_CLICK("button_click")
    }

    private companion object {
        const val MAX_STREAMS = 5
        const val PRIORITY = 1
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f
        const val FULL_VOLUME = 1f
    }
}

private fun String.toRawResId(context: Context): Int =
    context.resources.getIdentifier(this, "raw", context.packageName)
