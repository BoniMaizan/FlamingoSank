package yos.music.player.ui.pages.settings.audio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import yos.music.player.R
import yos.music.player.code.utils.player.AudioEffects
import yos.music.player.ui.pages.settings.Divider
import yos.music.player.ui.pages.settings.GroupSpacer
import yos.music.player.ui.pages.settings.ListHeader
import yos.music.player.ui.pages.settings.SettingBackground
import yos.music.player.ui.pages.settings.SwitchItem
import yos.music.player.ui.widgets.basic.Pill
import yos.music.player.ui.widgets.basic.RoundColumn
import yos.music.player.ui.widgets.basic.Segmented
import yos.music.player.ui.widgets.basic.Title

private val Speeds = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
private fun speedLabel(speed: Float) = "${speed.toString().removeSuffix(".0")}×"
private fun dbLabel(millibels: Float) = "%+.0f dB".format(millibels / 100f).replace("+0 ", "0 ")

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
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EqualizerBody() {
    Column(Modifier.fillMaxSize()) {
        ListHeader(stringResource(id = R.string.settings_audio_equalizer_eq))
        val eq = AudioEffects.equalizer.value
        if (eq == null) {
            ListHeader(content = stringResource(id = R.string.settings_audio_equalizer_unavailable))
        } else {
            val presets = remember(eq) { List(eq.numberOfPresets.toInt()) { eq.getPresetName(it.toShort()) } }
            // "" means the bands were set by hand
            val preset = remember { mutableStateOf("") }
            val (min, max) = eq.bandLevelRange.let { it[0].toFloat() to it[1].toFloat() }
            val levels = AudioEffects.levels.value
            val enabled = AudioEffects.enabled.value
            RoundColumn {
                SwitchItem(
                    title = stringResource(id = R.string.settings_audio_equalizer_eq),
                    onClick = { AudioEffects.setEnabled(!enabled) },
                    checkedLambda = { AudioEffects.enabled.value }
                )
                Divider()
                if (presets.isNotEmpty()) FlowRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Pill(stringResource(id = R.string.settings_audio_equalizer_custom), preset.value.isEmpty()) {}
                    presets.forEachIndexed { index, name ->
                        Pill(name, preset.value == name) {
                            preset.value = name
                            AudioEffects.usePreset(index.toShort())
                        }
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 14.dp)
                        .alpha(if (enabled) 1f else 0.45f)
                ) {
                    // Scale on the left, lined up with the top, middle and bottom of the tracks
                    Column(
                        Modifier
                            .height(BandHeight)
                            .padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(max, 0f, min).forEach {
                            Text(dbLabel(it), fontSize = 11.sp, modifier = Modifier.alpha(0.5f))
                        }
                    }
                    repeat(eq.numberOfBands.toInt()) { band ->
                        val hz = eq.getCenterFreq(band.toShort()) / 1000
                        VerticalBand(
                            label = if (hz >= 1000) "${hz / 1000}k" else "$hz",
                            level = levels.getOrNull(band)?.toFloat() ?: eq.getBandLevel(band.toShort()).toFloat(),
                            range = min..max,
                            enabled = enabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            preset.value = ""
                            AudioEffects.setLevel(band, it.toInt().toShort())
                        }
                    }
                }
            }
        }

        GroupSpacer()
        ListHeader(stringResource(id = R.string.settings_audio_equalizer_playback))
        RoundColumn {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(id = R.string.settings_audio_equalizer_speed),
                    modifier = Modifier
                        .weight(1f)
                        .alpha(0.94f)
                )
                Segmented(
                    options = Speeds.map(::speedLabel),
                    selected = Speeds.indexOf(AudioEffects.speed.value),
                    onSelect = { AudioEffects.setSpeed(Speeds[it]) },
                    modifier = Modifier.width(320.dp)
                )
            }
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

private val BandHeight = 180.dp

// One band: a vertical track filled from 0 dB, with a round knob. Tap or drag to set it
@Composable
private fun VerticalBand(
    label: String,
    level: Float,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    modifier: Modifier,
    onChange: (Float) -> Unit
) {
    val change by rememberUpdatedState(onChange)
    val fill = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
    val span = range.endInclusive - range.start
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            Modifier
                .width(36.dp)
                .height(BandHeight)
                .semantics {
                    contentDescription = label
                    stateDescription = dbLabel(level)
                    progressBarRangeInfo = ProgressBarRangeInfo(level.coerceIn(range), range)
                    if (enabled) setProgress { change(it.coerceIn(range)); true }
                }
                .pointerInput(enabled, range) {
                    if (!enabled) return@pointerInput
                    val knob = 10.dp.toPx()
                    val at = { y: Float -> range.endInclusive - ((y - knob) / (size.height - 2 * knob)).coerceIn(0f, 1f) * span }
                    detectTapGestures { change(at(it.y)) }
                }
                .pointerInput(enabled, range) {
                    if (!enabled) return@pointerInput
                    val knob = 10.dp.toPx()
                    val at = { y: Float -> range.endInclusive - ((y - knob) / (size.height - 2 * knob)).coerceIn(0f, 1f) * span }
                    detectVerticalDragGestures { pointer, _ ->
                        pointer.consume()
                        change(at(pointer.position.y))
                    }
                }
        ) {
            val knob = 10.dp.toPx()
            val cx = size.width / 2
            val yOf = { v: Float -> knob + (range.endInclusive - v.coerceIn(range)) / span * (size.height - 2 * knob) }
            drawLine(track, Offset(cx, knob), Offset(cx, size.height - knob), 4.dp.toPx(), StrokeCap.Round)
            val y = yOf(level)
            drawLine(fill, Offset(cx, yOf(0f)), Offset(cx, y), 6.dp.toPx(), StrokeCap.Round)
            drawCircle(Color.Black.copy(alpha = 0.3f), knob + 1.dp.toPx(), Offset(cx, y + 1.dp.toPx()))
            drawCircle(Color.White, knob, Offset(cx, y))
        }
        Text(label, fontSize = 12.sp, modifier = Modifier
            .padding(top = 6.dp)
            .alpha(0.6f))
    }
}
