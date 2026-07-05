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
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeftEllipsis
import com.composables.icons.heroicons.outline.Home
import com.composables.icons.heroicons.outline.User

data class BottomBarItem(
    val title: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit,
)

enum class RootBottomBarTab {
    Space,
    Chat,
    Profile,
}

@Composable
fun rememberRootBottomBarItems(
    selectedTab: RootBottomBarTab?,
    onSpaceClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit,
): List<BottomBarItem> {
    return remember(selectedTab, onSpaceClick, onChatClick, onProfileClick) {
        listOf(
            BottomBarItem(
                title = "Space",
                icon = Heroicons.Outline.Home,
                selected = selectedTab == RootBottomBarTab.Space,
                onClick = onSpaceClick,
            ),
            BottomBarItem(
                title = "Chat",
                icon = Heroicons.Outline.ChatBubbleOvalLeftEllipsis,
                selected = selectedTab == RootBottomBarTab.Chat,
                onClick = onChatClick,
            ),
            BottomBarItem(
                title = "Profile",
                icon = Heroicons.Outline.User,
                selected = selectedTab == RootBottomBarTab.Profile,
                onClick = onProfileClick,
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
