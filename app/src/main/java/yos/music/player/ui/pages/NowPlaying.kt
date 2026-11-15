@file:Suppress("DEPRECATION")

package yos.music.player.ui.pages

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import yos.music.player.ui.widgets.basic.Segmented
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.mutableIntStateOf
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.MaterialTheme
import yos.music.player.data.libraries.defaultTitle
import yos.music.player.ui.widgets.basic.MenuGroup
import yos.music.player.ui.widgets.basic.MenuRow
import yos.music.player.ui.widgets.basic.SheetHeader
import yos.music.player.ui.widgets.basic.tileColor
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentUris
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderPositions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastMap
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.blankj.utilcode.util.TimeUtils
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.rememberModalBottomSheetState
import yos.music.player.data.objects.LibraryObject
import yos.music.player.ui.UI
import yos.music.player.ui.toUI
import yos.music.player.ui.pages.settings.SelectObjectItem
import yos.music.player.ui.pages.settings.SwitchItem
import yos.music.player.ui.widgets.basic.OptionDialog
import yos.music.player.code.utils.player.SleepTimer
import kotlinx.coroutines.withContext
import yos.music.player.R
import yos.music.player.code.MediaController
import yos.music.player.code.MediaController.mediaControl
import yos.music.player.code.MediaController.musicPlaying
import yos.music.player.code.MediaController.playingMusicList
import yos.music.player.code.SystemMediaControlResolver
import yos.music.player.code.VolumeChangeReceiver
import yos.music.player.code.YosPlaybackService
import yos.music.player.code.utils.lrc.YosMediaEvent
import yos.music.player.code.utils.lrc.YosUIConfig
import yos.music.player.code.utils.others.Vibrator
import yos.music.player.code.utils.player.FadeExo.fadePause
import yos.music.player.code.utils.player.FadeExo.fadePlay
import yos.music.player.data.libraries.FavPlayListLibrary
import yos.music.player.data.libraries.PlayList
import yos.music.player.data.libraries.PlayListLibrary
import yos.music.player.data.libraries.MusicLibrary
import yos.music.player.data.libraries.artistsList
import yos.music.player.ui.widgets.basic.DialogContent
import yos.music.player.ui.widgets.basic.SearchTextField
import yos.music.player.data.libraries.SettingsLibrary
import yos.music.player.data.libraries.YosMediaItem
import yos.music.player.data.libraries.artistsName
import yos.music.player.data.libraries.defaultArtistsName
import yos.music.player.data.libraries.defaultTitle
import yos.music.player.data.models.MainViewModel
import yos.music.player.data.models.MediaViewModel
import yos.music.player.data.objects.MediaViewModelObject
import yos.music.player.ui.pages.NowPlayingPage.Album
import yos.music.player.ui.pages.NowPlayingPage.Lyric
import yos.music.player.ui.pages.NowPlayingPage.PlayingList
import yos.music.player.ui.theme.YosRoundedCornerShape
import yos.music.player.ui.widgets.YosLyricView
import yos.music.player.ui.widgets.effects.YosFloatingLight
import yos.music.player.ui.widgets.audio.MusicQualityIndicator
import yos.music.player.ui.widgets.basic.ImageQuality
import yos.music.player.ui.widgets.basic.ShadowImageWithCache
import yos.music.player.ui.widgets.basic.YosWrapper
import yos.music.player.ui.widgets.effects.ShadowType
import yos.music.player.ui.widgets.effects.overlayEffect


@Stable
object NowPlayingPage {
    const val Album = "Album"
    const val PlayingList = "PlayingList"
    const val Lyric = "Lyric"

    // Tablet only: song details in the side pane
    const val Info = "Info"
}

private const val ShareAlbumKey = "album"
private const val AnimDurationMillis = 300

/*
private val MaterialFadeInTransitionSpec
    get() = SharedElementsTransitionSpec(
        pathMotionFactory = LinearMotionFactory,
        durationMillis = AnimDurationMillis,
        fadeMode = FadeMode.In,
        easing = EaseOutQuart
    )

private val MaterialFadeOutTransitionSpec
    get() = SharedElementsTransitionSpec(
        pathMotionFactory = LinearMotionFactory,
        durationMillis = AnimDurationMillis,
        fadeMode = FadeMode.Out,
        easing = EaseOutQuart
    )
*/

private val WidePaneWidth = 360.dp

// Tablets: top of the right card, level with the cover (22dp above the player column, 20dp above the cover, 7dp inside the image)
private val WideCardTop = 49.dp

