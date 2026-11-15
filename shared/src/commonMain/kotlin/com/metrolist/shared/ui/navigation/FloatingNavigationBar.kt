@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.shared.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.shared.ui.components.focusOutline
import com.metrolist.shared.settings.Settings
import kotlin.math.abs

val LocalFloatingNavBar = compositionLocalOf { false }

private val FloatingBarHeight = 64.dp
private val FloatingItemHeight = 48.dp
private val FloatingBarShadow = 6.dp
private val NavSpring = spring<Float>(dampingRatio = 0.9f, stiffness = 700f)

@Composable
internal fun <T> FloatingNavigation(
    items: List<AdaptiveNavigationItem<T>>,
    selectedKey: T?,
    onSelect: (T) -> Unit,
    navigationVisible: Boolean,
    separateKey: T?,
    itemModifier: @Composable (T) -> Modifier,
    onBarHeightChanged: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val iosAnimationsEnabled = Settings.shared.veloraIosStyleAnimations
    AnimatedVisibility(
        visible = navigationVisible,
        modifier = modifier,
        enter =
            if (iosAnimationsEnabled) {
                expandVertically(
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 700f),
                    expandFrom = Alignment.Bottom,
                )
            } else {
                EnterTransition.None
            },
        exit =
            if (iosAnimationsEnabled) {
                shrinkVertically(
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 700f),
                    shrinkTowards = Alignment.Bottom,
                )
            } else {
                ExitTransition.None
            },
    ) {
        FloatingNavigationBar(
            items = items,
            selectedKey = selectedKey,
            onSelect = onSelect,
            separateKey = separateKey,
            itemModifier = itemModifier,
            modifier =
                Modifier.onSizeChanged { size ->
                    onBarHeightChanged(with(density) { size.height.toDp() })
                },
        )
    }
}

@Composable
private fun <T> FloatingNavigationBar(
    items: List<AdaptiveNavigationItem<T>>,
    selectedKey: T?,
    onSelect: (T) -> Unit,
    separateKey: T?,
    itemModifier: @Composable (T) -> Modifier,
    modifier: Modifier = Modifier,
) {
    val iosAnimationsEnabled = Settings.shared.veloraIosStyleAnimations
    val pillItems = items.filter { it.key != separateKey }
    val separateItem = items.firstOrNull { it.key == separateKey }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { 48.dp.toPx() }

    Row(
        modifier =
            modifier
                .wrapContentWidth()
                .windowInsetsPadding(ShortNavigationBarDefaults.windowInsets)
                .pointerInput(items, selectedKey) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragDistance += dragAmount
                        },
                        onDragEnd = {
                            val currentIndex = items.indexOfFirst { it.key == selectedKey }
                            if (currentIndex >= 0 && abs(dragDistance) >= swipeThreshold) {
                                val direction = if (dragDistance < 0f) 1 else -1
                                val nextIndex = (currentIndex + direction).coerceIn(items.indices)
                                if (nextIndex != currentIndex) onSelect(items[nextIndex].key)
                            }
                            dragDistance = 0f
                        },
                        onDragCancel = { dragDistance = 0f },
                    )
                }
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.wrapContentWidth().height(FloatingBarHeight),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = FloatingBarShadow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
        ) {
            Row(
                modifier = Modifier.wrapContentWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pillItems.forEach { item ->
                    FloatingNavigationItem(
                        item = item,
                        selected = item.key == selectedKey,
                        onClick = { onSelect(item.key) },
                        modifier = itemModifier(item.key),
                    )
                }
            }
        }

        if (separateItem != null) {
            val selected = separateItem.key == selectedKey
            val targetContainer =
                if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
            val animatedContainer by animateColorAsState(targetContainer, label = "floatingSearchContainer")
            val container = if (iosAnimationsEnabled) animatedContainer else targetContainer
            val targetContent =
                if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            val animatedContent by animateColorAsState(targetContent, label = "floatingSearchContent")
            val content = if (iosAnimationsEnabled) animatedContent else targetContent
            Surface(
                onClick = { onSelect(separateItem.key) },
                modifier = Modifier.size(FloatingBarHeight).then(itemModifier(separateItem.key)),
                shape = CircleShape,
                color = container,
                contentColor = content,
                shadowElevation = FloatingBarShadow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = if (selected) separateItem.selectedIcon else separateItem.icon,
                        contentDescription = separateItem.label,
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> FloatingNavigationItem(
    item: AdaptiveNavigationItem<T>,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iosAnimationsEnabled = Settings.shared.veloraIosStyleAnimations
    val targetIndicator =
        if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0f)
    val animatedIndicator by animateColorAsState(targetIndicator, label = "floatingItemIndicator")
    val indicator = if (iosAnimationsEnabled) animatedIndicator else targetIndicator
    val targetContentColor =
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val animatedContentColor by animateColorAsState(targetContentColor, label = "floatingItemContent")
    val contentColor = if (iosAnimationsEnabled) animatedContentColor else targetContentColor
    Row(
        modifier =
            modifier
                .height(FloatingItemHeight)
                .clip(CircleShape)
                .background(indicator)
                .focusOutline(CircleShape)
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.Tab,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                ).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = if (iosAnimationsEnabled) fadeIn() + expandHorizontally(spring(dampingRatio = 0.9f, stiffness = 700f)) else EnterTransition.None,
            exit = if (iosAnimationsEnabled) fadeOut() + shrinkHorizontally(spring(dampingRatio = 0.9f, stiffness = 700f)) else ExitTransition.None,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = item.selectedIcon, contentDescription = null, tint = contentColor)
                Spacer(Modifier.width(8.dp))
            }
        }
        Text(
            text = item.label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
