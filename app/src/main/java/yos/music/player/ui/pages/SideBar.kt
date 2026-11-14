package yos.music.player.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import yos.music.player.R
import yos.music.player.data.libraries.FavPlayListLibrary
import yos.music.player.data.libraries.MusicLibrary
import yos.music.player.data.libraries.PlayListLibrary
import yos.music.player.data.objects.LibraryObject
import yos.music.player.ui.UI
import yos.music.player.ui.pages.library.playlists.PlayListCover
import yos.music.player.ui.theme.withNight

val SideBarWidth = 230.dp

// Tablets: wide enough for a sidebar instead of the two bottom tabs
@Composable
fun isWideLayout(): Boolean {
    val c = androidx.compose.ui.platform.LocalConfiguration.current
    return c.screenWidthDp >= 840 && c.screenWidthDp > c.screenHeightDp
}

// Left navigation for tablets: Home, the Library sections, the playlists and Settings
@Composable
fun SideBar(navController: NavController, route: String, onHome: () -> Unit, modifier: Modifier = Modifier) {
    // NormalMusic shows songs, a playlist, an artist…: remember which entry opened it
    val opened = rememberSaveable(key = "SideBar_opened") { mutableStateOf("") }
    val active = when (route) {
        UI.HomePage -> "home"
        UI.PlayLists, UI.PlayListEdit -> "playlists"
        UI.LocalArtists -> "artists"
        UI.LocalAlbums, UI.AlbumInfo -> "albums"
        UI.History -> "history"
        UI.NormalMusic -> opened.value
        else -> if (route.startsWith(UI.Settings.Main) || route.contains("Setting")) "settings" else ""
    }
    // Every entry starts from Home, so Back always returns there
    val go: (String, String) -> Unit = { key, target ->
        opened.value = key
        navController.navigate(target) {
            popUpTo(UI.HomePage)
            launchSingleTop = true
        }
    }
    val songsTitle = stringResource(id = R.string.page_library_songs)
    val favTitle = stringResource(id = R.string.page_library_playlists_fav_title)

    Column(
        modifier
            .width(SideBarWidth)
            .fillMaxHeight()
            .background(Color(0xFFF2F2F7) withNight Color(0xFF0B0B0D))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
            .padding(top = 18.dp, bottom = 12.dp)
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "Flamingo", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp, bottom = 14.dp)
            )
            SideItem(painterResource(id = R.drawable.flamingo_icon), stringResource(id = R.string.page_home_title), active == "home") {
                opened.value = "home"
                onHome()
            }
            SideHeader(stringResource(id = R.string.page_library_title))
            SideItem(painterResource(id = R.drawable.ic_library_link_icon_playlists), stringResource(id = R.string.page_library_playlists), active == "playlists") {
                go("playlists", UI.PlayLists)
            }
            SideItem(painterResource(id = R.drawable.ic_library_link_icon_artists), stringResource(id = R.string.page_library_artists), active == "artists") {
                go("artists", UI.LocalArtists)
            }
            SideItem(painterResource(id = R.drawable.ic_library_link_icon_album), stringResource(id = R.string.page_library_albums), active == "albums") {
                go("albums", UI.LocalAlbums)
            }
            SideItem(painterResource(id = R.drawable.ic_library_link_icon_songs), songsTitle, active == "songs") {
                LibraryObject.setTargetListWithTitle(songsTitle, MusicLibrary.songs)
                go("songs", UI.NormalMusic)
            }
            SideItem(painterResource(id = R.drawable.ic_library_link_icon_history), stringResource(id = R.string.page_library_history), active == "history") {
                go("history", UI.History)
            }
            SideHeader(stringResource(id = R.string.page_library_playlists))
            SideItem(null, favTitle, active == "fav") {
                LibraryObject.setTargetListWithTitle(favTitle, FavPlayListLibrary.favPlayList)
                go("fav", UI.NormalMusic)
            }
            PlayListLibrary.playList.forEach { list ->
                val key = "list:" + list.listID
                SideItem(null, list.name, active == key, cover = {
                    PlayListCover(list.songDataList, Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(4.dp)))
                }) {
                    val songs = MusicLibrary.songs
                    LibraryObject.setTargetListWithTitle(
                        list.name,
                        list.songDataList.mapNotNull { uri -> songs.firstOrNull { it.uri == uri } },
                        keepOrder = true
                    )
                    go(key, UI.NormalMusic)
                }
            }
        }
        SideItem(rememberVectorPainter(Icons.Rounded.Settings), stringResource(id = R.string.page_settings_title), active == "settings") {
            go("settings", UI.Settings.Main)
        }
    }
}


@Composable
private fun SideHeader(text: String) = Text(
    text.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp,
    modifier = Modifier
        .padding(start = 10.dp, top = 16.dp, bottom = 4.dp)
        .alpha(0.5f)
)

@Composable
private fun SideItem(
    icon: Painter?,
    label: String,
    selected: Boolean,
    cover: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) = Row(
    Modifier
        .fillMaxWidth()
        .height(if (icon != null) 44.dp else 40.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(if (selected) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f) else Color.Transparent)
        .semantics { this.selected = selected }
        .clickable(onClick = onClick)
        .padding(horizontal = 10.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    when {
        icon != null -> Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        cover != null -> cover()
        else -> Box(
            Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) { Text("★", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp) }
    }
    Text(
        label, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier.padding(start = 12.dp)
    )
}
