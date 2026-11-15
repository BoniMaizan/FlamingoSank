package yos.music.player.ui.pages

import androidx.compose.foundation.clickable
import yos.music.player.ui.theme.withNight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Box
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

// Tablets: the last song played on the left, the ones before it on the right
@Composable
fun HomeHero(history: List<YosMediaItem>) {
    val music = history.firstOrNull() ?: return
    val scope = rememberCoroutineScope()
    val queue = history.take(SHELF_SIZE)
    val play: () -> Unit = { scope.launch(Dispatchers.IO) { MediaController.prepare(music, queue) } }
    val card = (Color.Black withNight Color.White).copy(alpha = 0.07f)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 6.dp)
            .height(236.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
    Row(
        Modifier
            .weight(1.15f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(card)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
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
                .size(170.dp)
                .clip(YosRoundedCornerShape(12.dp))
                .clickable(onClick = play)
        )
        Column(
            Modifier
                .weight(1f)
                .padding(start = 18.dp)
        ) {
            Text(
                text = stringResource(id = R.string.home_last_played),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = music.title ?: defaultTitle,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = music.artistsName ?: defaultArtistsName,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .alpha(0.6f)
            )
            music.album?.let {
                Text(
                    text = it,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .alpha(0.45f)
                )
            }
            Row(
                Modifier
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = play)
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(id = R.drawable.button_icon_play),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(id = R.string.normal_button_play),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
    Column(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(card)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(id = R.string.home_jump_back_in),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.home_jump_back_in_desc),
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .alpha(0.5f)
            )
        }
        history.drop(1).take(4).forEach { song ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(40.dp)
                    .clickable { scope.launch(Dispatchers.IO) { MediaController.prepare(song, queue) } },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(song.thumb)
                        .crossfade(true)
                        .placeholder(R.drawable.placeholder_music_default_artwork)
                        .error(R.drawable.placeholder_music_default_artwork)
                        .fallback(R.drawable.placeholder_music_default_artwork).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(YosRoundedCornerShape(5.dp))
                )
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = song.title ?: defaultTitle,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artistsName ?: defaultArtistsName,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(0.5f)
                    )
                }
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(card),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(id = R.drawable.button_icon_play),
                        contentDescription = stringResource(id = R.string.normal_button_play),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
    }
}
