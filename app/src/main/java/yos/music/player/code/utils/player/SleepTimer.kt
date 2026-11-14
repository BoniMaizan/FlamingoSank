package yos.music.player.code.utils.player

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableLongStateOf
import androidx.media3.exoplayer.ExoPlayer
import yos.music.player.code.utils.player.FadeExo.fadePause

@Stable
object SleepTimer {
    const val END_OF_SONG = -1L

    // Time (epoch ms) when playback stops, END_OF_SONG, or 0 when off
    val stopAt = mutableLongStateOf(0L)

    // Set by the playback service
    var player: ExoPlayer? = null

    private val handler = Handler(Looper.getMainLooper())
    private val stop = Runnable {
        stopAt.longValue = 0L
        player?.fadePause()
    }

    fun start(minutes: Int) {
        cancel()
        stopAt.longValue = System.currentTimeMillis() + minutes * 60_000L
        handler.postDelayed(stop, minutes * 60_000L)
    }

    fun startEndOfSong() {
        cancel()
        stopAt.longValue = END_OF_SONG
        player?.pauseAtEndOfMediaItems = true
    }

    fun cancel() {
        handler.removeCallbacks(stop)
        stopAt.longValue = 0L
        player?.pauseAtEndOfMediaItems = false
    }

    // Called when the player paused by itself at the end of a song
    fun onSongEndPause() {
        if (stopAt.longValue == END_OF_SONG) cancel()
    }
}
