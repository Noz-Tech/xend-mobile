package com.noztek.xend.core.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeftEllipsis
import com.composables.icons.heroicons.outline.Gift
import com.composables.icons.heroicons.outline.Home
import com.composables.icons.heroicons.outline.Sparkles

data class BottomBarItem(
    val title: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit,
)

enum class RootBottomBarTab {
    Space,
    Rituals,
    Games,
    Challenges,
    Chat,
}

@Composable
fun rememberRootBottomBarItems(
    selectedTab: RootBottomBarTab?,
    onSpaceClick: () -> Unit,
    onRitualsClick: () -> Unit,
    onGamesClick: () -> Unit,
    onChallengesClick: () -> Unit,
    onChatClick: () -> Unit,
): List<BottomBarItem> {
    return remember(selectedTab, onSpaceClick, onRitualsClick, onGamesClick, onChallengesClick, onChatClick) {
        listOf(
            BottomBarItem(
                title = "Space",
                icon = Heroicons.Outline.Home,
                selected = selectedTab == RootBottomBarTab.Space,
                onClick = onSpaceClick,
            ),
            BottomBarItem(
                title = "Rituals",
                icon = Heroicons.Outline.CalendarDays,
                selected = selectedTab == RootBottomBarTab.Rituals,
                onClick = onRitualsClick,
            ),
            BottomBarItem(
                title = "Games",
                icon = Heroicons.Outline.Sparkles,
                selected = selectedTab == RootBottomBarTab.Games,
                onClick = onGamesClick,
            ),
            BottomBarItem(
                title = "Challenges",
                icon = Heroicons.Outline.Gift,
                selected = selectedTab == RootBottomBarTab.Challenges,
                onClick = onChallengesClick,
            ),
            BottomBarItem(
                title = "Chat",
                icon = Heroicons.Outline.ChatBubbleOvalLeftEllipsis,
                selected = selectedTab == RootBottomBarTab.Chat,
                onClick = onChatClick,
            ),
        )
    }
}

@Composable
fun AppBottomBar(
    items: List<BottomBarItem>,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    NavigationBar(
        containerColor = containerColor,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                    )
                },
                label = {
                    Text(text = item.title)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                ),
            )
        }
    }
}
