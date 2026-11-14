package yos.music.player.ui.pages.library.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import yos.music.player.R
import yos.music.player.data.libraries.MusicLibrary.songs
import yos.music.player.data.libraries.PlayListLibrary
import yos.music.player.data.libraries.PlayListLibrary.rename
import yos.music.player.data.libraries.PlayListLibrary.setSongs
import yos.music.player.ui.pages.settings.LabelItem
import yos.music.player.ui.widgets.basic.OptionDialog
import yos.music.player.ui.widgets.basic.RoundColumn
import yos.music.player.ui.widgets.basic.SearchTextField
import yos.music.player.ui.widgets.basic.Title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayListEdit(navController: NavController) {
    val id = PlayListLibrary.editingID
    val initial = remember { PlayListLibrary.playList.firstOrNull { it.listID == id } }
    if (initial == null) {
        navController.popBackStack()
        return
    }
    // Local copies so the page redraws right away; every change is saved immediately
    val name = remember { mutableStateOf(initial.name) }
    val uris = remember { mutableStateOf(initial.songDataList) }
    val confirmDelete = remember { mutableStateOf(false) }
    val current = { PlayListLibrary.playList.firstOrNull { it.listID == id } }
    val save: (List<android.net.Uri>) -> Unit = { list ->
        uris.value = list
        current()?.setSongs(list)
    }
    val saveName = {
        val newName = name.value.trim()
        if (newName.isNotEmpty()) current()?.takeIf { it.name != newName }?.rename(newName)
    }
    DisposableEffect(Unit) { onDispose { saveName() } }

    Title(
        title = name.value.ifBlank { initial.name },
        onBack = { navController.popBackStack() }
    ) {
        item("name") {
            SearchTextField(
                text = name.value,
                placeholder = stringResource(id = R.string.nowplaying_more_new_playlist),
                onValueChange = { name.value = it },
                onSearch = saveName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 5.dp, bottom = 12.dp)
            )
        }
        itemsIndexed(uris.value, key = { index, uri -> "$index$uri" }) { index, uri ->
            val song = songs.firstOrNull { it.uri == uri }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = song?.title ?: uri.lastPathSegment ?: "",
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song?.artists ?: "",
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(0.6f)
                    )
                }
                val list = uris.value
                IconButton(enabled = index > 0, onClick = {
                    save(list.toMutableList().apply { add(index - 1, removeAt(index)) })
                }) { Icon(Icons.Rounded.KeyboardArrowUp, stringResource(id = R.string.playlist_edit_move_up)) }
                IconButton(enabled = index < list.size - 1, onClick = {
                    save(list.toMutableList().apply { add(index + 1, removeAt(index)) })
                }) { Icon(Icons.Rounded.KeyboardArrowDown, stringResource(id = R.string.playlist_edit_move_down)) }
                IconButton(onClick = {
                    save(list.toMutableList().apply { removeAt(index) })
                }) {
                    Icon(
                        Icons.Rounded.Close,
                        stringResource(id = R.string.playlist_edit_remove),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        item("delete") {
            Column(Modifier.padding(top = 20.dp)) {
                RoundColumn {
                    LabelItem(
                        title = stringResource(id = R.string.playlist_edit_delete),
                        superLink = true
                    ) { confirmDelete.value = true }
                }
            }
        }
    }

    if (confirmDelete.value) {
        OptionDialog(
            icon = {},
            title = stringResource(id = R.string.playlist_edit_delete_confirm),
            content = null,
            positiveContent = stringResource(id = R.string.playlist_edit_delete),
            negativeContent = stringResource(id = R.string.settings_play_history_clear_cancel),
            onPositive = {
                confirmDelete.value = false
                current()?.let { PlayListLibrary.remove(it) }
                navController.popBackStack()
            },
            onNegative = { confirmDelete.value = false },
            onDismissRequest = { confirmDelete.value = false }
        )
    }
}
