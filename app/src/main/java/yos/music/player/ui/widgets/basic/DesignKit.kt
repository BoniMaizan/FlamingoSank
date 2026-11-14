package yos.music.player.ui.widgets.basic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Building blocks of the mockups: grouped tiles, segmented control and pills

private val TileShape = RoundedCornerShape(12.dp)

@Composable
fun tileColor() = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)

// Rows of a group are 2dp apart, the group itself has round ends
@Composable
fun MenuGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(TileShape),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        content = content
    )

@Composable
fun MenuRow(
    text: String,
    icon: ImageVector? = null,
    trailing: String? = null,
    selected: Boolean = false,
    onClick: () -> Unit
) = Row(
    Modifier
        .fillMaxWidth()
        .height(50.dp)
        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else tileColor())
        .semantics { this.selected = selected }
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    if (icon != null) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Box(Modifier.size(14.dp))
    }
    Text(text, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    if (trailing != null) Text(trailing, fontSize = 13.sp, modifier = Modifier.alpha(0.5f))
    if (selected) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
}

// Header used by the sheets: optional leading picture, bold title, grey line under it
@Composable
fun SheetHeader(title: String, subTitle: String? = null, centered: Boolean = false, leading: (@Composable () -> Unit)? = null) {
    if (centered) Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        leading?.invoke()
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (subTitle != null) Text(subTitle, fontSize = 14.sp, modifier = Modifier.alpha(0.5f))
    } else Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke()
        Column(Modifier.padding(start = if (leading != null) 14.dp else 0.dp)) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subTitle != null) Text(subTitle, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.alpha(0.55f))
        }
    }
}

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) =
    Row(
        modifier
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            val on = index == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (on) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.22f) else Color.Transparent)
                    .semantics { this.selected = on }
                    .clickable(role = Role.Tab) { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label, fontSize = 14.sp, maxLines = 1,
                    fontWeight = if (on) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .alpha(if (on) 1f else 0.6f)
                )
            }
        }
    }

@Composable
fun Pill(text: String, selected: Boolean, onClick: () -> Unit) =
    Box(
        Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            .semantics { this.selected = selected }
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 13.sp, color = if (selected) Color.White else MaterialTheme.colorScheme.onBackground)
    }
