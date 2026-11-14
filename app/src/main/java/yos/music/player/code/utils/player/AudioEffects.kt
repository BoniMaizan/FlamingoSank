package yos.music.player.code.utils.player

import android.media.audiofx.Equalizer
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import yos.music.player.data.libraries.SettingsLibrary

// Equaliser (Android's built-in effect, so the band count depends on the device) and playback speed
@Stable
object AudioEffects {
    // Null when the device has no equaliser or no player is running
    val equalizer = mutableStateOf<Equalizer?>(null)
    private var player: ExoPlayer? = null

    // Observable copies of the saved settings: the saved values alone don't redraw the UI
    val enabled = mutableStateOf(SettingsLibrary.EqEnabled)
    val levels = mutableStateOf(SettingsLibrary.EqLevels.split(",").mapNotNull { it.toShortOrNull() })
    val speed = mutableStateOf(SettingsLibrary.PlaybackSpeed.toFloatOrNull() ?: 1f)
    val keepPitch = mutableStateOf(SettingsLibrary.KeepPitch)

    fun attach(player: ExoPlayer) {
        this.player = player
        equalizer.value?.release()
        equalizer.value = runCatching { Equalizer(0, player.audioSessionId) }.getOrNull()
        applyEqualizer()
        applySpeed()
    }

    fun release() {
        equalizer.value?.release()
        equalizer.value = null
        player = null
    }

    fun setEnabled(enabled: Boolean) {
        SettingsLibrary.EqEnabled = enabled
        this.enabled.value = enabled
        applyEqualizer()
    }

    fun setLevel(band: Int, level: Short) {
        val eq = equalizer.value ?: return
        val current = levels.value.takeIf { it.size == eq.numberOfBands.toInt() }
            ?: List(eq.numberOfBands.toInt()) { eq.getBandLevel(it.toShort()) }
        saveLevels(current.toMutableList().also { it[band] = level })
        applyEqualizer()
    }

    fun usePreset(preset: Short) {
        val eq = equalizer.value ?: return
        runCatching { eq.usePreset(preset) }
        saveLevels(List(eq.numberOfBands.toInt()) { eq.getBandLevel(it.toShort()) })
        applyEqualizer()
    }

    private fun saveLevels(list: List<Short>) {
        levels.value = list
        SettingsLibrary.EqLevels = list.joinToString(",")
    }

    private fun applyEqualizer() {
        val eq = equalizer.value ?: return
        runCatching {
            levels.value.takeIf { it.size == eq.numberOfBands.toInt() }
                ?.forEachIndexed { band, level -> eq.setBandLevel(band.toShort(), level) }
            eq.enabled = enabled.value
        }
    }

    fun setSpeed(speed: Float, keepPitch: Boolean = this.keepPitch.value) {
        SettingsLibrary.PlaybackSpeed = speed.toString()
        SettingsLibrary.KeepPitch = keepPitch
        this.speed.value = speed
        this.keepPitch.value = keepPitch
        applySpeed()
    }

    private fun applySpeed() {
        val speed = speed.value
        player?.playbackParameters = PlaybackParameters(speed, if (keepPitch.value) 1f else speed)
    }
}
