@file:Suppress("FunctionName")

package com.metrolist.shared.ui.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.shared.db.ListeningStreakStats
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.db.historyChanges
import com.metrolist.shared.db.completedListeningStreakDates
import com.metrolist.shared.db.listeningStreakStats
import com.metrolist.shared.db.listeningStreakRecoveryStatus
import com.metrolist.shared.db.recoverListeningStreak
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
internal fun MobileListeningStreakScreen(
    database: MusicDatabase,
    onBack: () -> Unit,
    bottomContentPadding: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    var stats by remember(database) { mutableStateOf<ListeningStreakStats?>(null) }
    var activity by remember(database) { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var loading by remember(database) { mutableStateOf(true) }
    var failed by remember(database) { mutableStateOf(false) }
    var recoveryDialog by remember { mutableStateOf(false) }
    var recoveryUsed by remember { mutableStateOf(0) }
    var recoveryRemaining by remember { mutableStateOf(5) }
    var canRecover by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(database) {
        database.historyChanges().collectLatest {
            loading = true
            failed = false
            try {
                val now = Clock.System.now()
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                activity = completedListeningStreakDates().associate { it.toString() to 1 }
                stats = database.listeningStreakStats()
                listeningStreakRecoveryStatus(today).also {
                    recoveryUsed = it.usedThisMonth
                    recoveryRemaining = it.remainingThisMonth
                    canRecover = it.canRecover
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                failed = true
            } finally {
                loading = false
            }
        }
    }

    val current = stats?.currentDays ?: 0
    val longest = stats?.longestDays ?: 0
    val currentColor = streakMilestoneColor(current)
    val longestColor = streakMilestoneColor(longest)
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val firstDay = today.minus(364 + today.dayOfWeek.ordinal, DateTimeUnit.DAY)
    val calendarDays = (0 until 371).map { firstDay.plus(it, DateTimeUnit.DAY) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = bottomContentPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Text("‹", style = MaterialTheme.typography.headlineLarge)
            }
            Text("Listening streak", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentColor.copy(alpha = 0.42f)),
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🔥", style = MaterialTheme.typography.displayMedium)
                Text("$current days", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = currentColor)
                Text(
                    if (stats?.listenedToday == true) "You're keeping the streak alive today" else "Play a song today to keep your streak going",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Milestone: ${streakMilestoneLabel(maxOf(current, longest))}",
                    style = MaterialTheme.typography.labelLarge,
                    color = currentColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StreakMetricCard("Personal best", "$longest days", longestColor, Modifier.weight(1f))
            StreakMetricCard("Active days · 12 mo", "${activity.size} days", currentColor, Modifier.weight(1f))
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Activity calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.68f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Last 12 months", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        calendarDays.chunked(7).forEach { week ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                week.forEach { date ->
                                    val count = activity[date.toString()] ?: 0
                                    Box(
                                        Modifier.size(10.dp).clip(RoundedCornerShape(3.dp))
                                            .background(activityColor(count, currentColor)),
                                    )
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf(0, 1, 3, 6).forEach { level ->
                            Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(activityColor(level, currentColor)))
                        }
                        Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Streak milestones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val milestones = listOf(3, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.68f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    milestones.forEach { milestone ->
                        val earned = longest >= milestone
                        val milestoneColor = streakMilestoneColor(milestone)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                Modifier.size(34.dp).clip(CircleShape)
                                    .background(if (earned) milestoneColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(if (earned) "🔥" else "·", color = if (earned) milestoneColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("$milestone days", fontWeight = FontWeight.SemiBold, color = if (earned) milestoneColor else MaterialTheme.colorScheme.onSurface)
                                Text(if (earned) "Unlocked" else "${(milestone - longest).coerceAtLeast(0)} days to go", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (earned) Text("✓", color = milestoneColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (canRecover || recoveryUsed > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.68f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Streak recovery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$recoveryRemaining of 5 recoveries remaining this month", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (canRecover) {
                        TextButton(onClick = { recoveryDialog = true }) { Text("Pulihkan Streak") }
                    } else if (recoveryRemaining == 0) {
                        Text("Monthly recovery limit reached", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (recoveryDialog) {
            AlertDialog(
                onDismissRequest = { recoveryDialog = false },
                title = { Text("Pulihkan streak?") },
                text = { Text("Sekali pemulihan akan menyambungkan kembali streak yang terputus dan menggunakan 1 dari 5 jatah bulan ini.") },
                confirmButton = {
                    TextButton(onClick = {
                        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                        if (recoverListeningStreak(today)) {
                            recoveryUsed = listeningStreakRecoveryStatus(today).usedThisMonth
                            recoveryRemaining = 5 - recoveryUsed
                            canRecover = false
                            scope.launch { stats = database.listeningStreakStats() }
                        }
                        recoveryDialog = false
                    }) { Text("Pulihkan") }
                },
                dismissButton = { TextButton(onClick = { recoveryDialog = false }) { Text("Batal") } },
            )
        }

        if (loading) Text("Updating listening activity…", modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (failed) Text("Couldn't load listening activity. Try again later.", modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun StreakMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.68f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun activityColor(count: Int, accent: Color): Color =
    when {
        count <= 0 -> Color(0xFF777777).copy(alpha = 0.18f)
        count == 1 -> accent.copy(alpha = 0.35f)
        count <= 3 -> accent.copy(alpha = 0.58f)
        else -> accent
    }

private fun streakMilestoneLabel(days: Int): String =
    when {
        days >= 1000 -> "1,000-day legend"
        days >= 900 -> "900-day master"
        days >= 800 -> "800-day elite"
        days >= 700 -> "700-day elite"
        days >= 600 -> "600-day champion"
        days >= 500 -> "500-day champion"
        days >= 400 -> "400-day legend"
        days >= 300 -> "300-day legend"
        days >= 200 -> "200-day pro"
        days >= 100 -> "100-day pro"
        days >= 50 -> "50-day rising star"
        days >= 10 -> "10-day spark"
        days >= 3 -> "3-day starter"
        else -> "First steps"
    }

private fun streakMilestoneColor(days: Int): Color =
    when {
        days >= 1000 -> Color(0xFF3D5AFE)
        days >= 900 -> Color(0xFF3F51B5)
        days >= 800 -> Color(0xFF2196F3)
        days >= 700 -> Color(0xFF03A9F4)
        days >= 600 -> Color(0xFF00BCD4)
        days >= 500 -> Color(0xFF009688)
        days >= 400 -> Color(0xFF4CAF50)
        days >= 300 -> Color(0xFF8BC34A)
        days >= 200 -> Color(0xFFCDDC39)
        days >= 100 -> Color(0xFFFFC107)
        days >= 50 -> Color(0xFFFF9800)
        days >= 10 -> Color(0xFFFF5722)
        days >= 3 -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }
