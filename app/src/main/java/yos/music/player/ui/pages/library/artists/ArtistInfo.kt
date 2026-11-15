package yos.music.player.ui.pages.library.artists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
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
import yos.music.player.data.libraries.ArtistArt
import yos.music.player.data.libraries.MusicLibrary
import yos.music.player.data.libraries.YosMediaItem
import yos.music.player.data.libraries.defaultAlbum
import yos.music.player.data.libraries.defaultTitle
import yos.music.player.data.objects.LibraryObject
import yos.music.player.ui.UI
import yos.music.player.ui.pages.isWideLayout
import yos.music.player.ui.theme.YosRoundedCornerShape
import yos.music.player.ui.theme.withNight
import yos.music.player.ui.toUI

// Artist page: a photo banner with the artist's logo, then songs and albums (side by side on tablets)
@Composable
fun ArtistInfo(navController: NavController) {
    val artist = remember { LibraryObject.getTargetArtistName() }
    val songs = remember(artist) { MusicLibrary.Artist[artist].sortedBy { it.title ?: "" } }
    val albums = remember(songs) { songs.groupBy { it.album ?: defaultAlbum }.toList() }
    val context = LocalContext.current
    val art = remember { mutableStateOf<ArtistArt.Art?>(null) }
    LaunchedEffect(artist) {
        art.value = ArtistArt.get(context, artist)?.let { a ->
            a.copy(photo = a.photo?.let { ArtistArt.local(context, it) }, logo = a.logo?.let { ArtistArt.local(context, it) })
        }
    }
    val wide = isWideLayout()
    val scope = rememberCoroutineScope()
    val play: (YosMediaItem, Boolean) -> Unit = { song, shuffle ->
        if (shuffle) MediaController.mediaControl?.shuffleModeEnabled = true
        scope.launch(Dispatchers.IO) { MediaController.prepare(song, songs) }
    }

    val pad = if (wide) 24.dp else 16.dp
    val back = @Composable { tint: Color, m: Modifier ->
        Row(
            m
                .padding(vertical = 10.dp)
                .clickable { navController.popBackStack() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(id = R.drawable.ic_back), contentDescription = null,
                tint = tint, modifier = Modifier.height(16.dp)
            )
            Text(
                stringResource(id = R.string.page_library_artists), color = tint,
                fontSize = 15.sp, modifier = Modifier.padding(start = 6.dp)
            )
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (songs.isEmpty()) {
            back(MaterialTheme.colorScheme.primary, Modifier.statusBarsPadding().padding(horizontal = pad))
            return@Column
        }

        // full-bleed header like the system music apps: the artist's photo edge to edge, under the status bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (wide) 420.dp else 340.dp)
                .background(Color(0xFF26262A))
        ) {
            // the cover stays underneath, so a photo that fails to load leaves the cover showing
            listOfNotNull(songs.first().thumb, art.value?.photo).forEach { image ->
                AsyncImage(
                    model = ImageRequest.Builder(context).data(image).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    // faces sit in the upper part of artist photos: crop from there, not the middle
                    alignment = BiasAlignment(0f, -0.6f),
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.35f), 0.22f to Color.Transparent,
                            0.5f to Color.Transparent, 0.8f to Color.Black.copy(alpha = 0.45f), 1f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
            )
            back(Color.White, Modifier.statusBarsPadding().padding(horizontal = pad))
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = pad, vertical = 24.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(Modifier.weight(1f)) {
                    val logo = art.value?.logo
                    if (logo != null) AsyncImage(
                        model = ImageRequest.Builder(context).data(logo).crossfade(true).build(),
                        contentDescription = artist,
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.CenterStart,
                        modifier = Modifier
                            .heightIn(max = if (wide) 80.dp else 60.dp)
                            .widthIn(max = 380.dp)
                    ) else Text(
                        artist, color = Color.White, fontSize = if (wide) 40.sp else 32.sp,
                        lineHeight = if (wide) 44.sp else 36.sp, fontWeight = FontWeight.Bold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        pluralStringResource(R.plurals.artist_songs, songs.size, songs.size) + " · " +
                            pluralStringResource(R.plurals.artist_albums, albums.size, albums.size),
                        color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                if (wide) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BannerButton(R.drawable.button_icon_play, stringResource(id = R.string.normal_button_play)) { play(songs.first(), false) }
                    BannerButton(R.drawable.button_icon_shuffle, stringResource(id = R.string.normal_button_shuffle)) { play(songs.random(), true) }
                }
            }
        }
        Column(Modifier.padding(horizontal = pad)) {
            if (!wide) Row(
                Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BannerButton(R.drawable.button_icon_play, stringResource(id = R.string.normal_button_play), Modifier.weight(1f)) { play(songs.first(), false) }
                BannerButton(R.drawable.button_icon_shuffle, stringResource(id = R.string.normal_button_shuffle), Modifier.weight(1f)) { play(songs.random(), true) }
            }

            val songList = @Composable { m: Modifier ->
                Column(m) {
                    SectionTitle(stringResource(id = R.string.page_library_songs))
                    songs.forEach { song -> SongRow(song) { play(song, false) } }
                }
            }
            val albumList = @Composable { m: Modifier ->
                Column(m) {
                    SectionTitle(stringResource(id = R.string.page_library_albums))
                    albums.chunked(2).forEach { pair ->
                        Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            pair.forEach { (album, tracks) ->
                                AlbumTile(album, tracks.first(), Modifier.weight(1f)) {
                                    LibraryObject.setTargetAlbumName(album)
                                    navController.toUI(UI.AlbumInfo)
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            if (wide) Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                songList(Modifier.weight(1f))
                albumList(Modifier.width(280.dp))
            } else {
                songList(Modifier.padding(top = 8.dp))
                albumList(Modifier)
            }
            // room for the mini player
            Spacer(Modifier.height(140.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) = Text(
    text, fontSize = 19.sp, fontWeight = FontWeight.Bold,
    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
)

@Composable
private fun BannerButton(icon: Int, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) = Row(
    modifier
        .height(40.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(Color.White)
        .clickable(onClick = onClick)
        .padding(horizontal = 18.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
) {
    Icon(painterResource(id = icon), contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
    Text(label, color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
}

@Composable
private fun SongRow(song: YosMediaItem, onClick: () -> Unit) = Row(
    Modifier
        .fillMaxWidth()
        .height(58.dp)
        .clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(song.thumb)
            .placeholder(R.drawable.placeholder_music_default_artwork)
            .error(R.drawable.placeholder_music_default_artwork)
            .fallback(R.drawable.placeholder_music_default_artwork).build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(44.dp)
            .clip(YosRoundedCornerShape(5.dp))
    )
    Column(
        Modifier
            .weight(1f)
            .padding(start = 12.dp)
    ) {
        Text(song.title ?: defaultTitle, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            song.album ?: defaultAlbum, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.alpha(0.5f)
        )
    }
}

@Composable
private fun AlbumTile(album: String, first: YosMediaItem, modifier: Modifier, onClick: () -> Unit) = Column(
    modifier.clickable(onClick = onClick)
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(first.thumb)
            .placeholder(R.drawable.placeholder_music_default_artwork)
            .error(R.drawable.placeholder_music_default_artwork)
            .fallback(R.drawable.placeholder_music_default_artwork).build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .clip(YosRoundedCornerShape(8.dp))
            .background((Color.Black withNight Color.White).copy(alpha = 0.06f))
            .then(Modifier.height(130.dp))
    )
    Text(album, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp))
}
