package yos.music.player.ui.pages

import androidx.compose.foundation.clickable
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
fun HomeShelf(title: String, list: List<YosMediaItem>, navController: NavController) {
    if (list.isEmpty()) return
    val scope = rememberCoroutineScope()
    val shown = list.take(SHELF_SIZE)
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
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
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
                        .width(130.dp)
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
                            .size(130.dp)
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
