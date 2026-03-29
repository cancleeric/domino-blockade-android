package com.cancleeric.dominoblockade.presentation.audio

import android.content.Context
import android.media.MediaPlayer
import com.cancleeric.dominoblockade.data.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages looping background music tracks using MediaPlayer.
 *
 * BGM files should be placed in res/raw/ with the following names:
 *   menu_bgm.ogg, game_bgm.ogg
 *
 * When a music file is absent the corresponding play call is silently ignored.
 */
@Singleton
class MusicManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var isEnabled = true
    private var currentPlayer: MediaPlayer? = null
    private var currentTrack: MusicTrack? = null

    init {
        scope.launch {
            settingsRepository.musicEnabled.collect { enabled ->
                isEnabled = enabled
                if (!enabled) {
                    pause()
                } else {
                    currentTrack?.let { resume() }
                }
            }
        }
    }

    fun playMenuMusic() = playTrack(MusicTrack.MENU)

    fun playGameMusic() = playTrack(MusicTrack.GAME)

    private fun playTrack(track: MusicTrack) {
        if (!isEnabled) return
        if (currentTrack == track && currentPlayer?.isPlaying == true) return

        stopCurrentPlayer()
        currentTrack = track

        val resId = track.resName.toRawResId(context)
        if (resId == 0) return

        currentPlayer = MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            setVolume(BGM_VOLUME, BGM_VOLUME)
            start()
        }
    }

    fun pause() {
        currentPlayer?.takeIf { it.isPlaying }?.pause()
    }

    fun resume() {
        if (!isEnabled) return
        currentPlayer?.takeIf { !it.isPlaying }?.start()
    }

    fun stop() {
        stopCurrentPlayer()
        currentTrack = null
    }

    private fun stopCurrentPlayer() {
        currentPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        currentPlayer = null
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        scope.launch {
            settingsRepository.setMusicEnabled(enabled)
        }
        if (!enabled) {
            pause()
        } else {
            currentTrack?.let { resume() }
        }
    }

    fun release() {
        stopCurrentPlayer()
    }

    enum class MusicTrack(val resName: String) {
        MENU("menu_bgm"),
        GAME("game_bgm")
    }

    private companion object {
        const val BGM_VOLUME = 0.5f
    }
}

private fun String.toRawResId(context: Context): Int =
    context.resources.getIdentifier(this, "raw", context.packageName)
