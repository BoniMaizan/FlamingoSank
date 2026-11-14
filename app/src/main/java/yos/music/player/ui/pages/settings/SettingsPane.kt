package yos.music.player.ui.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import yos.music.player.ui.UI
import yos.music.player.ui.pages.isWideLayout
import yos.music.player.ui.theme.withNight

// Tablets: the Settings list stays on the left and the open page shows on the right; phones show the page alone
@Composable
fun SettingsPane(navController: NavController, route: String, page: @Composable () -> Unit) {
    if (!isWideLayout()) {
        if (route == UI.Settings.Main) Settings(navController) else page()
        return
    }
    Row(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .width(380.dp)
                .fillMaxHeight()
        ) {
            // Main has no page of its own: show the first one beside the list
            Settings(navController, selected = if (route == UI.Settings.Main) UI.Settings.LibraryOverview else route)
        }
        Box(
            Modifier
                .width(0.5.dp)
                .fillMaxHeight()
                .background((Color.Black withNight Color.White).copy(alpha = 0.12f))
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
        ) { page() }
    }
}
