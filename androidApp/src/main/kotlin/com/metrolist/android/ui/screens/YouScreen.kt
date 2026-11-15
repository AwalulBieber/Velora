package com.metrolist.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.about
import com.metrolist.shared.generated.resources.equalizer
import com.metrolist.shared.generated.resources.listen_together
import com.metrolist.shared.generated.resources.menu_history
import com.metrolist.shared.generated.resources.recognize_music
import com.metrolist.shared.generated.resources.settings
import com.metrolist.shared.generated.resources.sign_in_to_youtube_music
import com.metrolist.shared.generated.resources.statistics
import com.metrolist.shared.generated.resources.you
import com.metrolist.shared.ui.icons.MetrolistIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun YouScreen(
    isLoggedIn: Boolean,
    onHistory: () -> Unit,
    onStatistics: () -> Unit,
    onListenTogether: () -> Unit,
    onRecognition: () -> Unit,
    onEqualizer: () -> Unit,
    onLogin: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    data class MenuItem(val title: String, val icon: ImageVector, val action: () -> Unit)
    val items =
        buildList {
            add(MenuItem(stringResource(Res.string.menu_history), MetrolistIcons.Outlined.History, onHistory))
            add(MenuItem(stringResource(Res.string.statistics), MetrolistIcons.Outlined.SsidChart, onStatistics))
            add(MenuItem(stringResource(Res.string.listen_together), MetrolistIcons.Outlined.Group, onListenTogether))
            add(MenuItem(stringResource(Res.string.recognize_music), MetrolistIcons.Outlined.Search, onRecognition))
            add(MenuItem(stringResource(Res.string.equalizer), MetrolistIcons.Outlined.Settings, onEqualizer))
            if (!isLoggedIn) {
                add(MenuItem(stringResource(Res.string.sign_in_to_youtube_music), MetrolistIcons.Outlined.Login, onLogin))
            }
            add(MenuItem(stringResource(Res.string.settings), MetrolistIcons.Outlined.Settings, onSettings))
            add(MenuItem(stringResource(Res.string.about), MetrolistIcons.Outlined.Info, onAbout))
        }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = stringResource(Res.string.you),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        )
        LazyColumn {
            items(items) { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    leadingContent = { Icon(item.icon, contentDescription = null) },
                    trailingContent = { Icon(MetrolistIcons.Outlined.ChevronRight, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = item.action),
                )
            }
        }
    }
}
