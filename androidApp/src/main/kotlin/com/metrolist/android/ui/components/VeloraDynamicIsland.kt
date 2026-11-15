package com.metrolist.android.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.previewThumbnailUrl
import com.metrolist.shared.settings.displayName
import com.metrolist.shared.ui.components.images.AsyncImage

@Composable
fun VeloraDynamicIsland(
    song: SongItem,
    isPlaying: Boolean,
    expanded: Boolean,
    showArtwork: Boolean,
    showControls: Boolean,
    onExpand: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val artist = song.artists.firstOrNull()?.displayName.orEmpty()
    Surface(
        onClick = onExpand,
        modifier = modifier.animateContentSize(spring(dampingRatio = 0.82f, stiffness = 650f)),
        shape = RoundedCornerShape(if (expanded) 28.dp else 24.dp),
        color = Color(0xF20B0B0E),
        contentColor = Color.White,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.13f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = if (expanded) 11.dp else 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showArtwork) {
                AsyncImage(
                    url = song.previewThumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.size(if (expanded) 42.dp else 34.dp).clip(RoundedCornerShape(9.dp)),
                )
            } else {
                Box(
                    modifier = Modifier.size(10.dp).clip(CircleShape).then(
                        Modifier
                    ),
                )
            }
            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (expanded && artist.isNotBlank()) {
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.68f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (showControls) {
                Text(
                    text = if (isPlaying) "Ⅱ" else "▶",
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onTogglePlayback).padding(horizontal = 7.dp, vertical = 5.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "»",
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onSkipNext).padding(horizontal = 6.dp, vertical = 5.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
