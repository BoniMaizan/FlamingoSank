package yos.music.player.ui.pages.library.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
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
    // Each row gets a stable id so a dragged row keeps its gesture while it moves
    var nextId = remember { longArrayOf(0L) }
    val entries = remember { mutableStateOf(initial.songDataList.map { nextId[0]++ to it }) }
    val draggingId = remember { mutableStateOf<Long?>(null) }
    val dragOffset = remember { mutableFloatStateOf(0f) }
    val rowPx = with(LocalDensity.current) { 64.dp.toPx() }
    val confirmDelete = remember { mutableStateOf(false) }
    val current = { PlayListLibrary.playList.firstOrNull { it.listID == id } }
    val save: (List<Pair<Long, android.net.Uri>>) -> Unit = { list ->
        entries.value = list
        current()?.setSongs(list.map { it.second })
    }
    val move: (Int, Int) -> List<Pair<Long, android.net.Uri>> = { from, to ->
        entries.value.toMutableList().apply { add(to, removeAt(from)) }
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
        itemsIndexed(entries.value, key = { _, entry -> entry.first }) { index, entry ->
            val (rowId, uri) = entry
            val song = songs.firstOrNull { it.uri == uri }
            val dragging = draggingId.value == rowId
            val moveUp = stringResource(id = R.string.playlist_edit_move_up)
            val moveDown = stringResource(id = R.string.playlist_edit_move_down)
            Row(
                Modifier
                    .zIndex(if (dragging) 1f else 0f)
                    .graphicsLayer { translationY = if (dragging) dragOffset.floatValue else 0f }
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(if (dragging) MaterialTheme.colorScheme.onSecondary else Color.Transparent)
                    .padding(start = 22.dp, end = 10.dp)
                    // Screen readers can't drag: offer the moves as actions
                    .semantics {
                        customActions = listOfNotNull(
                            if (index > 0) CustomAccessibilityAction(moveUp) { save(move(index, index - 1)); true } else null,
                            if (index < entries.value.size - 1) CustomAccessibilityAction(moveDown) { save(move(index, index + 1)); true } else null
                        )
                    },
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
                IconButton(onClick = {
                    save(entries.value.filter { it.first != rowId })
                }) {
                    Icon(
                        Icons.Rounded.Close,
                        stringResource(id = R.string.playlist_edit_remove),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    Icons.Rounded.Menu,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(40.dp)
                        .padding(8.dp)
                        .alpha(0.6f)
                        .pointerInput(rowId) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    draggingId.value = rowId
                                    dragOffset.floatValue = 0f
                                },
                                onVerticalDrag = { change, dy ->
                                    change.consume()
                                    dragOffset.floatValue += dy
                                    val list = entries.value
                                    val at = list.indexOfFirst { it.first == rowId }
                                    // Swap with the neighbour once the row has passed half of it
                                    if (dragOffset.floatValue > rowPx / 2 && at < list.size - 1) {
                                        entries.value = move(at, at + 1)
                                        dragOffset.floatValue -= rowPx
                                    } else if (dragOffset.floatValue < -rowPx / 2 && at > 0) {
                                        entries.value = move(at, at - 1)
                                        dragOffset.floatValue += rowPx
                                    }
                                },
                                onDragEnd = {
                                    save(entries.value)
                                    draggingId.value = null
                                    dragOffset.floatValue = 0f
                                },
                                onDragCancel = {
                                    save(entries.value)
                                    draggingId.value = null
                                    dragOffset.floatValue = 0f
                                }
                            )
                        }
                )
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
