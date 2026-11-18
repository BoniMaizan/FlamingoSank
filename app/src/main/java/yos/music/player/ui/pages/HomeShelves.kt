package yos.music.player.ui.pages

import androidx.compose.foundation.clickable
import yos.music.player.ui.theme.withNight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import yos.music.player.data.libraries.MusicLibrary
import yos.music.player.data.libraries.artistsList
import yos.music.player.ui.pages.library.artists.ArtistTile
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import yos.music.player.R
import yos.music.player.code.MediaController
import yos.music.player.data.libraries.YosMediaItem
import yos.music.player.data.libraries.artistsName
import yos.music.player.data.libraries.defaultArtistsName
import yos.music.player.data.libraries.defaultTitle
import yos.music.player.data.objects.LibraryObject
import yos.music.player.ui.UI
import yos.music.player.ui.theme.YosRoundedCornerShape
import yos.music.player.ui.toUI

private const val SHELF_SIZE = 20

// A titled row of covers on Home; "See All" opens the whole list in its order
@Composable
fun HomeShelf(
    title: String,
    list: List<YosMediaItem>,
    navController: NavController,
    subTitle: String? = null
) {
    val shown = list.take(SHELF_SIZE)
    if (shown.isEmpty()) return
    val scope = rememberCoroutineScope()
    val tile = if (isWideLayout()) 160.dp else 130.dp
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 20.sp
            )
            Text(
                text = subTitle ?: "",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
                    .alpha(0.5f)
            )
            Text(
                text = stringResource(id = R.string.home_see_all),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    LibraryObject.setTargetListWithTitle(title, list, keepOrder = true)
                    navController.toUI(UI.NormalMusic)
                }
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(top = 10.dp)
        ) {
            items(shown, key = { it.uri.toString() }) { music ->
                Column(
                    Modifier
                        .width(tile)
                        .clickable {
                            scope.launch(Dispatchers.IO) { MediaController.prepare(music, shown) }
                        }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(music.thumb)
                            .crossfade(true)
                            .placeholder(R.drawable.placeholder_music_default_artwork)
                            .error(R.drawable.placeholder_music_default_artwork)
                            .fallback(R.drawable.placeholder_music_default_artwork).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(tile)
                            .clip(YosRoundedCornerShape(8.dp))
                    )
                    Text(
                        text = music.title ?: defaultTitle,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        text = music.artistsName ?: defaultArtistsName,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(0.6f)
                    )
                }
            }
        }
    }
}

// Tablets: a banner on the last song played, its cover blurred behind
@Composable
fun HomeHero(history: List<YosMediaItem>) {
    val music = history.firstOrNull() ?: return
    val scope = rememberCoroutineScope()
    val queue = history.take(SHELF_SIZE)
    val play: (Boolean) -> Unit = { shuffle ->
        MediaController.mediaControl?.shuffleModeEnabled = shuffle
        scope.launch(Dispatchers.IO) { MediaController.prepare(if (shuffle) queue.random() else music, queue) }
    }
    val cover = ImageRequest.Builder(LocalContext.current).data(music.thumb)
        .crossfade(true)
        .placeholder(R.drawable.placeholder_music_default_artwork)
        .error(R.drawable.placeholder_music_default_artwork)
        .fallback(R.drawable.placeholder_music_default_artwork).build()
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 6.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1C1C1E))
            .clickable { play(false) }
    ) {
        AsyncImage(
            model = cover, contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(40.dp)
        )
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.2f)))))
        Row(Modifier.fillMaxSize().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = cover, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.size(164.dp).clip(YosRoundedCornerShape(14.dp))
            )
            Column(Modifier.weight(1f).padding(start = 22.dp)) {
                Text(
                    stringResource(id = R.string.home_jump_back_in).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp, color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    music.title ?: defaultTitle, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black, color = Color.White,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    music.artistsName ?: defaultArtistsName, fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp)
                )
            }
            Row(Modifier.padding(start = 16.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroPill(R.drawable.button_icon_play, stringResource(id = R.string.home_resume), true) { play(false) }
                HeroPill(R.drawable.button_icon_shuffle, stringResource(id = R.string.normal_button_shuffle), false) { play(true) }
            }
        }
    }
}

@Composable
private fun HeroPill(icon: Int, label: String, primary: Boolean, onClick: () -> Unit) {
    val fg = if (primary) Color.Black else Color.White
    Row(
        Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (primary) Color.White else Color.White.copy(alpha = 0.18f))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(id = icon), contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Text(label, color = fg, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun ShelfTitle(title: String, onSeeAll: () -> Unit) = Row(
    Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(top = 18.dp, bottom = 10.dp),
    verticalAlignment = Alignment.Bottom
) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
    Text(
        stringResource(id = R.string.home_see_all), fontSize = 15.sp, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable(onClick = onSeeAll)
    )
}

// Tablets: the artists last listened to first, then the rest of the library
@Composable
fun HomeArtists(history: List<YosMediaItem>, navController: NavController) {
    val artists = remember(history) {
        (history.mapNotNull { it.artistsList?.firstOrNull() } + MusicLibrary.artists)
            .distinct().filter { MusicLibrary.Artist[it].isNotEmpty() }.take(SHELF_SIZE)
    }
    if (artists.isEmpty()) return
    ShelfTitle(stringResource(id = R.string.home_your_artists)) { navController.toUI(UI.LocalArtists) }
    LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(artists, key = { it }) { artist ->
            ArtistTile(artist, Modifier.width(128.dp)) {
                LibraryObject.setTargetArtistName(artist)
                navController.toUI(UI.ArtistInfo)
            }
        }
    }
}

// Tablets: albums in the order their files arrived on the device
@Composable
fun HomeAlbums(recent: List<YosMediaItem>, navController: NavController) {
    val albums = remember(recent) { recent.filter { !it.album.isNullOrEmpty() }.distinctBy { it.album }.take(SHELF_SIZE) }
    if (albums.isEmpty()) return
    ShelfTitle(stringResource(id = R.string.home_recent_albums)) { navController.toUI(UI.LocalAlbums) }
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(albums, key = { it.album!! }) { song ->
            Column(
                Modifier
                    .width(160.dp)
                    .clickable {
                        LibraryObject.setTargetAlbumName(song.album!!)
                        navController.toUI(UI.AlbumInfo)
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(song.thumb)
                        .crossfade(true)
                        .placeholder(R.drawable.placeholder_music_default_artwork)
                        .error(R.drawable.placeholder_music_default_artwork)
                        .fallback(R.drawable.placeholder_music_default_artwork).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(160.dp).clip(YosRoundedCornerShape(12.dp))
                )
                Text(song.album!!, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp))
                Text(song.artistsName ?: defaultArtistsName, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(0.6f))
            }
        }
    }
}
