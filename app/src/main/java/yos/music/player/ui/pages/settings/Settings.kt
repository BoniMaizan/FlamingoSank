package yos.music.player.ui.pages.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yos.music.player.R
import yos.music.player.code.MediaController
import yos.music.player.data.libraries.HistoryLibrary
import yos.music.player.data.libraries.MusicLibrary
import yos.music.player.data.libraries.SettingsLibrary
import yos.music.player.ui.UI
import yos.music.player.ui.toUI
import yos.music.player.ui.pages.isWideLayout
import yos.music.player.ui.widgets.basic.OptionDialog
import yos.music.player.ui.widgets.basic.RoundColumn
import yos.music.player.ui.widgets.basic.Title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavController, selected: String? = null) =
    SettingBackground {
        val context = LocalContext.current
        val wide = isWideLayout()
        // Tablets: sub-pages replace each other next to this list instead of stacking up
        val open: (String) -> Unit = { route ->
            if (wide) navController.navigate(route) {
                popUpTo(UI.Settings.Main)
                launchSingleTop = true
            } else navController.toUI(route)
        }
        Title(title = stringResource(id = R.string.page_settings_title),
            onBack = {
                // from the side list, leave Settings entirely rather than just the open sub-page
                navController.popBackStack(UI.Settings.Main, inclusive = true)
            },
            content = {
                item("settings") {
                    Column(Modifier.fillMaxSize()) {
                        // GroupSpacerMedium()
                        ListHeader(stringResource(id = R.string.page_library_title))
                        RoundColumn {
                            SwitchItem(
                                title = stringResource(id = R.string.settings_library_refresh_everytime),
                                onClick = {
                                    SettingsLibrary.RefreshEveryTime =
                                        !SettingsLibrary.RefreshEveryTime
                                },
                                checkedLambda = { SettingsLibrary.RefreshEveryTime }
                            )

                            Divider()
                            LabelItem(selected = selected == UI.Settings.LibraryOverview, title = stringResource(id = R.string.settings_library_overview)) {
                                open(UI.Settings.LibraryOverview)
                            }
                        }

                        GroupSpacerMedium()
                        RoundColumn {
                            val scope = rememberCoroutineScope()
                            LabelItem(
                                title = stringResource(id = R.string.settings_library_refresh_now),
                                //desc = stringResource(id = R.string.settings_library_refresh_now_desc)
                                superLink = true
                            ) {
                                scope.launch(Dispatchers.Main) {
                                    var toast = Toast.makeText(
                                        context,
                                        R.string.tip_scanning,
                                        Toast.LENGTH_SHORT
                                    )
                                    toast.show()
                                    withContext(Dispatchers.IO) {
                                        MusicLibrary.scanMedia(context)
                                    }
                                    toast.cancel()
                                    val size = MediaController.mainMusicList.size
                                    if (size == 0) {
                                        toast = Toast.makeText(
                                            context,
                                            R.string.tip_no_song,
                                            Toast.LENGTH_SHORT
                                        )
                                    } else {
                                        val msg =
                                            context.getString(R.string.tip_scan_finished, size)
                                        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                                    }
                                    toast.show()
                                }
                            }
                        }
                        ListHeader(content = stringResource(id = R.string.settings_library_refresh_now_desc))

                        GroupSpacer()
                        ListHeader(stringResource(id = R.string.settings_performance))
                        RoundColumn {
                            LabelItem(selected = selected == UI.Settings.LyricSetting, title = stringResource(id = R.string.settings_performance_lyric_title)) {
                                open(UI.Settings.LyricSetting)
                            }
                            Divider()
                            LabelItem(selected = selected == UI.Settings.UserInterfaceSetting, title = stringResource(id = R.string.settings_performance_ui_title)) {
                                open(UI.Settings.UserInterfaceSetting)
                            }
                            Divider()
                            LabelItem(selected = selected == UI.Settings.NotificationSetting, title = stringResource(id = R.string.settings_performance_notification_title)) {
                                open(UI.Settings.NotificationSetting)
                            }
                        }

                        GroupSpacer()
                        ListHeader(stringResource(id = R.string.settings_audio))
                        RoundColumn {
                            LabelItem(selected = selected == UI.Settings.ExoplayerSetting, title = stringResource(id = R.string.settings_audio_exoplayer)) {
                                open(UI.Settings.ExoplayerSetting)
                            }
                            Divider()
                            LabelItem(selected = selected == UI.Settings.EqualizerSetting, title = stringResource(id = R.string.settings_audio_equalizer)) {
                                open(UI.Settings.EqualizerSetting)
                            }
                            Divider()
                            SwitchItem(
                                title = stringResource(id = R.string.settings_audio_fade_in_out),
                                // desc = stringResource(id = R.string.settings_audio_fade_in_out_desc),
                                onClick = { SettingsLibrary.FadePlay = !SettingsLibrary.FadePlay },
                                checkedLambda = { SettingsLibrary.FadePlay }
                            )
                        }
                        ListHeader(content = stringResource(id = R.string.settings_audio_fade_in_out_desc))

                        GroupSpacer()
                        ListHeader(stringResource(id = R.string.settings_play))
                        RoundColumn {
                            SwitchItem(
                                title = stringResource(id = R.string.settings_play_history),
                                // desc = stringResource(id = R.string.settings_play_history_desc),
                                onClick = { SettingsLibrary.ListenHistory = !SettingsLibrary.ListenHistory },
                                checkedLambda = { SettingsLibrary.ListenHistory }
                            )
                            Divider()
                            val confirmClear = remember { mutableStateOf(false) }
                            LabelItem(
                                title = stringResource(id = R.string.settings_play_history_clear),
                                superLink = true
                            ) { confirmClear.value = true }
                            if (confirmClear.value) {
                                OptionDialog(
                                    icon = {},
                                    title = stringResource(id = R.string.settings_play_history_clear_confirm),
                                    content = null,
                                    positiveContent = stringResource(id = R.string.settings_play_history_clear),
                                    negativeContent = stringResource(id = R.string.settings_play_history_clear_cancel),
                                    onPositive = {
                                        HistoryLibrary.clear()
                                        confirmClear.value = false
                                    },
                                    onNegative = { confirmClear.value = false },
                                    onDismissRequest = { confirmClear.value = false }
                                )
                            }
                        }
                        ListHeader(content = stringResource(id = R.string.settings_play_history_desc))

                        GroupSpacer()
                        ListHeader(stringResource(id = R.string.settings_extend))
                        RoundColumn {
                            LabelItem(selected = selected == UI.Settings.LyricGetter, 
                                title = stringResource(id = R.string.settings_extend_statusbarlyric),
                                // desc = stringResource(id = R.string.settings_extend_statusbarlyric_desc)
                            ) {
                                open(UI.Settings.LyricGetter)
                            }
                        }

                        GroupSpacer()
                        ListHeader(stringResource(id = R.string.settings_others))
                        RoundColumn {
                            LabelItem(selected = selected == UI.Settings.About, 
                                title = stringResource(id = R.string.settings_others_about),
                                // desc = stringResource(id = R.string.settings_others_about_desc)
                            ) {
                                open(UI.Settings.About)
                            }
                        }
                    }
                }

                /*item("blank") {
                    Spacer(modifier = Modifier.height(15.dp))
                }*/
            })
    }


fun safeStartActivity(context: Context, intent: Intent, options: Bundle?) {
    if (intent.resolveActivity(context.packageManager) != null) {
        ContextCompat.startActivity(context, intent, options)
    } else {
        Toast.makeText(
            context,
            context.getString(R.string.tip_intent_resolve_failed),
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun startWeb(url: String, context: Context) {
    try {
        val uri: Uri =
            Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        safeStartActivity(context, intent, null)
    } catch (_: Exception) {
    }
}
