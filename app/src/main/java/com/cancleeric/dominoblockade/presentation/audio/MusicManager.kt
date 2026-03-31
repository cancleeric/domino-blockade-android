package com.cancleeric.dominoblockade.presentation.audio

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages background music using [MediaPlayer].
 *
 * BGM files must be placed in `res/raw/` with names `music_menu` and `music_game`.
 * Missing files are silently ignored.
 */
@Singleton
class MusicManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var enabled = true

    /** Starts looping menu background music. Stops any currently playing track. */
    fun playMenuMusic() {
        if (!enabled) return
        playMusic("music_menu")
    }

    /** Starts looping in-game background music. Stops any currently playing track. */
    fun playGameMusic() {
        if (!enabled) return
        playMusic("music_game")
    }

    private fun playMusic(resourceName: String) {
        stop()
        val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resId == 0) return
        mediaPlayer = MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            start()
        }
    }

    /** Stops the currently playing track and releases resources. */
    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    /**
     * Enables or disables background music playback.
     * When disabled, the current track is stopped immediately.
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) stop()
    }

    /** Releases all resources held by this manager. */
    fun release() = stop()
}
