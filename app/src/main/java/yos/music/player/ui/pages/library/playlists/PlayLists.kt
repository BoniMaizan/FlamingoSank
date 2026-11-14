package yos.music.player.ui.pages.library.playlists

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import yos.music.player.data.libraries.PlayListLibrary
import yos.music.player.ui.widgets.basic.OptionDialog
import yos.music.player.ui.widgets.basic.SearchTextField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastMapNotNull
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yos.music.player.R
import yos.music.player.data.libraries.FavPlayListLibrary
import yos.music.player.data.libraries.MusicLibrary.songs
import yos.music.player.data.libraries.PlayList
import yos.music.player.data.libraries.PlayListLibrary.playList
import yos.music.player.data.libraries.YosMediaItem
import yos.music.player.data.objects.LibraryObject
import yos.music.player.ui.UI
import yos.music.player.ui.theme.YosRoundedCornerShape
import yos.music.player.ui.theme.withNight
import yos.music.player.ui.toUI
import yos.music.player.ui.widgets.basic.Title

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayLists(navController: NavController) {
    val playLists = playList.sortedBy { it.name }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val creating = remember { mutableStateOf(false) }
    val newName = remember { mutableStateOf("") }
    val openEdit: (PlayList) -> Unit = { list ->
        PlayListLibrary.editingID = list.listID
        navController.toUI(UI.PlayListEdit)
    }
    Title(title = stringResource(id = R.string.page_library_playlists),
        onBack = {
            navController.popBackStack()
        },
        content = {
            item("AddList") {
                val targetTitle = context.getString(R.string.page_library_playlists_add_title)
                PlayListItem(playListType = PlayListType.Add, title = targetTitle) {
                    newName.value = ""
                    creating.value = true
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 102.dp)
                        .alpha(0.15f)
                        .height(0.5.dp)
                        .background(Color.Black withNight Color.White)
                )
            }

            item("FavList") {
                val targetTitle = context.getString(R.string.page_library_playlists_fav_title)
                PlayListItem(playListType = PlayListType.Favorite, title = targetTitle) {
                    scope.launch(Dispatchers.IO) {
                        val targetList = FavPlayListLibrary.favPlayList
                        LibraryObject.setTargetListWithTitle(targetTitle, targetList)
                        withContext(Dispatchers.Main) {
                            navController.toUI(UI.NormalMusic)
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 102.dp)
                        .alpha(0.15f)
                        .height(0.5.dp)
                        .background(Color.Black withNight Color.White)
                )
            }

            itemsIndexed(
                playLists,
                key = { _, playList -> playList.listID }
            ) { index, playList ->
                PlayListItem(playList = playList, onLongClick = { openEdit(playList) }) {
                    scope.launch(Dispatchers.IO) {
                        val targetTitle = playList.name
                        val targetList = convertToSongList(playList.songDataList, songs)
                        LibraryObject.setTargetListWithTitle(targetTitle, targetList, keepOrder = true)
                        withContext(Dispatchers.Main) {
                            navController.toUI(UI.NormalMusic)
                        }
                    }
                }
                key(index) {
                    val needDivider = index < playLists.size - 1
                    if (!needDivider) {
                        Text(
                            text = stringResource(id = R.string.playlist_edit_hint),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .padding(start = 22.dp, top = 12.dp)
                                .alpha(0.5f)
                        )
                    }
                    if (needDivider) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 102.dp)
                                .alpha(0.15f)
                                .height(0.5.dp)
                                .background(Color.Black withNight Color.White)
                        )
                    }
                }
            }
        }
    )

    if (creating.value) {
        OptionDialog(
            icon = {},
            title = stringResource(id = R.string.page_library_playlists_add_title),
            content = {
                SearchTextField(
                    text = newName.value,
                    placeholder = stringResource(id = R.string.nowplaying_more_new_playlist),
                    onValueChange = { newName.value = it },
                    onSearch = {},
                    modifier = Modifier.fillMaxWidth()
                )
            },
            positiveContent = stringResource(id = R.string.playlist_edit_create),
            negativeContent = stringResource(id = R.string.settings_play_history_clear_cancel),
            onPositive = {
                val name = newName.value.trim()
                if (name.isNotEmpty()) PlayListLibrary.create(name)
                creating.value = false
            },
            onNegative = { creating.value = false },
            onDismissRequest = { creating.value = false }
        )
    }
}

private fun convertToSongList(
    songDataList: List<Uri>,
    songs: List<YosMediaItem>
): List<YosMediaItem> {
    return songDataList.fastMapNotNull { uri ->
        songs.find { it.uri == uri }
    }
}

@Stable
private enum class PlayListType {
    Add, Favorite
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.PlayListItem(playList: PlayList, onLongClick: () -> Unit, itemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .animateItem(fadeInSpec = null, fadeOutSpec = null)
            .height(80.dp)
            .fillMaxWidth()
            .combinedClickable(onLongClick = onLongClick, onClick = itemClick)
            .padding(start = 22.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        println("重组：播放列表 ${playList.name}")

        val shape = YosRoundedCornerShape(4.dp)
        val density = LocalDensity.current

        Image(painter = painterResource(id = R.drawable.placeholder_playlist_default),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
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
                            color = Color.Gray.copy(alpha = 0.1f),
                            style = Stroke(width = 8f)
                        )
                        drawOutline(
                            outline = outline,
                            color = Color.Gray.copy(alpha = 0.5f),
                            style = Stroke(width = 8f),
                            blendMode = BlendMode.Overlay
                        )
                    }
                })

        Column(
            Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = playList.name,
                modifier = Modifier.padding(bottom = 1.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                lineHeight = 16.sp,
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_action_next), contentDescription = null,
            modifier = Modifier
                .height(40.dp)
                .alpha(0.3f), tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun LazyItemScope.PlayListItem(playListType: PlayListType, title: String, itemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .animateItem(fadeInSpec = null, fadeOutSpec = null)
            .height(80.dp)
            .fillMaxWidth()
            .clickable {
                itemClick()
            }
            .padding(start = 22.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        println("重组：播放列表 ${playListType.name}")

        val shape = YosRoundedCornerShape(4.dp)
        val density = LocalDensity.current

        Image(painter = painterResource(id = if (playListType == PlayListType.Add) R.drawable.placeholder_playlist_new else R.drawable.placeholder_playlist_default),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
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
                            color = Color.Gray.copy(alpha = 0.1f),
                            style = Stroke(width = 8f)
                        )
                        drawOutline(
                            outline = outline,
                            color = Color.Gray.copy(alpha = 0.5f),
                            style = Stroke(width = 8f),
                            blendMode = BlendMode.Overlay
                        )
                    }
                })

        Column(
            Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(bottom = 1.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                lineHeight = 16.sp,
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_action_next), contentDescription = null,
            modifier = Modifier
                .height(40.dp)
                .alpha(0.3f), tint = MaterialTheme.colorScheme.onBackground
        )
    }
}