package yos.music.player.ui.pages.settings.audio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import yos.music.player.R
import yos.music.player.code.utils.player.AudioEffects
import yos.music.player.ui.pages.settings.Divider
import yos.music.player.ui.pages.settings.GroupSpacer
import yos.music.player.ui.pages.settings.ListHeader
import yos.music.player.ui.pages.settings.SelectItem
import yos.music.player.ui.pages.settings.SettingBackground
import yos.music.player.ui.pages.settings.SwitchItem
import yos.music.player.ui.widgets.basic.RoundColumn
import yos.music.player.ui.widgets.basic.Title

private val Speeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
private fun speedLabel(speed: Float) = "${speed.toString().removeSuffix(".0")}×"

@Composable
fun EqualizerSettings(navController: NavController) =
    SettingBackground {
        Title(title = stringResource(id = R.string.settings_audio_equalizer),
            onBack = { navController.popBackStack() },
            content = {
                item("settings") { EqualizerBody() }
            }
        )
    }

// Own composable so it is redrawn when the settings change (the list item lambda is not)
@Composable
private fun EqualizerBody() {
                    Column(Modifier.fillMaxSize()) {
                        ListHeader(stringResource(id = R.string.settings_audio_equalizer_eq))
                        val eq = AudioEffects.equalizer.value
                        if (eq == null) {
                            ListHeader(content = stringResource(id = R.string.settings_audio_equalizer_unavailable))
                        } else {
                            val presets = remember(eq) { List(eq.numberOfPresets.toInt()) { eq.getPresetName(it.toShort()) } }
                            val preset = remember { mutableStateOf("") }
                            val (min, max) = eq.bandLevelRange.let { it[0].toFloat() to it[1].toFloat() }
                            val levels = AudioEffects.levels.value
                            RoundColumn {
                                SwitchItem(
                                    title = stringResource(id = R.string.settings_audio_equalizer_eq),
                                    onClick = { AudioEffects.setEnabled(!AudioEffects.enabled.value) },
                                    checkedLambda = { AudioEffects.enabled.value }
                                )
                                if (presets.isNotEmpty()) {
                                    Divider()
                                    SelectItem(
                                        title = stringResource(id = R.string.settings_audio_equalizer_preset),
                                        items = presets,
                                        value = preset.value,
                                        onValueChange = {
                                            preset.value = it
                                            AudioEffects.usePreset(presets.indexOf(it).toShort())
                                        }
                                    )
                                }
                                Divider()
                                repeat(eq.numberOfBands.toInt()) { band ->
                                    val level = levels.getOrNull(band)?.toFloat() ?: eq.getBandLevel(band.toShort()).toFloat()
                                    BandRow(
                                        freqHz = eq.getCenterFreq(band.toShort()) / 1000,
                                        level = level,
                                        range = min..max,
                                        enabled = AudioEffects.enabled.value,
                                        onChange = {
                                            preset.value = ""
                                            AudioEffects.setLevel(band, it.toInt().toShort())
                                        }
                                    )
                                }
                            }
                        }

                        GroupSpacer()
                        ListHeader(stringResource(id = R.string.settings_audio_equalizer_playback))
                        RoundColumn {
                            SelectItem(
                                title = stringResource(id = R.string.settings_audio_equalizer_speed),
                                items = Speeds.map(::speedLabel),
                                value = speedLabel(AudioEffects.speed.value),
                                onValueChange = { label ->
                                    Speeds.firstOrNull { speedLabel(it) == label }?.let { AudioEffects.setSpeed(it) }
                                }
                            )
                            Divider()
                            SwitchItem(
                                title = stringResource(id = R.string.settings_audio_equalizer_keep_pitch),
                                onClick = { AudioEffects.setSpeed(AudioEffects.speed.value, !AudioEffects.keepPitch.value) },
                                checkedLambda = { AudioEffects.keepPitch.value }
                            )
                        }
                        ListHeader(content = stringResource(id = R.string.settings_audio_equalizer_keep_pitch_desc))
                        GroupSpacer()
                    }
}

@Composable
private fun BandRow(freqHz: Int, level: Float, range: ClosedFloatingPointRange<Float>, enabled: Boolean, onChange: (Float) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (freqHz >= 1000) "${freqHz / 1000}k" else "$freqHz",
            fontSize = 14.sp,
            modifier = Modifier.width(48.dp)
        )
        Slider(
            value = level.coerceIn(range),
            onValueChange = onChange,
            valueRange = range,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%+.0f dB".format(level / 100f),
            fontSize = 13.sp,
            modifier = Modifier
                .width(64.dp)
                .padding(start = 8.dp)
        )
    }
}
