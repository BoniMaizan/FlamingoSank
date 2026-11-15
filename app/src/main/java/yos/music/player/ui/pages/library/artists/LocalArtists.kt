package yos.music.player.ui.pages.library.artists

import yos.music.player.ui.toUI
import yos.music.player.ui.UI
import yos.music.player.data.objects.LibraryObject
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import yos.music.player.data.libraries.ArtistArt
import yos.music.player.ui.pages.isWideLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import yos.music.player.R
import yos.music.player.data.libraries.MusicLibrary
import yos.music.player.ui.theme.withNight
import yos.music.player.ui.widgets.basic.SearchTextField
import yos.music.player.ui.widgets.basic.Title
import yos.music.player.ui.widgets.basic.YosWrapper

@Composable
fun LocalArtists(navController: NavController) {
    Column(
        Modifier
            .fillMaxSize()
        /*.statusBarsPadding()*/
    ) {
        val artistsList = MusicLibrary.artists

        val searchText = remember("LocalArtists_searchText") {
            mutableStateOf("")
        }

        val hideMusic = remember("LocalArtists_showMusic") {
            derivedStateOf {
                artistsList.isEmpty()
            }
        }
        if (hideMusic.value) {
            val message =
                stringResource(
                    id = R.string.tip_no_song
                )
            Title(
                title = stringResource(id = R.string.page_library_artists), onBack = {
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
            val useSearch = remember { derivedStateOf { searchText.value.isNotEmpty() } }
            val list = remember { mutableStateOf(artistsList) }

            YosWrapper {
                LaunchedEffect(searchText.value) {
                    withContext(Dispatchers.IO) {
                        val filteredList = withContext(Dispatchers.IO) {
                            if (useSearch.value) {
                                MusicLibrary.artists.asSequence().filter { artist ->
                                    artist.contains(searchText.value, ignoreCase = true)
                                }.toList()
                            } else {
                                artistsList
                            }
                        }
                        list.value = filteredList
                    }
                }
            }

            val wide = isWideLayout()
            // tablets: a grid of round portraits, A to Z
            val gridRows = remember(list.value, wide) {
                if (wide) list.value.sortedBy { it.lowercase() }.chunked(6) else emptyList()
            }
            Title(
                title = stringResource(id = R.string.page_library_artists), onBack = {
                    navController.popBackStack()
                }
            ) {
                item("SearchField") {
                    val keyboardController = LocalSoftwareKeyboardController.current

                    SearchTextField(
                        text = searchText.value,
                        placeholder = stringResource(id = R.string.page_library_search_artists),
                        onValueChange = {
                            searchText.value = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .padding(top = 5.dp, bottom = 12.dp),
                        onSearch = {
                            if (searchText.value.isNotEmpty()) {
                                keyboardController?.hide()
                            }
                        })
                }

                if (wide) items(gridRows, key = { it.first() }) { row ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { artist ->
                            ArtistTile(artist, Modifier.weight(1f)) {
                                LibraryObject.setTargetArtistName(artist)
                                navController.toUI(UI.ArtistInfo)
                            }
                        }
                        repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                } else itemsIndexed(
                    list.value,
                    key = { _, artist -> artist }/*,
                    contentType = { _, _ -> "LocalArtists_item" }*/
                ) { index, artist ->
                    ArtistItem(artistName = artist) {
                        LibraryObject.setTargetArtistName(artist)
                        navController.toUI(UI.ArtistInfo)
                    }

                    key(index) {
                        val needDivider = index < artistsList.size - 1
                        if (needDivider) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 81.dp)
                                    .alpha(0.15f)
                                    .height(0.5.dp)
                                    .background(Color.Black withNight Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.ArtistItem(
    modifier: Modifier = Modifier,
    artistName: String,
    onClick: () -> Unit
) =
    Row(
        modifier = Modifier
            .animateItem(fadeInSpec = null, fadeOutSpec = null)
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val songs = MusicLibrary.Artist[artistName]
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
            YosWrapper {
                val shape = CircleShape

                    val density = LocalDensity.current
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data = songs.getOrNull(0)?.thumb).crossfade(true)
                            .error(R.drawable.songcredits_monogram_person)
                            .placeholder(R.drawable.songcredits_monogram_person)
                            .fallback(R.drawable.songcredits_monogram_person)
                            .allowHardware(true)
                            .precision(Precision.INEXACT)
                            .size(128)
                            .build(),
                        contentDescription = "Artist_Image",
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                            .size(48.dp)
                            .aspectRatio(1f)
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                                clip = true
                                this.shape = shape
                            }
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    val outline = shape.createOutline(
                                        Size(size.width, size.height),
                                        LayoutDirection.Ltr,
                                        density
                                    )
                                    drawOutline(
                                        outline = outline,
                                        color = Color.DarkGray.copy(alpha = 0.08f),
                                        style = Stroke(width = 6f)
                                    )
                                    drawOutline(
                                        outline = outline,
                                        color = Color.DarkGray.copy(alpha = 0.4f),
                                        style = Stroke(width = 6f),
                                        blendMode = BlendMode.Overlay
                                    )
                                }
                            }
                    )
            }
        }
        Spacer(modifier = Modifier.width(15.dp))
        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text(
                text = artistName,
                fontSize = 16.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_action_next), contentDescription = null,
            modifier = Modifier
                .height(12.dp).padding(end = 8.dp)
                .alpha(0.3f), tint = MaterialTheme.colorScheme.onBackground
        )
    }

@Composable
private fun ArtistTile(artist: String, modifier: Modifier, onClick: () -> Unit) = Column(
    modifier
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .padding(vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    val context = LocalContext.current
    val songs = remember(artist) { MusicLibrary.Artist[artist] }
    val thumb = songs.firstOrNull()?.thumb
    val photo = remember(artist) { mutableStateOf<String?>(null) }
    LaunchedEffect(artist) { photo.value = ArtistArt.get(context, artist)?.let { it.thumb ?: it.photo } }
    Box(
        Modifier
            .fillMaxWidth(0.86f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background((Color.Black withNight Color.White).copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        if (thumb == null && photo.value == null) Text(
            artist.take(1).uppercase(), fontSize = 40.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.alpha(0.45f)
        )
        // the cover stays underneath until the artist's photo arrives (or if there is none)
        listOfNotNull(thumb, photo.value).forEach { image ->
            AsyncImage(
                model = ImageRequest.Builder(context).data(image).size(320).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(0f, -0.6f),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    Text(
        artist, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
    )
    Text(
        pluralStringResource(R.plurals.artist_songs, songs.size, songs.size),
        fontSize = 12.sp, modifier = Modifier.alpha(0.5f)
    )
}
