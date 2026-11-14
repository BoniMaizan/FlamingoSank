package yos.music.player.code.utils.player

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.media3.exoplayer.ExoPlayer
import yos.music.player.code.utils.player.FadeExo.fadePause
import yos.music.player.code.utils.player.FadeExo.slowFadePause
import yos.music.player.data.libraries.SettingsLibrary

@Stable
object SleepTimer {
    const val END_OF_SONG = -1L
    private const val FADE_MS = 30_000L

    // Time (epoch ms) when playback stops, END_OF_SONG, or 0 when off
    val stopAt = mutableLongStateOf(0L)

    // Lower the volume over the last 30 seconds instead of stopping at once
    val fadeOut = mutableStateOf(SettingsLibrary.SleepTimerFade)

    // Set by the playback service
    var player: ExoPlayer? = null

    private val handler = Handler(Looper.getMainLooper())
    private val stop = Runnable {
        stopAt.longValue = 0L
        player?.fadePause()
    }
    private val fade = Runnable {
        stopAt.longValue = 0L
        player?.slowFadePause(FADE_MS)
    }

    fun start(minutes: Int) {
        cancel()
        val ms = minutes * 60_000L
        stopAt.longValue = System.currentTimeMillis() + ms
        if (fadeOut.value && ms > FADE_MS) handler.postDelayed(fade, ms - FADE_MS)
        else handler.postDelayed(stop, ms)
    }

    fun startEndOfSong() {
        cancel()
        stopAt.longValue = END_OF_SONG
        player?.pauseAtEndOfMediaItems = true
    }

    fun cancel() {
        handler.removeCallbacks(stop)
        handler.removeCallbacks(fade)
        stopAt.longValue = 0L
        player?.pauseAtEndOfMediaItems = false
    }

    fun setFadeOut(enabled: Boolean) {
        fadeOut.value = enabled
        SettingsLibrary.SleepTimerFade = enabled
        // Re-plan a running minutes timer with the new choice
        val left = stopAt.longValue - System.currentTimeMillis()
        if (stopAt.longValue > 0 && left > 0) {
            handler.removeCallbacks(stop)
            handler.removeCallbacks(fade)
            if (enabled && left > FADE_MS) handler.postDelayed(fade, left - FADE_MS)
            else handler.postDelayed(stop, left)
        }
    }

    // Called when the player paused by itself at the end of a song
    fun onSongEndPause() {
        if (stopAt.longValue == END_OF_SONG) cancel()
    }
}
