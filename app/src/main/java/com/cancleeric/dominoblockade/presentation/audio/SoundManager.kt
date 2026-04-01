package com.cancleeric.dominoblockade.presentation.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_STREAMS = 4
private const val LOAD_PRIORITY = 1
private const val PLAY_RATE = 1f
private const val PLAY_VOLUME = 1f
private const val PLAY_PRIORITY = 1
private const val PLAY_LOOP_NONE = 0

/** Sound events corresponding to in-game actions. */
enum class SoundEvent {
    PLACE_DOMINO,
    DRAW_DOMINO,
    BLOCKED,
    WIN,
    LOSE,
    BUTTON_CLICK,
}

/**
 * Manages short sound effects using [SoundPool].
 *
 * Sound files must be placed in `res/raw/` with the following names:
 * `sound_place_domino`, `sound_draw_domino`, `sound_blocked`,
 * `sound_win`, `sound_lose`, `sound_button_click`.
 *
 * Missing sound files are silently ignored; the manager will not crash.
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds: MutableMap<SoundEvent, Int> = mutableMapOf()
    private var enabled = true

    init {
        loadSounds()
    }

    private fun loadSounds() {
        loadSound(SoundEvent.PLACE_DOMINO, "sound_place_domino")
        loadSound(SoundEvent.DRAW_DOMINO, "sound_draw_domino")
        loadSound(SoundEvent.BLOCKED, "sound_blocked")
        loadSound(SoundEvent.WIN, "sound_win")
        loadSound(SoundEvent.LOSE, "sound_lose")
        loadSound(SoundEvent.BUTTON_CLICK, "sound_button_click")
    }

    private fun loadSound(event: SoundEvent, resourceName: String) {
        val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resId != 0) {
            soundIds[event] = soundPool.load(context, resId, LOAD_PRIORITY)
        }
    }

    fun playPlaceDomino() = play(SoundEvent.PLACE_DOMINO)

    fun playDrawDomino() = play(SoundEvent.DRAW_DOMINO)

    fun playBlocked() = play(SoundEvent.BLOCKED)

    fun playWin() = play(SoundEvent.WIN)

    fun playLose() = play(SoundEvent.LOSE)

    fun playButtonClick() = play(SoundEvent.BUTTON_CLICK)

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    private fun play(event: SoundEvent) {
        if (!enabled) return
        val soundId = soundIds[event] ?: return
        soundPool.play(soundId, PLAY_VOLUME, PLAY_VOLUME, PLAY_PRIORITY, PLAY_LOOP_NONE, PLAY_RATE)
    }

    fun release() {
        soundPool.release()
    }
}
