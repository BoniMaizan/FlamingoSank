package yos.music.player.ui.pages.library

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import yos.music.player.R
import yos.music.player.code.MediaController
import yos.music.player.data.libraries.HistoryLibrary
import yos.music.player.data.libraries.YosMediaItem
import yos.music.player.data.libraries.artistsName
import yos.music.player.data.libraries.defaultArtistsName
import yos.music.player.data.libraries.defaultTitle
import yos.music.player.ui.pages.MoreMenu
import yos.music.player.ui.theme.withNight
import yos.music.player.ui.toUI
import yos.music.player.ui.widgets.basic.ImageQuality
import yos.music.player.ui.widgets.basic.OptionDialog
import yos.music.player.ui.widgets.basic.Segmented
import yos.music.player.ui.widgets.basic.ShadowImageWithCache
import yos.music.player.ui.widgets.basic.Title
import yos.music.player.ui.widgets.basic.tileColor
import java.util.Calendar
import java.util.Date

// Start of the day holding this time, to group listens by day
private fun dayOf(time: Long): Long = Calendar.getInstance().apply {
    timeInMillis = time
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tab = rememberSaveable { mutableIntStateOf(0) }
    val confirmClear = remember { mutableStateOf(false) }
    val menuSong = remember { mutableStateOf<YosMediaItem?>(null) }
    menuSong.value?.let { song ->
        MoreMenu(song, { route -> navController.toUI(route) }, queueActions = true) { menuSong.value = null }
    }

    val play: (YosMediaItem) -> Unit = { music ->
        // The queue can't hold a song twice
        val queue = HistoryLibrary.history.distinctBy { it.uri }
        scope.launch(Dispatchers.IO) { MediaController.prepare(music, queue) }
    }

    Title(
        title = stringResource(id = R.string.page_library_history),
        onBack = { navController.popBackStack() },
        rightBarIcon = {
            if (HistoryLibrary.history.isNotEmpty()) Text(
                text = stringResource(id = R.string.history_clear),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(tileColor())
                    .clickable { confirmClear.value = true }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }
    ) {
        // Read here, not above: Title keeps its first content lambda, so only state read inside it updates the list
        val history = HistoryLibrary.history
        val times = HistoryLibrary.timeList()
        item("tabs") {
            Segmented(
                options = listOf(stringResource(id = R.string.history_recent), stringResource(id = R.string.history_most_played)),
                selected = tab.intValue,
                onSelect = { tab.intValue = it },
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .padding(top = 5.dp, bottom = 6.dp)
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
            )
        }
        if (history.isEmpty()) {
            item("empty") {
                Text(
                    text = stringResource(id = R.string.history_empty),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(horizontal = 22.dp, vertical = 16.dp)
                        .alpha(0.6f)
                )
            }
        } else if (tab.intValue == 0) {
            val today = dayOf(System.currentTimeMillis())
            // history is newest first, so the groups come out newest first too
            history.indices.groupBy { i -> times[i]?.let(::dayOf) }.forEach { (day, rows) ->
                item("day$day") {
                    Text(
                        text = when {
                            day == null -> stringResource(id = R.string.history_earlier)
                            day == today -> stringResource(id = R.string.history_today)
                            today - day in 1..DateUtils.DAY_IN_MILLIS + DateUtils.HOUR_IN_MILLIS -> stringResource(id = R.string.history_yesterday)
                            else -> DateUtils.formatDateTime(context, day, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY)
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 22.dp, top = 14.dp, bottom = 2.dp)
                            .alpha(0.6f)
                    )
                }
                items(rows, key = { "r$it" }) { i ->
                    HistoryRow(
                        music = history[i],
                        trailing = times[i]?.let { DateFormat.getTimeFormat(context).format(Date(it)) },
                        onLongClick = { menuSong.value = history[i] }
                    ) { play(history[i]) }
                }
            }
        } else {
            val counts = history.groupingBy { it.uri }.eachCount()
            val ranked = history.distinctBy { it.uri }.map { it to (counts[it.uri] ?: 0) }.sortedByDescending { it.second }
            items(ranked, key = { "m${it.first.uri}" }) { (music, count) ->
                HistoryRow(
                    music = music,
                    trailing = if (count == 1) stringResource(id = R.string.history_plays_one) else stringResource(id = R.string.history_plays, count),
                    onLongClick = { menuSong.value = music }
                ) { play(music) }
            }
        }
    }

    if (confirmClear.value) {
        OptionDialog(
            icon = {},
            title = stringResource(id = R.string.settings_play_history_clear_confirm),
            content = null,
            positiveContent = stringResource(id = R.string.history_clear),
            negativeContent = stringResource(id = R.string.settings_play_history_clear_cancel),
            onPositive = {
                confirmClear.value = false
                HistoryLibrary.clear()
            },
            onNegative = { confirmClear.value = false },
            onDismissRequest = { confirmClear.value = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryRow(music: YosMediaItem, trailing: String?, onLongClick: () -> Unit, onClick: () -> Unit) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .height(62.dp)
                .combinedClickable(onLongClick = onLongClick, onClick = onClick)
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShadowImageWithCache(
                dataLambda = { music.thumb },
                contentDescription = null,
                modifier = Modifier.size(46.dp),
                cornerRadius = 4.dp,
                shadowAlpha = 0f,
                imageQuality = ImageQuality.LOW
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(music.title ?: defaultTitle, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    music.artistsName ?: defaultArtistsName, fontSize = 13.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.alpha(0.6f)
                )
            }
            if (trailing != null) Text(
                trailing, fontSize = 13.sp,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .alpha(0.5f)
            )
        }
        Spacer(
            Modifier
                .fillMaxWidth()
                .padding(start = 82.dp)
                .alpha(0.15f)
                .height(0.5.dp)
                .background(Color.Black withNight Color.White)
        )
    }
}