@Composable
private fun TranslationToggle(
    enabled: Boolean,
    translation: MutableState<Boolean>,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .overlayEffect()
            .alpha(0.4f)
            // Only hit-testable while visible: at alpha 0 it sat over the "…" button and swallowed its taps
            .then(if (!enabled) Modifier else Modifier.clickable(onClick = onToggle, indication = null, interactionSource = interaction)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetState = translation.value, transitionSpec = { fadeIn() togetherWith fadeOut() }) {
            Icon(
                painterResource(id = if (it) R.drawable.ic_nowplaying_translateon else R.drawable.ic_nowplaying_translate),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@ExperimentalSharedTransitionApi
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun NowPlaying(
    mainViewModel: MainViewModel,
    mediaViewModel: MediaViewModel,
    navController: NavController,
    isPlayingStatusLambda: () -> Boolean,
    isPlayingOnChanged: (Boolean) -> Unit,
    nowPageLambda: () -> String,
    showMiniPlayer: () -> Boolean,
    onClose: () -> Unit,
    nowPageOnChanged: (String) -> Unit
) =
    Surface(
        modifier = Modifier.fillMaxSize(),
        contentColor = Color.White,
        color = Color.Transparent
    ) {
        val context = LocalContext.current

        // Tablet in landscape: a centred player column like the installed app, lyrics and queue beside it
        val configuration = LocalConfiguration.current
        val wide = configuration.screenWidthDp > configuration.screenHeightDp && configuration.screenWidthDp >= 600
        val screenWidth = configuration.screenWidthDp.dp
        // the player column takes 38% of the width, like the mockup
        val paneWidth = (screenWidth * 0.35f).coerceIn(WidePaneWidth, 520.dp)
        val paneStart by animateDpAsState(
            targetValue = when {
                !wide -> 0.dp
                else -> (screenWidth / 2 - paneWidth) / 2
            }, label = "NowPlaying_paneStart"
        )
        // wrapContentWidth: the Surface parent forces its full width as a minimum
        val paneModifier = if (wide) Modifier.wrapContentWidth(Alignment.Start).padding(start = paneStart).width(paneWidth) else Modifier
        val sideModifier = if (wide) Modifier.padding(start = screenWidth / 2, end = 32.dp) else Modifier
        // Tablets always show a side pane: the queue unless lyrics or info were picked
        if (wide) LaunchedEffect(nowPageLambda()) {
            if (nowPageLambda() == Album) nowPageOnChanged(PlayingList)
        }

        val lrcEntries: MutableState<List<List<Pair<Float, String>>>> =
            MediaViewModelObject.lrcEntries
        val bitmap: MutableState<Uri?> = MediaViewModelObject.bitmap

        val thisMusicPlaying = remember("NowPlaying_thisMusicPlaying") {
            musicPlaying
        }

        // Closes the player, then opens a library page (its target is set by the caller)
        val openPage: (String) -> Unit = { route ->
            onClose()
            navController.toUI(route)
        }

        val lastClickTime = rememberSaveable(key = "NowPlaying_lastClickTime") {
            mutableLongStateOf(0L)
        }

        val showControl = rememberSaveable(key = "NowPlaying_showControl") {
            mutableStateOf(true)
        }

        val translation = rememberSaveable(key = "NowPlaying_translation") {
            mutableStateOf(SettingsLibrary.NowPlayingTranslation)
        }

        val shuffleModeEnabled = rememberSaveable(key = "NowPlaying_shuffleModeEnabled") {
            mutableStateOf(mediaControl?.shuffleModeEnabled ?: false)
        }
        val repeatMode = rememberSaveable(key = "NowPlaying_repeatMode") {
            mutableIntStateOf(mediaControl?.repeatMode ?: REPEAT_MODE_OFF)
        }

        /*val nowPage = rememberSaveable(key = "NowPlaying_nowPage") {
            MainViewModelObject.nowPage
        }*/

        println("重组：NowPlaying")

        // 触摸超时
        YosWrapper {
            LaunchedEffect(showControl.value, nowPageLambda(), lastClickTime.longValue) {
                if (nowPageLambda() != Lyric && !showControl.value) {
                    showControl.value = true
                }
                if (showControl.value) {
                    val time = 2500L
                    delay(time)
                    withContext(Dispatchers.Main) {
                        if (TimeUtils.getNowMills() - lastClickTime.longValue >= time && nowPageLambda() == Lyric && !wide) {
                            showControl.value = false
                        }
                    }
                }
            }
        }


        // 背景流光
        YosWrapper {
            /*BlendBackgroundView(
        bitmapLambda = { bitmap.value },
        isPlayingLambda = { isPlaying.value },
        nowPage = { nowPage.value }
    )*/
            println("重组：背景")

            YosFloatingLight(
                album = { bitmap.value },
                isPlaying = isPlayingStatusLambda,
                modifier = Modifier.fillMaxSize(),
                nowPage = { nowPageLambda() },
                showMiniPlayer = showMiniPlayer
            )
        }


        // 实际显示区
        YosWrapper {
            /*
        val controlAlpha = animateFloatAsState(
            targetValue = if (showControl.value) 1f else 0f,
            tween(200)
        )

        val buttonEnabled = remember("NowPlaying_buttonEnabled") {
            derivedStateOf { controlAlpha.value != 0f }
        }

        val translationButtonEnabled = remember("NowPlaying_translationButtonEnabled") {
            derivedStateOf { buttonEnabled.value && alpha.value != 0f }
        }*/

            val scope = rememberCoroutineScope()

            val alphaAnim = remember { Animatable(0f) }

            YosWrapper {
                LaunchedEffect(nowPageLambda()) {
                    val targetAlpha = if (nowPageLambda() == Lyric) 1f else 0f
                    scope.launch {
                        alphaAnim.animateTo(targetAlpha)
                    }
                }
            }

            val translationButtonEnabled = remember("NowPlaying_translationButtonEnabled") {
                derivedStateOf { showControl.value && alphaAnim.value != 0f }
            }
            val toggleTranslation = {
                Vibrator.click(context)
                translation.value = !translation.value
                showControl.value = true
                lastClickTime.longValue = TimeUtils.getNowMills()
                SettingsLibrary.NowPlayingTranslation = translation.value
            }

            println("重组：主功能区")

            if (wide) Box(
                Modifier
                    .fillMaxSize()
                    .then(sideModifier)
                    .padding(top = WideCardTop, bottom = 24.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
            )

            // 歌词
            YosWrapper {

                Box(
                    Modifier
                        .fillMaxSize()
                        .then(sideModifier)
                        // room for the tabs above, and kept inside the dark card
                        .then(
                            if (wide) Modifier
                                .padding(top = WideCardTop + 56.dp, bottom = 24.dp)
                                .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                            else Modifier
                        )
                        .graphicsLayer {
                            compositingStrategy =
                                CompositingStrategy.ModulateAlpha
                            this.alpha = alphaAnim.value
                        }
                ) {
                    println("重组：YosLyricView 外层 3")

                    Lyric(
                        wide = wide,
                        lrcEntries = { lrcEntries.value },
                        weightLambda = { showControl.value && !wide },
                        translationLambda = { translation.value },
                        onBackClick = {
                            showControl.value = true
                            lastClickTime.longValue =
                                TimeUtils.getNowMills()
                        },
                        mainViewModel = mainViewModel,
                        mediaViewModel = mediaViewModel
                    )
                }
            }

            // 这是小把手
            YosWrapper {
                Column(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 20.dp), contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .overlayEffect()
                                .size(
                                    width = 32.dp,
                                    height = 4.5.dp
                                )
                                .background(Color(0x4DFFFFFF), RoundedCornerShape(2.25.dp))
                                .clip(RoundedCornerShape(2.25.dp))
                        )
                    }
                }
            }

            // 主 View
            YosWrapper {
                SharedTransitionLayout {
                    Crossfade(
                        targetState = if (wide) Album else nowPageLambda(),
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(top = 22.dp)
                    ) {
                        //println("nowPage: ${nowPageLambda()}")
                        //println("nowPageIt: $it")
                        when (it) {
                            Album ->
                                Column(
                                    Modifier
                                        .then(paneModifier)
                                        .fillMaxSize()
                                        .clickable(enabled = false, onClick = {})
                                ) {
                                    YosWrapper {
                                        Column(Modifier.fillMaxHeight(if (wide) 0.64f else 0.595f)) {
                                            val isVisible = wide || nowPageLambda() == Album

                                            Album(
                                                modifier = Modifier.sharedElementWithCallerManagedVisibility(
                                                    sharedContentState = rememberSharedContentState(
                                                        key = ShareAlbumKey
                                                    ),
                                                    visible = isVisible
                                                ),
                                                albumUrl = { thisMusicPlaying.value?.thumb },
                                                isPlaying = isPlayingStatusLambda,
                                                wide = wide
                                            )
                                            AnimatedContent(
                                                targetState = thisMusicPlaying.value,
                                                transitionSpec = {
                                                    fadeIn() togetherWith fadeOut()
                                                }, modifier = Modifier.padding(horizontal = 32.dp)
                                            ) {
                                                Row(
                                                    Modifier
                                                        .fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .weight(1f)
                                                            .padding(end = 15.dp)
                                                    ) {
                                                        Text(
                                                            text = it?.title
                                                                ?: defaultTitle,/*
                                                        fontWeight = FontWeight.Bold,*/
                                                            fontSize = 19.5.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                            text = it?.artistsName
                                                                ?: defaultArtistsName,
                                                            fontSize = 18.5.sp,
                                                            modifier = Modifier.overlayEffect(),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            color = Color.White.copy(alpha = 0.35f)
                                                        )
                                                    }

                                                    YosWrapper {
                                                        ActionButtonsRow({ it }, openPage)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            Lyric ->
                                Column(Modifier.fillMaxSize()) {
                                    YosWrapper {
                                        val isVisible = nowPageLambda() == Lyric
                                        PlayingBar(
                                            modifier = Modifier.sharedElementWithCallerManagedVisibility(
                                                sharedContentState = rememberSharedContentState(
                                                    key = ShareAlbumKey
                                                ),
                                                visible = isVisible
                                            ),
                                            albumUrlLambda = {
                                                thisMusicPlaying.value?.thumb
                                            },
                                            musicPlayingLambda = { thisMusicPlaying.value },
                                            onOpenPage = openPage) {
                                            nowPageOnChanged(Album)
                                        }
                                    }
                                }

                            PlayingList ->
                                YosWrapper {
                                    Column(
                                        Modifier
                                            .fillMaxSize()
                                            .clickable(enabled = false, onClick = {})
                                    ) {
                                        val isVisible = nowPageLambda() == PlayingList
                                        PlayingBar(
                                            modifier = Modifier.sharedElementWithCallerManagedVisibility(
                                                sharedContentState = rememberSharedContentState(
                                                    key = ShareAlbumKey
                                                ),
                                                visible = isVisible
                                            ),
                                            albumUrlLambda = {
                                                thisMusicPlaying.value?.thumb
                                            },
                                            musicPlayingLambda = { thisMusicPlaying.value },
                                            onOpenPage = openPage) {
                                            nowPageOnChanged(Album)
                                        }
                                        YosWrapper {
                                            PlayingList(
                                                shuffleModeEnabledLambda = { shuffleModeEnabled.value },
                                                shuffleModeOnChanged = { shuffleModeSet ->
                                                    shuffleModeEnabled.value = shuffleModeSet
                                                },
                                                repeatModeLambda = { repeatMode.intValue },
                                                repeatModeOnChanged = { repeatModeSet ->
                                                    repeatMode.intValue = repeatModeSet
                                                },
                                                thisMusicPlayingLambda = { thisMusicPlaying.value }
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
            }

            // Wide mode: the queue sits beside the player column
            if (wide) {
                AnimatedVisibility(
                    visible = nowPageLambda() == PlayingList,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .then(sideModifier)
                            .padding(top = WideCardTop + 56.dp)
                    ) {
                        PlayingList(
                            shuffleModeEnabledLambda = { shuffleModeEnabled.value },
                            shuffleModeOnChanged = { shuffleModeEnabled.value = it },
                            repeatModeLambda = { repeatMode.intValue },
                            repeatModeOnChanged = { repeatMode.intValue = it },
                            thisMusicPlayingLambda = { thisMusicPlaying.value },
                            fullHeight = true
                        )
                    }
                }
                AnimatedVisibility(
                    visible = nowPageLambda() == NowPlayingPage.Info,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    SongInfoPane(
                        thisMusicPlaying.value,
                        Modifier
                            .fillMaxSize()
                            .then(sideModifier)
                            .padding(top = WideCardTop + 56.dp, bottom = 24.dp)
                    )
                }
            }

            // Wide mode: tabs above the side pane to switch between lyrics and the queue
            if (wide) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .then(sideModifier)
                        .padding(top = WideCardTop + 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val tabs = listOf(Lyric, PlayingList, NowPlayingPage.Info)
                    Segmented(
                        options = listOf(
                            stringResource(id = R.string.nowplaying_tab_lyrics),
                            stringResource(id = R.string.nowplaying_tab_up_next),
                            stringResource(id = R.string.nowplaying_tab_info)
                        ),
                        // Album never shows on tablets: count it as the queue
                        selected = tabs.indexOf(nowPageLambda()).let { if (it < 0) 1 else it },
                        onSelect = { nowPageOnChanged(tabs[it]) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 160.dp)
                    )
                    Row(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // translation: only for lyrics, its place is kept so the tabs don't resize
                        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            if (nowPageLambda() == Lyric) TranslationToggle(
                                translationButtonEnabled.value, translation, toggleTranslation
                            )
                        }
                        QueueModeButtons(
                            shuffleModeEnabledLambda = { shuffleModeEnabled.value },
                            shuffleModeOnChanged = { shuffleModeEnabled.value = it },
                            repeatModeLambda = { repeatMode.intValue },
                            repeatModeOnChanged = { repeatMode.intValue = it }
                        )
                    }
                }
            }

            // 音乐控制
            YosWrapper {
                Column(
                    Modifier
                        .then(paneModifier)
                        .fillMaxSize()
                        .statusBarsPadding(), verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        Modifier
                            /*.fillMaxHeight(0.385f)*/
                            .fillMaxHeight(if (wide) 0.4f else 0.437f)
                            .fillMaxWidth()
                    ) {
                        println("重组：控制区域外部")

                        YosWrapper {
                            if (showControl.value) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 40.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                //showControl.value = true
                                                /*lastClickTime.longValue =
                                                TimeUtils.getNowMills()*/
                                            })
                                )
                            }
                        }

                        YosWrapper {
                            Column(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                AnimatedVisibility(
                                    visible = showControl.value,
                                    enter = fadeIn() + expandVertically(
                                        expandFrom = Alignment.Top,
                                        initialHeight = { (it / 1.4).toInt() }),
                                    exit = fadeOut() + shrinkVertically(
                                        shrinkTowards = Alignment.Top,
                                        targetHeight = { (it / 1.4).toInt() })
                                ) {
                                    YosWrapper {
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 32.dp)
                                                .graphicsLayer {
                                                    compositingStrategy =
                                                        CompositingStrategy.ModulateAlpha
                                                    this.alpha = alphaAnim.value
                                                },
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            if (!wide) TranslationToggle(translationButtonEnabled.value, translation, toggleTranslation)
                                        }
                                    }

                                    PlayerControl(
                                        isPlayingLambda = isPlayingStatusLambda,
                                        isPlayingOnChanged = isPlayingOnChanged,
                                        onPrevious = {
                                            mediaControl?.seekToPreviousMediaItem()
                                            showControl.value = true
                                            lastClickTime.longValue = TimeUtils.getNowMills()
                                        },
                                        onStatus = { status ->
                                            if (status) {
                                                mediaControl?.fadePlay()
                                            } else {
                                                mediaControl?.fadePause()
                                            }
                                            showControl.value = true
                                            lastClickTime.longValue = TimeUtils.getNowMills()
                                        },
                                        onNext = {
                                            mediaControl?.seekToNextMediaItem()
                                            showControl.value = true
                                            lastClickTime.longValue = TimeUtils.getNowMills()
                                        },
                                        onSeek = { position ->
                                            mediaControl?.seekTo(position.toLong())
                                        },
                                        onLyrics = {
                                            if (nowPageLambda() == Lyric) {
                                                nowPageOnChanged(Album)
                                            } else {
                                                nowPageOnChanged(Lyric)
                                            }
                                        },
                                        onPlaylist = {
                                            if (nowPageLambda() == PlayingList) {
                                                nowPageOnChanged(Album)
                                            } else {
                                                nowPageOnChanged(PlayingList)
                                            }
                                        },
                                        nowPage = {
                                            nowPageLambda()
                                        },
                                        onSlider = {
                                            showControl.value = true
                                            lastClickTime.longValue = TimeUtils.getNowMills()
                                        },
                                        modifier = Modifier
                                            /*.graphicsLayer {
                                                compositingStrategy =
                                                    CompositingStrategy.Offscreen
                                                //this.alpha = controlAlpha.value
                                            }*/
                                            .padding(top = 52.dp),
                                        onWhile = {
                                            shuffleModeEnabled.value =
                                                mediaControl?.shuffleModeEnabled ?: false
                                            repeatMode.intValue =
                                                mediaControl?.repeatMode ?: REPEAT_MODE_OFF
                                        })
                                }
                            }
                        }
                    }
                }
            }

        }
    }

@Composable
private fun ColumnScope.Album(
    modifier: Modifier,
    albumUrl: () -> Uri?,
    isPlaying: () -> Boolean,
    wide: Boolean = false
) = Box(
    Modifier
        .weight(1f)
        .then(if (wide) Modifier.fillMaxWidth() else Modifier)
        .padding(top = 20.dp)
        // tablets: 25 + the cover's 7dp = the 32dp margin of the title and controls below
        .padding(horizontal = if (wide) 25.dp else 15.dp)
        .padding(bottom = if (wide) 0.dp else 33.dp),
    contentAlignment = if (wide) Alignment.TopCenter else Alignment.BottomCenter
) {
    val springSpec: AnimationSpec<Float> = remember("Album_springSpec") {
        SpringSpec(stiffness = 300f, dampingRatio = 1f, visibilityThreshold = 0.001f)
    }

    val tweenSpec: AnimationSpec<Float> = remember("Album_tweenSpec") {
        TweenSpec(durationMillis = 350, easing = EaseOutQuart)
    }

    val scale = animateFloatAsState(
        targetValue = if (isPlaying()) 0f else 1f,
        animationSpec = if (isPlaying()) springSpec else tweenSpec,
        visibilityThreshold = 0.001f
    )

    YosWrapper {
        // Phones shrink the cover on pause; tablets keep one size
        val dp = if (wide) 7.dp else (7 + (27 * scale.value)).dp
        // Tablets: a square as tall as the space allows, so the wider column doesn't push the cover off the top
        Box(
            if (wide) Modifier.aspectRatio(1f) else Modifier,
            contentAlignment = Alignment.BottomCenter
        ) {
        ShadowImageWithCache(
            dataLambda = albumUrl, contentDescription = null, modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                    // scaleX = scale.value
                    // scaleY = scale.value
                }
                .padding(start = dp, end = dp, bottom = dp)
                .then(modifier),
            imageQuality = ImageQuality.RAW,
            shadowOverlay = true
        )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayingList(
    shuffleModeEnabledLambda: () -> Boolean,
    shuffleModeOnChanged: (Boolean) -> Unit,
    repeatModeLambda: () -> Int,
    repeatModeOnChanged: (Int) -> Unit,
    thisMusicPlayingLambda: () -> YosMediaItem?,
    // the wide side pane has the whole height; on phones the controls sit below
    fullHeight: Boolean = false
) {
    val context = LocalContext.current

    Spacer(modifier = Modifier.height(12.dp))

    val musicList = remember("PlayingList_musicList") {
        playingMusicList
    }

    YosWrapper {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (fullHeight) 0.92f else 0.545f),
        ) {
            val hide = remember("PlayingList_hide") {
                derivedStateOf {
                    musicList.value.isNullOrEmpty() || shuffleModeEnabledLambda()
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .padding(top = 10.dp)
                    .height(if (fullHeight) 36.dp else 65.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                val favTitle = stringResource(id = R.string.page_library_playlists_fav_title)
                val songsTitle = stringResource(id = R.string.page_library_songs)
                val source = remember(musicList.value) { queueSource(musicList.value, favTitle, songsTitle) }
                // tablets: one line; the song count when the queue matches no playlist, album or library
                val total = stringResource(id = R.string.page_library_playlists_music_total, musicList.value?.size ?: 0)
                val playingFrom = stringResource(id = R.string.nowplaying_playing_from)
                if (fullHeight) Text(
                    text = buildAnnotatedString {
                        if (source == null) append(total) else {
                            append("$playingFrom ")
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Medium)) { append(source) }
                        }
                    },
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                ) else Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.page_library_playlists),
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier/*.padding(top = 10.dp)*/
                    )
                    Text(
                        text = stringResource(
                            id = R.string.page_library_playlists_music_total,
                            musicList.value?.size ?: 0
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .overlayEffect()
                            .alpha(0.35f)
                    )
                }

                if (!fullHeight) QueueModeButtons(
                    shuffleModeEnabledLambda, shuffleModeOnChanged, repeatModeLambda, repeatModeOnChanged
                )
            }


            if (hide.value) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_uitabbar_library),
                        contentDescription = null,
                        modifier = Modifier
                            .overlayEffect()
                            .size(70.dp)
                            .alpha(0.6f)
                    )
                    Text(
                        text = stringResource(id = R.string.playlist_unavailable_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 18.dp, bottom = 12.dp)
                    )
                    YosWrapper {
                        val msg = remember("PlayingList_msg") {
                            derivedStateOf {
                                if (musicList.value.isNullOrEmpty()) {
                                    R.string.playlist_unavailable_desc
                                } else {
                                    R.string.playlist_shuffle_desc
                                }
                            }
                        }
                        Text(
                            text = stringResource(id = msg.value),
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier
                                .overlayEffect()
                                .alpha(0.4f)
                        )
                    }
                }
            } else {
                val musicIndex = remember(musicList.value, thisMusicPlayingLambda()) {
                    musicList.value?.indexOf(musicPlaying.value) ?: 0
                }
                val scope = rememberCoroutineScope()
                val state = rememberLazyListState(
                    initialFirstVisibleItemIndex = musicIndex + 1,
                    initialFirstVisibleItemScrollOffset = -15
                )

                // drag to reorder: the order shown while a song is held, written back on release
                val dragged = remember { mutableStateOf<YosMediaItem?>(null) }
                val dragFrom = remember { mutableIntStateOf(-1) }
                val dragOffset = remember { mutableFloatStateOf(0f) }
                val dragOrder = remember { mutableStateOf<List<YosMediaItem>?>(null) }
                val itemPx = with(LocalDensity.current) { 64.dp.toPx() }
                // swap the held song with its neighbour once it is dragged past half a row
                val settle: (YosMediaItem) -> Unit = { music ->
                    dragOrder.value?.let { list ->
                        val i = list.indexOf(music)
                        val target = when {
                            dragOffset.floatValue > itemPx / 2 && i < list.lastIndex -> i + 1
                            dragOffset.floatValue < -itemPx / 2 && i > 0 -> i - 1
                            else -> i
                        }
                        if (target != i) {
                            dragOrder.value = list.toMutableList().apply { add(target, removeAt(i)) }
                            dragOffset.floatValue -= (target - i) * itemPx
                        }
                    }
                }
                // near the top or bottom edge the list scrolls by itself; the held song stays under the finger
                LaunchedEffect(dragged.value) {
                    val music = dragged.value ?: return@LaunchedEffect
                    while (true) {
                        val info = state.layoutInfo
                        val item = info.visibleItemsInfo.firstOrNull { it.key == music }
                        if (item != null) {
                            val top = item.offset + dragOffset.floatValue
                            val bottom = top + item.size
                            val speed = when {
                                top < info.viewportStartOffset + itemPx -> -(info.viewportStartOffset + itemPx - top) / 4
                                bottom > info.viewportEndOffset - itemPx -> (bottom - info.viewportEndOffset + itemPx) / 4
                                else -> 0f
                            }
                            if (speed != 0f) {
                                dragOffset.floatValue += state.scrollBy(speed)
                                settle(music)
                            }
                        }
                        delay(16)
                    }
                }
                val endDrag: (Boolean) -> Unit = { commit ->
                    val music = dragged.value
                    val to = music?.let { dragOrder.value?.indexOf(it) } ?: -1
                    if (commit && to >= 0) MediaController.move(dragFrom.intValue, to)
                    dragged.value = null
                    dragOrder.value = null
                    dragOffset.floatValue = 0f
                }

                YosWrapper {
                    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {

                        LazyColumn(state = state, modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                            .drawWithCache {
                                onDrawWithContent {
                                    val colors = listOf(
                                        Color.Transparent,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Transparent
                                    )

                                    drawContent()

                                    drawRect(
                                        brush = Brush.verticalGradient(colors),
                                        blendMode = BlendMode.DstIn
                                    )
                                }
                            }/*, contentPadding = PaddingValues(vertical = 12.dp)*/
                        ) {
                            item("blank_before") {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            items(
                                dragOrder.value ?: musicList.value ?: emptyList(),
                                key = { music -> music }/*,
                                contentType = { _ -> "NowPlaying_item" }*/
                            ) { music ->
                                val handle = Modifier.pointerInput(music) {
                                    detectDragGestures(
                                        onDragStart = {
                                            val list = musicList.value.orEmpty()
                                            dragged.value = music
                                            dragFrom.intValue = list.indexOf(music)
                                            dragOrder.value = list
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragOffset.floatValue += amount.y
                                            settle(music)
                                        },
                                        onDragEnd = { endDrag(true) },
                                        onDragCancel = { endDrag(false) }
                                    )
                                }
                                SmallMusicListItem(
                                    music,
                                    if (dragged.value == music) Modifier
                                        .zIndex(1f)
                                        .graphicsLayer { translationY = dragOffset.floatValue }
                                        .background(Color.White.copy(alpha = 0.08f))
                                    else Modifier.animateItem(),
                                    handle
                                ) {
                                    scope.launch(Dispatchers.IO) {
                                        MediaController.prepare(
                                            music,
                                            musicList.value ?: emptyList()
                                        )
                                    }
                                }
                            }
                            item("blank_after") {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.SmallMusicListItem(
    music: YosMediaItem,
    modifier: Modifier,
    handle: Modifier,
    itemClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(64.dp)
            .fillMaxWidth()
            .clickable {
                itemClick()
            }
            .padding(horizontal = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        println("重组：播放界面歌曲列表 ${music.title}")
        ShadowImageWithCache(
            dataLambda = { music.thumb },
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            cornerRadius = 4.dp,
            shadowAlpha = 0f,
            imageQuality = ImageQuality.LOW
        )

        Column(
            Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = music.title ?: defaultTitle,
                modifier = Modifier.padding(bottom = 1.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                lineHeight = 16.sp,
            )

            Text(
                text = music.artistsName ?: defaultArtistsName,
                modifier = Modifier.alpha(0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.5.sp,
                lineHeight = 11.5.sp,
            )
        }
        Icon(
            Icons.Rounded.DragHandle,
            contentDescription = "Reorder",
            modifier = handle
                .padding(start = 8.dp)
                .size(28.dp)
                .alpha(0.4f)
        )
    }
}

@Composable
private fun Lyric(
    wide: Boolean,
    lrcEntries: () -> List<List<Pair<Float, String>>>,
    weightLambda: () -> Boolean,
    translationLambda: () -> Boolean,
    mainViewModel: MainViewModel,
    mediaViewModel: MediaViewModel,
    onBackClick: () -> Unit
) = YosWrapper {

    val context = LocalContext.current

    println("重组：YosLyricView 外层 2")

    Column(
        Modifier
            .fillMaxSize()
    ) {
        YosWrapper {
            println("重组：YosLyricView 外层 1")

            // phones keep room for the title above; tablets have the tabs instead
            if (!wide) Spacer(modifier = Modifier.statusBarsHeight(110.dp))


            YosLyricView(
                //mediaViewModel = mediaViewModel,
                lrcEntriesLambda = lrcEntries,
                liveTimeLambda = {
                    (mediaControl?.currentPosition ?: 0).toInt()
                },
                mediaEvent = object : YosMediaEvent {
                    override fun onSeek(position: Int) {
                        mediaControl?.seekTo(position.toLong())
                    }
                },
                translationLambda = translationLambda,
                blurLambda = {
                    SettingsLibrary.LyricBlurEffect
                },
                uiConfig = YosUIConfig(
                    noLrcText = stringResource(id = R.string.tip_no_lyrics)
                ),
                weightLambda = weightLambda,
                modifier = Modifier.drawWithCache {
                    onDrawWithContent {
                        val overlayPaint = Paint().apply {
                            blendMode = BlendMode.Plus
                        }
                        val rect = Rect(0f, 0f, size.width, size.height)
                        val canvas = this.drawContext.canvas

                        canvas.saveLayer(rect, overlayPaint)

                        val colors = if (weightLambda()) {
                            listOf(
                                Color.Transparent,
                                Color(0x59000000),
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color(0x59000000),
                                Color(0x21000000),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                Color(0x59000000),
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                /*Color(0xD9000000),
                                Color(0xA6000000),
                                Color(0x73000000),
                                Color(0x59000000),
                                Color(0x3F000000),
                                Color(0x21000000),
                                Color(0x0C000000),*/
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black
                            )
                        }

                        drawContent()

                        drawRect(
                            brush = Brush.verticalGradient(colors),
                            blendMode = BlendMode.DstIn
                        )

                        canvas.restore()
                    }
                },
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(musicPlayingLambda: () -> YosMediaItem?, onOpenPage: (String) -> Unit) {
    val showMenu = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .overlayEffect(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dp = 28.dp

        val context = LocalContext.current

        Box(
            modifier = Modifier
                .clickable(
                    onClick = {
                        //println("收藏 开始")
                        val musicPlaying = musicPlayingLambda()
                        //println("收藏 $musicPlaying")
                        if (musicPlaying != null) {
                            Vibrator.click(context)
                            //println("收藏 切换状态
                            if (musicPlaying.let { FavPlayListLibrary.isFavorite(it) }) {
                                FavPlayListLibrary.removeMusic(musicPlaying)
                            } else {
                                FavPlayListLibrary.addMusic(musicPlaying)
                            }
                            //println("收藏 完毕")
                        }
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .size(dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = musicPlayingLambda()?.let { FavPlayListLibrary.isFavorite(it) }
                    ?: false,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }) {
                if (it) {
                    Icon(
                        painterResource(id = R.drawable.ic_nowplaying_favorited),
                        contentDescription = null,
                        modifier = Modifier
                            .size(dp)
                    )
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_nowplaying_favorite),
                        contentDescription = null,
                        modifier = Modifier
                            .overlayEffect()
                            .size(dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = 90f
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                }
                .clickable(
                    onClick = {
                        if (musicPlayingLambda() != null) {
                            Vibrator.click(context)
                            showMenu.value = true
                        }
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() })
                .size(dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = showMenu.value,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }) {
                if (it) {
                    Icon(
                        painterResource(id = R.drawable.ic_nowplaying_more_fill),
                        contentDescription = null,
                        modifier = Modifier
                            .size(dp)
                    )
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_nowplaying_more),
                        contentDescription = null,
                        modifier = Modifier
                            .overlayEffect()
                            .size(dp)
                    )
                }
            }
        }
    }

    val music = musicPlayingLambda()
    if (showMenu.value && music != null) {
        MoreMenu(music, onOpenPage) { showMenu.value = false }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenu(music: YosMediaItem, onOpenPage: (String) -> Unit, queueActions: Boolean = false, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val pickPlaylist = remember { mutableStateOf(false) }
    val showInfo = remember { mutableStateOf(false) }
    val pickTimer = remember { mutableStateOf(false) }
    if (pickTimer.value) {
        SleepTimerSheet(onDismiss)
        return
    }
    // Hide the sheet with its animation, then run the action
    val closeThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            action()
        }
    }
    OptionDialog(
        icon = {},
        title = music.title ?: "",
        header = {
            SheetHeader(
                title = music.title ?: defaultTitle,
                subTitle = listOfNotNull(music.artists, music.album?.takeIf { it.isNotBlank() }).joinToString(" · "),
                leading = {
                    AsyncImage(
                        model = music.thumb, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                }
            )
        },
        bottomSheetState = sheetState,
        content = {
            if (pickPlaylist.value) {
                PlaylistPicker { list -> closeThen { with(PlayListLibrary) { list.addMusic(music) } } }
            } else {
                MenuGroup {
                    if (queueActions && music.uri != MediaController.musicPlaying.value?.uri) {
                        MenuRow(stringResource(id = R.string.song_menu_play_next), Icons.AutoMirrored.Rounded.PlaylistPlay) {
                            closeThen { MediaController.enqueue(music, next = true) }
                        }
                        MenuRow(stringResource(id = R.string.song_menu_add_queue), Icons.AutoMirrored.Rounded.QueueMusic) {
                            closeThen { MediaController.enqueue(music, next = false) }
                        }
                    }
                    MenuRow(stringResource(id = R.string.nowplaying_more_add_playlist), Icons.AutoMirrored.Rounded.PlaylistAdd) {
                        pickPlaylist.value = true
                    }
                }
                MenuGroup {
                    val stopAt = SleepTimer.stopAt.longValue
                    // The timer is about playback, not this song: only in the player's menu
                    if (!queueActions) MenuRow(
                        when {
                            stopAt == SleepTimer.END_OF_SONG -> stringResource(id = R.string.nowplaying_more_sleep_timer_song)
                            stopAt == SleepTimer.END_OF_QUEUE -> stringResource(id = R.string.nowplaying_more_sleep_timer_queue)
                            stopAt > 0 -> stringResource(
                                id = R.string.nowplaying_more_sleep_timer_minutes,
                                ((stopAt - System.currentTimeMillis() + 59_999) / 60_000).toInt()
                            )
                            else -> stringResource(id = R.string.nowplaying_more_sleep_timer)
                        }, Icons.Rounded.Bedtime
                    ) { pickTimer.value = true }
                    val album = music.album
                    if (!album.isNullOrBlank()) {
                        MenuRow(stringResource(id = R.string.nowplaying_more_go_album), Icons.Rounded.Album) {
                            closeThen {
                                LibraryObject.setTargetAlbumName(album)
                                onOpenPage(UI.AlbumInfo)
                            }
                        }
                    }
                    music.artistsList?.forEach { artist ->
                        MenuRow(stringResource(id = R.string.nowplaying_more_go_artist, artist), Icons.Rounded.Person) {
                            closeThen {
                                LibraryObject.setTargetListWithTitle(artist, MusicLibrary.Artist[artist])
                                onOpenPage(UI.NormalMusic)
                            }
                        }
                    }
                    val uri = music.uri
                    // Library uris are file://, which Android refuses to hand to other apps: share the MediaStore uri instead
                    val shareUri = music.mediaId?.toLongOrNull()
                        ?.let { ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it) }
                        ?: uri?.takeIf { it.scheme == "content" }
                    if (shareUri != null) {
                        MenuRow(stringResource(id = R.string.nowplaying_more_share), Icons.Rounded.Share) {
                            closeThen {
                                val send = Intent(Intent.ACTION_SEND)
                                    .setType(music.mimeType ?: "audio/*")
                                    .putExtra(Intent.EXTRA_STREAM, shareUri)
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                context.startActivity(Intent.createChooser(send, music.title))
                            }
                        }
                    }
                    val format = music.mimeType?.substringAfter('/')?.removePrefix("x-")
                    MenuRow(
                        stringResource(id = R.string.nowplaying_more_file_info), Icons.Outlined.Info,
                        trailing = when (format) {
                            null -> null
                            "flac", "alac", "wav" -> stringResource(id = R.string.song_menu_lossless)
                            "mpeg" -> "MP3"
                            else -> format.uppercase()
                        }
                    ) { showInfo.value = !showInfo.value }
                }
                if (showInfo.value) {
                    DialogContent(
                        text = listOfNotNull(
                            music.mimeType,
                            formatTime(music.duration / 1000),
                            music.genre,
                            (music.releaseYear ?: music.recordingYear)?.toString(),
                            music.uri?.let { Uri.decode(it.toString()) }
                        ).joinToString("\n"),
                        modifier = Modifier.padding(horizontal = 6.5.dp, vertical = 4.dp)
                    )
                }
            }
        },
        positiveContent = stringResource(id = R.string.nowplaying_more_close),
        onPositive = { closeThen {} },
        onDismissRequest = onDismiss
    )
}

// Opened by the moon button under the player and by the "…" menu: pick a length, then start it
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val closeThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            action()
        }
    }
    val stopAt = SleepTimer.stopAt.longValue
    // Choices: minutes, -1 end of song, -2 end of queue, 0 none
    val running = when {
        stopAt == SleepTimer.END_OF_SONG -> -1
        stopAt == SleepTimer.END_OF_QUEUE -> -2
        stopAt > 0 -> SleepTimer.minutes.intValue
        else -> 0
    }
    val choice = remember { mutableIntStateOf(running) }
    val options = listOf(15, 30, 45, 60).map { it to stringResource(id = R.string.sleep_timer_minutes, it) } +
            listOf(
                -1 to stringResource(id = R.string.sleep_timer_end_of_song),
                -2 to stringResource(id = R.string.sleep_timer_end_of_queue)
            )
    val picked = options.firstOrNull { it.first == choice.intValue }
    // Picking what already runs changes nothing, so the button only closes
    val starts = picked != null && choice.intValue != running
    OptionDialog(
        icon = {},
        title = stringResource(id = R.string.nowplaying_more_sleep_timer),
        header = {
            SheetHeader(
                title = stringResource(id = R.string.nowplaying_more_sleep_timer),
                subTitle = stringResource(id = R.string.sleep_timer_subtitle),
                centered = true,
                leading = {
                    Icon(
                        painterResource(id = R.drawable.ic_nowplaying_sleepon), null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            )
        },
        bottomSheetState = sheetState,
        content = {
            MenuGroup {
                options.forEach { (key, label) ->
                    MenuRow(label, selected = choice.intValue == key) { choice.intValue = key }
                }
            }
            if (stopAt != 0L) MenuGroup {
                MenuRow(stringResource(id = R.string.sleep_timer_off)) { closeThen { SleepTimer.cancel() } }
            }
            MenuGroup {
                Column(Modifier.background(tileColor())) {
                    SwitchItem(
                        title = stringResource(id = R.string.sleep_timer_fade),
                        desc = stringResource(id = R.string.sleep_timer_fade_desc),
                        onClick = { SleepTimer.setFadeOut(!SleepTimer.fadeOut.value) },
                        checkedLambda = { SleepTimer.fadeOut.value }
                    )
                }
            }
        },
        positiveContent = if (starts) stringResource(id = R.string.sleep_timer_start, picked!!.second)
        else stringResource(id = R.string.nowplaying_more_close),
        onPositive = {
            closeThen {
                if (starts) when (val c = choice.intValue) {
                    -1 -> SleepTimer.startEndOfSong()
                    -2 -> SleepTimer.startEndOfQueue()
                    else -> SleepTimer.start(c)
                }
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
private fun PlaylistPicker(onPick: (PlayList) -> Unit) {
    MenuGroup {
        PlayListLibrary.playList.forEach { list ->
            MenuRow(list.name, Icons.AutoMirrored.Rounded.QueueMusic) { onPick(list) }
        }
    }
    val newName = remember { mutableStateOf("") }
    SearchTextField(
        text = newName.value,
        placeholder = stringResource(id = R.string.nowplaying_more_new_playlist),
        onValueChange = { newName.value = it },
        onSearch = {
            val name = newName.value.trim()
            if (name.isNotEmpty()) {
                PlayListLibrary.create(name)
                PlayListLibrary.playList.firstOrNull { it.name == name }?.let(onPick)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}

@Composable
private fun PlayingBar(
    modifier: Modifier,
    albumUrlLambda: () -> Uri?,
    musicPlayingLambda: () -> YosMediaItem?,
    onOpenPage: (String) -> Unit,
    onAlbumClick: () -> Unit
) = YosWrapper {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.5.dp)
            .padding(top = 22.dp)
            .height(70.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        ShadowImageWithCache(
            dataLambda = albumUrlLambda, contentDescription = null, modifier = modifier
                .size(69.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        onAlbumClick()
                    }), cornerRadius = 5.dp,
            imageQuality = ImageQuality.LOW,
            shadowType = ShadowType.Small,
            shadowOverlay = true
        )
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 12.dp, end = 15.dp)
        ) {
            Text(
                text = musicPlayingLambda()?.title ?: defaultTitle,/*
                fontWeight = FontWeight.Bold,*/
                fontSize = 16.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.5.sp
            )
            Text(
                text = musicPlayingLambda()?.artistsName
                    ?: defaultArtistsName,
                fontSize = 15.sp,
                modifier = Modifier.overlayEffect(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White.copy(alpha = 0.35f)
            )
        }

        YosWrapper {
            ActionButtonsRow(musicPlayingLambda, onOpenPage)
        }
    }

}

@Composable
fun RowScope.AirPlay() {
    val contextCompose = LocalContext.current
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val connectedDevices =
        remember("AirPlay_connectedDevices") { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    val audioDeviceName = remember("AirPlay_audioDeviceName") { mutableStateOf("") }
    val showName = remember("AirPlay_showName") { mutableStateOf(false) }

    YosWrapper {
        DisposableEffect(Unit) {
            val filter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED).apply {
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction("yos.music.player.BLUETOOTH_STATUS_REFRESH")
            }
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val action = intent?.action
                    if (action == BluetoothDevice.ACTION_ACL_CONNECTED || action == BluetoothDevice.ACTION_ACL_DISCONNECTED || action == "yos.music.player.BLUETOOTH_STATUS_REFRESH") {
                        if (ActivityCompat.checkSelfPermission(
                                contextCompose,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        connectedDevices.value =
                            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()

                        val thisName =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                connectedDevices.value.firstOrNull { it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO && it.isConnected() }?.alias
                            } else {
                                connectedDevices.value.firstOrNull { it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO && it.isConnected() }?.name
                            }
                        showName.value = thisName != null
                        if (thisName != null) {
                            audioDeviceName.value = thisName.trim()
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                contextCompose.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                contextCompose.registerReceiver(receiver, filter)
            }

            if (ActivityCompat.checkSelfPermission(
                    contextCompose,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                connectedDevices.value = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
                val thisName =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        connectedDevices.value.firstOrNull { it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO && it.isConnected() }?.alias
                    } else {
                        connectedDevices.value.firstOrNull { it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO && it.isConnected() }?.name
                    }
                showName.value = thisName != null
                if (thisName != null) {
                    audioDeviceName.value = thisName.trim()
                }
            }

            onDispose {
                runCatching {
                    contextCompose.unregisterReceiver(receiver)
                }
            }
        }
    }

    YosWrapper {
        val context = LocalContext.current

        val systemMediaControlResolver = SystemMediaControlResolver(context)

        Column(
            modifier = Modifier
                .heightIn(min = 53.dp)
                .navigationBarsHeight(48.dp)
                .weight(1f)
                .clickable(
                    onClick = {
                        systemMediaControlResolver.intentSystemMediaDialog()
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.height(36.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(targetState = showName.value, transitionSpec = {
                    (scaleIn(initialScale = 0.3f) + fadeIn()).togetherWith(
                        scaleOut(
                            targetScale = 0.3f
                        ) + fadeOut()
                    )
                }, contentAlignment = Alignment.Center) {
                    if (it) {
                        Icon(
                            painterResource(id = R.drawable.ic_earphone),
                            contentDescription = null,
                            modifier = Modifier
                                .size(27.dp)
                        )
                    } else {
                        Icon(
                            painterResource(id = R.drawable.ic_nowplaying_airplay),
                            contentDescription = null,
                            modifier = Modifier
                                .size(21.5.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(showName.value, enter = scaleIn(initialScale = 0.3f) + fadeIn(), exit = scaleOut(
                targetScale = 0.3f
            ) + fadeOut()) {
                Text(
                    text = audioDeviceName.value,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun BluetoothDevice.isConnected(): Boolean {
    return runCatching {
        val isConnectedMethod =
            BluetoothDevice::class.java.getMethod("isConnected")
        isConnectedMethod.isAccessible = true
        isConnectedMethod.invoke(this) as Boolean
    }.getOrDefault(false)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerControl(
    isPlayingLambda: () -> Boolean,
    isPlayingOnChanged: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onStatus: (Boolean) -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onLyrics: () -> Unit,
    onPlaylist: () -> Unit,
    nowPage: () -> String,
    onSlider: () -> Unit,
    onWhile: suspend () -> Unit,
    modifier: Modifier
) {
    val playingDuration = rememberSaveable(key = "PlayerControl_playingDuration") {
        mutableLongStateOf(0L)
    }
    val playingPosition = rememberSaveable(key = "PlayerControl_playingPosition") {
        mutableLongStateOf(0L)
    }
    val context = LocalContext.current
    val playedTime = rememberSaveable(key = "PlayerControl_playedTime") { mutableStateOf("0:00") }
    val remainingTime =
        rememberSaveable(key = "PlayerControl_remainingTime") { mutableStateOf("-0:00") }
    val sliderPosition = remember("PlayerControl_sliderPosition") { mutableFloatStateOf(0f) }
    val isSliding = remember("PlayerControl_isSliding") {
        mutableStateOf(false)
    }

    YosWrapper {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
                .padding(bottom = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            println("重组：控制区域内部")

            YosWrapper {
                // 启动作用
                YosWrapper {
                    val lifecycleState =
                        LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()

                    LaunchedEffect(Unit) {
                        var lastPosition = 0L
                        while (true) {
                            //isPlaying.value = /*mediaControl?.isPlaying ?: false*/ FadeExo.targetStatus != 0
                            if (lifecycleState.value.isAtLeast(Lifecycle.State.RESUMED)) {
                                playingDuration.longValue = mediaControl?.duration ?: 0
                                playingPosition.longValue = mediaControl?.currentPosition ?: 0

                                if (!isSliding.value && playingDuration.longValue > 0L) {
                                    val totalSeconds =
                                        playingPosition.longValue.coerceAtLeast(0) / 1000
                                    if (totalSeconds != lastPosition) {
                                        playedTime.value = formatTime(totalSeconds)

                                        sliderPosition.floatValue =
                                            playingPosition.longValue.coerceAtLeast(0).toFloat()

                                        val remainingSeconds =
                                            playingDuration.longValue.coerceAtLeast(0) / 1000 - totalSeconds
                                        remainingTime.value = "-${formatTime(remainingSeconds)}"
                                        lastPosition = totalSeconds
                                    }
                                }

                                onWhile()
                            }

                            delay(700)
                        }
                    }
                }

                // 进度条
                YosWrapper {
                    //println("重组：控制区域内部 - 进度条")
                    Slider(
                        value = sliderPosition.floatValue,
                        onValueChange = { newValue ->
                            isSliding.value = true

                            sliderPosition.floatValue = newValue
                            val newTotalSeconds = newValue.toLong() / 1000
                            playedTime.value = formatTime(newTotalSeconds)

                            val newRemainingSeconds =
                                playingDuration.longValue / 1000 - newTotalSeconds
                            remainingTime.value = "-${formatTime(newRemainingSeconds)}"

                            onSlider()
                        },
                        onValueChangeFinished = {
                            Vibrator.longClick(context)
                            onSeek(sliderPosition.floatValue)
                            isSliding.value = false
                        },
                        valueRange = 0f..playingDuration.longValue.toFloat().coerceAtLeast(0f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color(0x0DFFFFFF)
                        ),
                        modifier = Modifier
                            .overlayEffect()
                            .alpha(0.45f)
                            .height(14.dp),
                        thumb = {
                        },
                        track = {
                            Track(
                                sliderPositions = SliderPositions(
                                    initialActiveRange = 0f..(sliderPosition.floatValue / playingDuration.longValue)
                                ), height = 7.dp
                            )
                        }
                    )
                }

                // 控制按钮&进度文本
                YosWrapper {
                    //println("重组：控制区域内部 - 控制按钮&进度文本")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 7.dp)
                            .height(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = playedTime.value,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.3.sp,
                                color = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.overlayEffect()
                            )
                            Text(
                                text = remainingTime.value,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.3.sp,
                                color = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.overlayEffect()
                            )
                        }

                        MusicQualityIndicator()
                    }


                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(61.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = false),
                                        onClick = {
                                            Vibrator.click(context)
                                            onPrevious()
                                        }),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_nowplaying_rewind),
                                    contentDescription = "Previous",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(43.dp))

                            Box(
                                modifier = Modifier
                                    .size(58.5.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = false),
                                        onClick = {
                                            Vibrator.click(context)
                                            isPlayingOnChanged(!isPlayingLambda())
                                            onStatus(isPlayingLambda())
                                        }),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(targetState = isPlayingLambda(), transitionSpec = {
                                    (scaleIn(initialScale = 0.3f) + fadeIn()).togetherWith(
                                        scaleOut(
                                            targetScale = 0.3f
                                        ) + fadeOut()
                                    )
                                }) {
                                    if (it) {
                                        Icon(
                                            painterResource(id = R.drawable.ic_nowplaying_pause),
                                            contentDescription = "Pause",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(10.dp)
                                        )
                                    } else {
                                        Icon(
                                            painterResource(id = R.drawable.ic_nowplaying_play),
                                            contentDescription = "Play",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(9.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(43.dp))
                            Box(
                                modifier = Modifier
                                    .size(61.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = false),
                                        onClick = {
                                            Vibrator.click(context)
                                            onNext()
                                        }),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_nowplaying_fforward),
                                    contentDescription = "Next",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 音量调节
            YosWrapper {
                if (SettingsLibrary.NowPlayingShowVolumeBar) {
                    VolumeSlider(context = context, onSlider)
                }
            }

            // 底部 歌词&播放列表
            YosWrapper {
                //println("重组：控制区域内部 - 底部栏")
                Row(
                    modifier = Modifier
                        .overlayEffect()
                        .fillMaxWidth()
                        .alpha(0.4f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val dp = 32.dp
                    val config = LocalConfiguration.current
                    val wide = config.screenWidthDp > config.screenHeightDp && config.screenWidthDp >= 600
                    if (!wide) Box(
                        modifier = Modifier
                            .height(36.dp)
                            .weight(1f)
                            .clickable(
                                onClick = { onLyrics() },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = nowPage() == Lyric,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }) {
                            if (it) {
                                Icon(
                                    painterResource(id = R.drawable.ic_nowplaying_lyricson),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(dp)
                                )
                            } else {
                                Icon(
                                    painterResource(id = R.drawable.ic_nowplaying_lyrics),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(dp)
                                )
                            }
                        }
                    }

                    if (!wide) Spacer(modifier = Modifier.weight(0.1f))

                    AirPlay()

                    Spacer(modifier = Modifier.weight(0.1f))

                    val showTimer = remember { mutableStateOf(false) }
                    val timerOn = SleepTimer.stopAt.longValue != 0L
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .weight(1f)
                            .clickable(
                                onClick = { showTimer.value = true },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(id = if (timerOn) R.drawable.ic_nowplaying_sleepon else R.drawable.ic_nowplaying_sleep),
                            contentDescription = stringResource(id = R.string.nowplaying_more_sleep_timer),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    if (showTimer.value) SleepTimerSheet { showTimer.value = false }

                    if (!wide) Spacer(modifier = Modifier.weight(0.1f))

                    if (!wide) Box(
                        modifier = Modifier
                            .height(36.dp)
                            .weight(1f)
                            .clickable(
                                onClick = { onPlaylist() },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = nowPage() == PlayingList,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }) {
                            if (it) {
                                Icon(
                                    painterResource(id = R.drawable.ic_nowplaying_queueon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(dp)
                                )
                            } else {
                                Icon(
                                    painterResource(id = R.drawable.ic_nowplaying_queue),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(dp)
                                )
                            }
                        }
                    }
                }
            }

            // 边距填充
            /*YosWrapper {
                Spacer(modifier = Modifier.navigationBarsHeight(5.dp))
            }*/
            // 为显示设备名称，迁移到 AirPlay 底部处理
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolumeSlider(context: Context, onSlider: () -> Unit) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val sliderPosition =
        remember("VolumeSlider_sliderPosition") { mutableFloatStateOf(currentVolume / maxVolume.toFloat()) }
    val sliding = remember("VolumeSlider_sliding") {
        mutableStateOf(false)
    }

    val volumeChangeReceiver = remember("VolumeSlider_volumeChangeReceiver") {
        VolumeChangeReceiver { newVolume ->
            sliderPosition.floatValue = newVolume / maxVolume.toFloat()
        }
    }
    val intentFilter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")

    DisposableEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                volumeChangeReceiver,
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(volumeChangeReceiver, intentFilter)
        }

        onDispose {
            context.unregisterReceiver(volumeChangeReceiver)
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = 1.5.dp)
            .padding(horizontal = 8.dp)
            .padding(top = 4.dp, bottom = 2.5.dp)
            .overlayEffect()
            .alpha(0.45f)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_nowplaying_volume),
            contentDescription = "Mute",
            modifier = Modifier.size(20.dp)
        )

        YosWrapper {
            val animatedProgress = if (sliding.value) {
                sliderPosition
            } else {
                animateFloatAsState(
                    targetValue = sliderPosition.floatValue,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    visibilityThreshold = 0.0001f
                )
            }

            Slider(
                value = (animatedProgress.value * maxVolume),
                onValueChange = { newValue ->
                    sliding.value = true
                    sliderPosition.floatValue = newValue / maxVolume
                    val volume = newValue.toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
                    onSlider()
                },
                valueRange = 0f..maxVolume.toFloat(),
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color(0x0DFFFFFF)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 1.5.dp, end = 5.dp),
                thumb = {
                },
                track = {
                    Track(
                        sliderPositions = SliderPositions(
                            initialActiveRange = 0f..animatedProgress.value
                        ), height = 7.dp
                    )
                },
                onValueChangeFinished = {
                    Vibrator.longClick(context)
                    sliding.value = false
                }
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_nowplaying_volume_full),
            contentDescription = "Max Volume",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun Track(
    sliderPositions: SliderPositions,
    modifier: Modifier = Modifier,
    height: Dp
) = YosWrapper {
    val inactiveTrackColor = Color.White.copy(alpha = 0.5f)
    val activeTrackColor = Color.White
    val inactiveTickColor = Color.White.copy(alpha = 0.5f)
    val activeTickColor = Color.White
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
        val tickSize = 2.0.dp.toPx()
        val trackStrokeWidth = height.toPx()
        drawLine(
            inactiveTrackColor,
            sliderStart,
            sliderEnd,
            trackStrokeWidth,
            StrokeCap.Round
        )
        val sliderValueEnd = Offset(
            sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.endInclusive,
            center.y
        )

        val sliderValueStart = Offset(
            sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.start,
            center.y
        )

        drawLine(
            activeTrackColor,
            sliderValueStart,
            sliderValueEnd,
            trackStrokeWidth,
            StrokeCap.Round
        )
        sliderPositions.tickFractions.groupBy {
            it > sliderPositions.activeRange.endInclusive ||
                    it < sliderPositions.activeRange.start
        }.forEach { (outsideFraction, list) ->
            drawPoints(
                list.fastMap {
                    Offset(lerp(sliderStart, sliderEnd, it).x, center.y)
                },
                PointMode.Points,
                (if (outsideFraction) inactiveTickColor else activeTickColor),
                tickSize,
                StrokeCap.Round
            )
        }
    }
}

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "$minutes:${if (secs < 10) "0$secs" else "$secs"}"
}
// Tablet side pane: details of the playing song
@Composable
private fun SongInfoPane(music: YosMediaItem?, modifier: Modifier) {
    if (music == null) return
    val rows = listOfNotNull(
        R.string.nowplaying_info_title to music.title,
        R.string.nowplaying_info_artist to music.artistsName,
        R.string.nowplaying_info_album to music.album,
        R.string.nowplaying_info_genre to music.genre,
        R.string.nowplaying_info_year to (music.releaseYear ?: music.recordingYear)?.toString(),
        R.string.nowplaying_info_track to music.trackNumber?.takeIf { it > 0 }?.toString(),
        R.string.nowplaying_info_duration to formatTime(music.duration / 1000),
        R.string.nowplaying_info_format to music.mimeType,
        R.string.nowplaying_info_file to music.uri?.let { Uri.decode(it.toString()) }
    ).filter { !it.second.isNullOrBlank() }
    Column(
        modifier
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        rows.forEach { (label, value) ->
            Column(Modifier.padding(vertical = 9.dp)) {
                Text(stringResource(id = label), fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f))
                Text(value!!, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// Shuffle and repeat toggles: in the queue header on phones, beside the tabs on tablets
@Composable
internal fun QueueModeButtons(
    shuffleModeEnabledLambda: () -> Boolean,
    shuffleModeOnChanged: (Boolean) -> Unit,
    repeatModeLambda: () -> Int,
    repeatModeOnChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .overlayEffect()
            .alpha(0.6f)
    ) {
        val dp = 36.dp
        YosWrapper {
            val shuffleBackgroundAlpha =
                animateFloatAsState(targetValue = if (shuffleModeEnabledLambda()) 0.9f else 0f)
            Box(
                modifier = Modifier
                    .clickable(
                        onClick = {
                            Vibrator.click(context)
                            mediaControl?.shuffleModeEnabled =
                                !shuffleModeEnabledLambda()
                            mediaControl?.let { YosPlaybackService().setCustomButtons(it) }
                            shuffleModeOnChanged(!shuffleModeEnabledLambda())
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
                    .size(36.dp)
                    .background(
                        Color.White.copy(alpha = shuffleBackgroundAlpha.value),
                        shape = YosRoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                YosWrapper {
                    val shuffleIconTint =
                        animateColorAsState(targetValue = if (shuffleModeEnabledLambda()) Color.Black else Color.White)
                    Icon(
                        painterResource(id = R.drawable.ic_nowplaying_shuffle),
                        contentDescription = null,
                        modifier = Modifier
                            .size(dp),
                        tint = shuffleIconTint.value
                    )
                }
            }
        }
        YosWrapper {
            val repeatHighlight =
                repeatModeLambda() == REPEAT_MODE_ALL || repeatModeLambda() == REPEAT_MODE_ONE
            val repeatBackgroundAlpha =
                animateFloatAsState(targetValue = if (repeatHighlight) 0.9f else 0f)
            Box(
                modifier = Modifier
                    .clickable(
                        onClick = {
                            Vibrator.click(context)
                            val targetMode = when (repeatModeLambda()) {
                                REPEAT_MODE_OFF -> {
                                    REPEAT_MODE_ALL
                                }

                                REPEAT_MODE_ALL -> {
                                    REPEAT_MODE_ONE
                                }

                                else -> {
                                    REPEAT_MODE_OFF
                                }
                            }
                            mediaControl?.repeatMode = targetMode
                            mediaControl?.let { YosPlaybackService().setCustomButtons(it) }
                            repeatModeOnChanged(targetMode)
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
                    .padding(start = 10.dp)
                    .size(36.dp)
                    .background(
                        Color.White.copy(alpha = repeatBackgroundAlpha.value),
                        shape = YosRoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                YosWrapper {
                    AnimatedContent(targetState = repeatModeLambda(), transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }) {
                        when (it) {
                            REPEAT_MODE_ONE -> Icon(
                                painterResource(id = R.drawable.ic_nowplaying_repeatone),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(dp),
                                tint = animateColorAsState(targetValue = if (repeatHighlight) Color.Black else Color.White).value
                            )

                            else -> Icon(
                                painterResource(id = R.drawable.ic_nowplaying_repeat),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(dp),
                                tint = animateColorAsState(targetValue = if (repeatHighlight) Color.Black else Color.White).value
                            )
                        }
                    }
                }
            }
        }
    }
}

// Where the queue comes from: a playlist, the favourites, one album or the whole library
private fun queueSource(list: List<YosMediaItem>?, favTitle: String, songsTitle: String): String? {
    if (list.isNullOrEmpty()) return null
    val uris = list.map { it.uri }.toSet()
    PlayListLibrary.playList.firstOrNull { it.songDataList.toSet() == uris }?.let { return it.name }
    if (FavPlayListLibrary.favPlayList.map { it.uri }.toSet() == uris) return favTitle
    list.map { it.album }.distinct().singleOrNull()?.let { return it }
    if (list.size == MusicLibrary.songs.size) return songsTitle
    return null
}
