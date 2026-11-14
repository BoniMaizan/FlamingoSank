package yos.music.player.ui.pages.library.playlists

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import yos.music.player.R
import yos.music.player.data.libraries.MusicLibrary.songs
import yos.music.player.data.libraries.PlayListLibrary
import yos.music.player.data.libraries.PlayListLibrary.rename
import yos.music.player.data.libraries.PlayListLibrary.setSongs
import yos.music.player.data.libraries.YosMediaItem
import yos.music.player.data.libraries.artistsName
import yos.music.player.data.libraries.defaultArtistsName
import yos.music.player.data.libraries.defaultTitle
import yos.music.player.ui.widgets.basic.ImageQuality
import yos.music.player.ui.widgets.basic.OptionDialog
import yos.music.player.ui.widgets.basic.SearchTextField
import yos.music.player.ui.widgets.basic.ShadowImageWithCache
import yos.music.player.ui.widgets.basic.Title
import yos.music.player.ui.widgets.basic.tileColor

private val RowHeight = 60.dp
private val RemoveRed = Color(0xFFFF3B30)

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
    val nextId = remember { longArrayOf(0L) }
    val entries = remember { mutableStateOf(initial.songDataList.map { nextId[0]++ to it }) }
    val confirmDelete = remember { mutableStateOf(false) }
    val addSongs = remember { mutableStateOf(false) }
    val current = { PlayListLibrary.playList.firstOrNull { it.listID == id } }
    val save: (List<Pair<Long, Uri>>) -> Unit = { list ->
        entries.value = list
        current()?.setSongs(list.map { it.second })
    }
    val saveName = {
        val newName = name.value.trim()
        if (newName.isNotEmpty()) current()?.takeIf { it.name != newName }?.rename(newName)
    }
    DisposableEffect(Unit) { onDispose { saveName() } }

    Title(
        title = name.value.ifBlank { initial.name },
        onBack = { navController.popBackStack() },
        rightBarIcon = {
            Text(
                text = stringResource(id = R.string.playlist_edit_done),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { navController.popBackStack() }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            )
        }
    ) {
        item("body") {
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 6.dp)
            ) {
                val side = @Composable { modifier: Modifier ->
                    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PlayListCover(
                            entries.value.map { it.second },
                            Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        SearchTextField(
                            text = name.value,
                            placeholder = stringResource(id = R.string.nowplaying_more_new_playlist),
                            onValueChange = { name.value = it },
                            onSearch = saveName,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            entries.value.size.let { n -> if (n == 1) stringResource(id = R.string.playlist_edit_count_one) else stringResource(id = R.string.playlist_edit_count, n) },
                            fontSize = 14.sp,
                            modifier = Modifier.alpha(0.6f)
                        )
                        SideButton(stringResource(id = R.string.playlist_edit_add_songs), MaterialTheme.colorScheme.primary) {
                            addSongs.value = true
                        }
                        SideButton(stringResource(id = R.string.playlist_edit_delete), RemoveRed) {
                            confirmDelete.value = true
                        }
                    }
                }
                // Side by side on a tablet, stacked on a phone
                if (maxWidth > 640.dp) Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    side(Modifier.width(240.dp))
                    SongRows(entries.value, save, Modifier.weight(1f))
                } else Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    side(Modifier.fillMaxWidth())
                    SongRows(entries.value, save, Modifier.fillMaxWidth())
                }
            }
        }
    }

    if (addSongs.value) SongPicker(
        chosen = entries.value.map { it.second }.toSet(),
        onToggle = { uri ->
            save(
                if (entries.value.any { it.second == uri }) entries.value.filter { it.second != uri }
                else entries.value + (nextId[0]++ to uri)
            )
        },
        onDismiss = { addSongs.value = false }
    )

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

@Composable
private fun SideButton(text: String, color: Color, onClick: () -> Unit) =
    Box(
        Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(tileColor())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(text, color = color, fontSize = 15.sp) }

