package yos.music.player.ui.pages.library.albums

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import yos.music.player.data.libraries.ArtistArt
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cormor.overscroll.core.overScrollVertical
import com.cormor.overscroll.core.rememberOverscrollFlingBehavior
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsHeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import yos.music.player.R
import yos.music.player.code.MediaController
import yos.music.player.data.libraries.MusicLibrary
import yos.music.player.data.libraries.YosMediaItem
import yos.music.player.data.libraries.artistsList
import yos.music.player.data.libraries.artistsName
import yos.music.player.data.libraries.defaultArtists
import yos.music.player.data.libraries.defaultArtistsName
import yos.music.player.data.libraries.defaultTitle
import yos.music.player.data.objects.LibraryObject
import yos.music.player.ui.theme.withNight
import yos.music.player.ui.pages.isWideLayout
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import yos.music.player.ui.widgets.basic.ImageQuality
import yos.music.player.ui.widgets.basic.ShadowImage
import yos.music.player.ui.widgets.basic.Title
import yos.music.player.ui.widgets.basic.YosWrapper
import yos.music.player.ui.widgets.effects.ShadowType

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AlbumInfo(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) =
    Box(
        Modifier
            .fillMaxSize()
        /*.statusBarsPadding()*/
    ) {
        val albumName = rememberSaveable(key = "AlbumInfo_albumName") {
            mutableStateOf(LibraryObject.getTargetAlbumName())
        }

        val hideMusic = remember("AlbumInfo_showMusic") {
            derivedStateOf {
                albumName.value.isEmpty()
            }
        }
        if (hideMusic.value) {
            val message = stringResource(id = R.string.tip_no_album_info)
            Title(
                title = stringResource(id = R.string.page_library_album_info_title), onBack = {
                    navController.popBackStack()
                }
            ) {
                item("tip_no_song") {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                    ) {
                        Text(text = message, fontSize = 18.sp, modifier = Modifier.alpha(0.6f))
                    }
                }
            }
        } else {
            val songs = MusicLibrary.Album[albumName.value].sortedWith(compareBy<YosMediaItem>{ it.trackNumber?:0 }.thenBy { it.title })

            /*Box(Modifier.height(56.dp), contentAlignment = Alignment.CenterStart) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.popBackStack()
                        },
                    tint = MaterialTheme.colorScheme.primary
                )
            }*/

            val state = rememberLazyListState()

            val mainArtists = rememberSaveable(key = "AlbumInfo_mainArtists") {
                mutableStateOf(songs.first().artistsList ?: defaultArtists)
            }
            val mainArtistsName = rememberSaveable(key = "AlbumInfo_mainArtistsName") {
                mutableStateOf(songs.first().artistsName ?: defaultArtistsName)
            }

            val (songCount, totalMinutes) = rememberSaveable(songs) {
                val totalDuration = songs.sumOf { it.duration }
                val totalMinutes = totalDuration / 60000
                val songCount = songs.size
                songCount to totalMinutes
            }

            val scope = rememberCoroutineScope()

            val header: @Composable () -> Unit = {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 9.5.dp)
                            .padding(horizontal = 18.dp)
                            .statusBarsPadding(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        /*with(sharedTransitionScope) {*/
                        ShadowImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 54.5.dp)
                            /*.sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "image-$albumName"),
                                animatedVisibilityScope = animatedContentScope
                            )*/,
                            dataLambda = { songs.getOrNull(0)?.thumb },
                            contentDescription = null,
                            cornerRadius = 7.dp,
                            imageQuality = ImageQuality.RAW,
                            shadowType = ShadowType.Medium
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = albumName.value,
                            fontSize = 20.sp,
                            /*modifier = Modifier.sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "album_name-$albumName"),
                                animatedVisibilityScope = animatedContentScope
                            ),*/
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.Medium
                        )
                        /*}*/

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = mainArtistsName.value,
                            fontSize = 17.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 23.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = stringResource(R.string.album_info_label),
                            fontSize = 11.5.sp,
                            modifier = Modifier
                                .alpha(0.4f)
                                .padding(top = 2.dp)
                        )

                        YosWrapper {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 15.dp)
                            ) {

                                NormalButton(
                                    icon = painterResource(id = R.drawable.button_icon_play),
                                    label = stringResource(id = R.string.normal_button_play),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    scope.launch(Dispatchers.IO) {
                                        MediaController.prepare(
                                            songs.first(),
                                            songs
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(15.dp))
                                NormalButton(
                                    icon = painterResource(id = R.drawable.button_icon_shuffle),
                                    label = stringResource(id = R.string.normal_button_shuffle),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    MediaController.mediaControl?.shuffleModeEnabled = true
                                    scope.launch(Dispatchers.IO) {
                                        MediaController.prepare(
                                            songs.random(),
                                            songs
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
            val wide = isWideLayout()
            val playing = MediaController.musicPlaying.value
            // tablets: the cover's glow behind the top of the page, as in the player
            if (wide) Box(Modifier.fillMaxWidth().height(440.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(songs.first().thumb).size(256).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(60.dp).alpha(0.5f)
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))
                    )
                )
            }
            Row(Modifier.fillMaxSize()) {
            // Tablets: cover and buttons stay on the left while the tracks scroll on the right
            if (wide) Column(
                Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(top = 64.dp, bottom = 134.dp)
                    .padding(horizontal = 28.dp)
            ) {
                WideHeader(albumName.value, songs, mainArtists.value.firstOrNull(), mainArtistsName.value,
                    stringResource(R.string.page_library_album_info_others, songCount, totalMinutes),
                    onPlay = { scope.launch(Dispatchers.IO) { MediaController.prepare(songs.first(), songs) } },
                    onShuffle = {
                        MediaController.mediaControl?.shuffleModeEnabled = true
                        scope.launch(Dispatchers.IO) { MediaController.prepare(songs.random(), songs) }
                    })
            }
            LazyColumn(
                state = state,
                modifier = Modifier
                    .then(if (wide) Modifier.weight(1f) else Modifier)
                    .fillMaxSize()
                    // same offset as the cover column on the left
                    .then(if (wide) Modifier.statusBarsPadding().padding(top = 9.5.dp) else Modifier)
                    .overScrollVertical(),
                flingBehavior = rememberOverscrollFlingBehavior { state },
                contentPadding = PaddingValues(bottom = 18.dp, top = 54.dp)
            ) {
                if (!wide) item("AlbumInfo") { header() }

                if (!wide) item {
                    AlbumDivider()
                }

                itemsIndexed(
                    songs,
                    key = { _, music -> music }/*,
                    contentType = { _, _ -> "AlbumInfo_item" }*/
                ) { index, music ->
                    key(music) {
                        AlbumSongsItem(
                            // tablets: rounded rows, the song playing tinted pink
                            modifier = if (wide) Modifier
                                .padding(end = 18.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (music == playing) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent)
                            else Modifier,
                            music = music,
                            mainArtists = mainArtists.value,
                            highlight = wide && music == playing
                        ) {
                            scope.launch(Dispatchers.IO) {
                                MediaController.prepare(
                                    music,
                                    songs
                                )
                            }
                        }
                    }

                    key(index) {
                        val needDivider = !wide && index < songs.size - 1
                        if (needDivider) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 50.dp, end = 18.dp)
                                    .alpha(0.25f)
                                    .height(0.5.dp)
                                    .background(Color.Black withNight Color.White)
                            )
                        }
                    }
                }

                if (!wide) item {
                    AlbumDivider()
                }

                if (!wide) item("AlbumInfo_others") {
                    Text(
                        text = stringResource(
                            id = R.string.page_library_album_info_others,
                            songCount,
                            totalMinutes
                        ), fontSize = 15.sp, modifier = Modifier
                            .alpha(0.4f)
                            .padding(horizontal = 18.dp)
                            .padding(top = 18.dp)
                    )
                }

                item("navbar") {
                    Spacer(modifier = Modifier.navigationBarsHeight(134.dp))
                }
            }

            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsHeight(54.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp)
                ) {
                    Box(
                        Modifier.statusBarsHeight(48.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = null,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(horizontal = 10.dp)
                                .size(32.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        navController.popBackStack()
                                    }
                                ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

// tablets: cover, bold title, the artist with their photo, and pill buttons, all aligned left
@Composable
private fun WideHeader(
    album: String, songs: List<YosMediaItem>, artist: String?, artistsName: String, info: String,
    onPlay: () -> Unit, onShuffle: () -> Unit
) {
    val context = LocalContext.current
    val photo = remember(artist) { mutableStateOf<String?>(null) }
    LaunchedEffect(artist) {
        if (artist != null) photo.value = ArtistArt.get(context, artist)?.thumb?.let { ArtistArt.local(context, it) }
    }
    ShadowImage(
        modifier = Modifier.fillMaxWidth(),
        dataLambda = { songs.getOrNull(0)?.thumb },
        contentDescription = null,
        cornerRadius = 14.dp,
        imageQuality = ImageQuality.RAW,
        shadowType = ShadowType.Medium
    )
    Text(album, fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 16.dp))
    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (photo.value != null) AsyncImage(
            model = photo.value, contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.padding(end = 8.dp).size(26.dp).clip(CircleShape)
        )
        Text(artistsName, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    Text(info, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp).alpha(0.5f))
    Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PillButton(R.drawable.button_icon_play, stringResource(R.string.normal_button_play), true, Modifier.weight(1f), onPlay)
        PillButton(R.drawable.button_icon_shuffle, stringResource(R.string.normal_button_shuffle), false, Modifier.weight(1f), onShuffle)
    }
}

@Composable
private fun PillButton(icon: Int, label: String, primary: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val fg = if (primary) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
    Row(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = if (primary) 1f else 0.14f))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(painterResource(id = icon), contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Text(label, color = fg, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
fun NormalButton(icon: Painter, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .background(
                color = (Color.LightGray withNight Color.DarkGray).copy(alpha = 0.25f),
                shape = shape
            )
            .clip(shape)
            .clickable(onClick = onClick)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp
        )
    }
}

/**
 * 专辑页面的横向分割线
 */
@Composable
private fun AlbumDivider(modifier: Modifier = Modifier) =
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .alpha(0.2f)
            .height(0.5.dp)
            .background(Color.Black withNight Color.White)
    )

@Composable
private fun AlbumSongsItem(
    modifier: Modifier = Modifier,
    music: YosMediaItem,
    mainArtists: List<String>,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    val tint = if (highlight) MaterialTheme.colorScheme.primary else Color.Unspecified
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.TopCenter, modifier = Modifier
                .width(24.dp)
                /*.height(21.dp)*/
                .fillMaxHeight()
        ) {
            Text(
                text = "${music.trackNumber?:"-"}",
                fontSize = 16.sp,
                color = tint,
                modifier = Modifier.alpha(if (highlight) 1f else 0.4f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(10.dp))

        Column(Modifier.padding(vertical = 10.dp)) {
            Text(
                text = music.title ?: defaultTitle,
                fontSize = 16.sp,
                color = tint,
                fontWeight = if (highlight) FontWeight.SemiBold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            YosWrapper {
                val needShowArtists = remember(music) {
                    derivedStateOf {
                        !mainArtists.containsAll(music.artistsList ?: defaultArtists)
                    }
                }
                if (needShowArtists.value) {
                    Text(
                        text = music.artistsName ?: defaultArtistsName,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                        modifier = Modifier.alpha(0.4f)
                    )
                }
            }
        }
    }
}