@Composable
private fun SongRows(entries: List<Pair<Long, Uri>>, save: (List<Pair<Long, Uri>>) -> Unit, modifier: Modifier) {
    val draggingId = remember { mutableStateOf<Long?>(null) }
    val dragOffset = remember { mutableFloatStateOf(0f) }
    // Order while dragging; saved once the finger lifts
    val live = remember { mutableStateOf<List<Pair<Long, Uri>>?>(null) }
    val list = live.value ?: entries
    val rowPx = with(LocalDensity.current) { RowHeight.toPx() }
    val moveUp = stringResource(id = R.string.playlist_edit_move_up)
    val moveDown = stringResource(id = R.string.playlist_edit_move_down)
    val removeLabel = stringResource(id = R.string.playlist_edit_remove)
    val divider = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    val lifted = MaterialTheme.colorScheme.onSecondary
    fun moved(from: Int, to: Int) = list.toMutableList().apply { add(to, removeAt(from)) }
    val endDrag = {
        live.value?.let(save)
        live.value = null
        draggingId.value = null
        dragOffset.floatValue = 0f
    }

    Column(modifier) {
        list.forEachIndexed { index, (rowId, uri) ->
            key(rowId) {
                val song = songs.firstOrNull { it.uri == uri }
                val dragging = draggingId.value == rowId
                Row(
                    Modifier
                        .zIndex(if (dragging) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (dragging) dragOffset.floatValue else 0f
                            if (dragging) {
                                scaleX = 1.02f; scaleY = 1.02f
                                shadowElevation = 16.dp.toPx()
                                shape = RoundedCornerShape(8.dp)
                                clip = true
                            }
                        }
                        .fillMaxWidth()
                        .height(RowHeight)
                        .background(if (dragging) lifted else Color.Transparent)
                        .drawBehind {
                            if (!dragging) drawLine(divider, Offset(0f, size.height), Offset(size.width, size.height), 1f)
                        }
                        .padding(horizontal = 8.dp)
                        // Screen readers can't drag: offer the moves as actions
                        .semantics {
                            customActions = listOfNotNull(
                                if (index > 0) CustomAccessibilityAction(moveUp) { save(moved(index, index - 1)); true } else null,
                                if (index < list.size - 1) CustomAccessibilityAction(moveDown) { save(moved(index, index + 1)); true } else null
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clickable { save(entries.filter { it.first != rowId }) },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(22.dp)
                                .background(RemoveRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Remove, removeLabel, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    ShadowImageWithCache(
                        dataLambda = { song?.thumb },
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(44.dp),
                        cornerRadius = 4.dp,
                        shadowAlpha = 0f,
                        imageQuality = ImageQuality.LOW
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Text(song?.title ?: uri.lastPathSegment ?: "", fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            song?.artistsName ?: "", fontSize = 13.sp, maxLines = 1,
                            overflow = TextOverflow.Ellipsis, modifier = Modifier.alpha(0.6f)
                        )
                    }
                    Icon(
                        Icons.Rounded.Menu,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .padding(10.dp)
                            .alpha(0.5f)
                            .pointerInput(rowId) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        live.value = entries
                                        draggingId.value = rowId
                                        dragOffset.floatValue = 0f
                                    },
                                    onVerticalDrag = { change, dy ->
                                        change.consume()
                                        dragOffset.floatValue += dy
                                        val now = live.value ?: return@detectVerticalDragGestures
                                        val at = now.indexOfFirst { it.first == rowId }
                                        // Swap with the neighbour once the row has passed half of it
                                        if (dragOffset.floatValue > rowPx / 2 && at < now.size - 1) {
                                            live.value = now.toMutableList().apply { add(at + 1, removeAt(at)) }
                                            dragOffset.floatValue -= rowPx
                                        } else if (dragOffset.floatValue < -rowPx / 2 && at > 0) {
                                            live.value = now.toMutableList().apply { add(at - 1, removeAt(at)) }
                                            dragOffset.floatValue += rowPx
                                        }
                                    },
                                    onDragEnd = endDrag,
                                    onDragCancel = endDrag
                                )
                            }
                    )
                }
            }
        }
        if (list.size > 1) Text(
            stringResource(id = R.string.playlist_edit_drag_hint),
            fontSize = 13.sp,
            modifier = Modifier
                .padding(top = 12.dp, start = 8.dp)
                .alpha(0.5f)
        )
    }
}

// All songs with a search box; tap one to add it to the playlist, tap again to take it out
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongPicker(chosen: Set<Uri>, onToggle: (Uri) -> Unit, onDismiss: () -> Unit) {
    val search = remember { mutableStateOf("") }
    val all: List<YosMediaItem> = runCatching { songs }.getOrDefault(emptyList())
    val shown = remember(search.value, all) {
        val q = search.value.trim()
        if (q.isEmpty()) all else all.filter {
            (it.title ?: "").contains(q, true) || (it.artistsName ?: "").contains(q, true)
        }
    }
    OptionDialog(
        icon = {},
        title = stringResource(id = R.string.playlist_edit_add_songs),
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        content = {
            SearchTextField(
                text = search.value,
                placeholder = stringResource(id = R.string.playlist_edit_search),
                onValueChange = { search.value = it },
                onSearch = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            LazyColumn(Modifier.height(360.dp)) {
                items(shown, key = { it.uri.toString() }) { song ->
                    val inList = song.uri in chosen
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { song.uri?.let(onToggle) }
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(song.title ?: defaultTitle, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                song.artistsName ?: defaultArtistsName, fontSize = 13.sp, maxLines = 1,
                                overflow = TextOverflow.Ellipsis, modifier = Modifier.alpha(0.6f)
                            )
                        }
                        Icon(
                            if (inList) Icons.Rounded.Check else Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        positiveContent = stringResource(id = R.string.playlist_edit_done),
        onPositive = onDismiss,
        onDismissRequest = onDismiss
    )
}
