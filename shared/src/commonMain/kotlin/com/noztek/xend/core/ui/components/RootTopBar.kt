package com.noztek.xend.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.Bell
import com.composables.icons.heroicons.outline.EllipsisVertical
import com.composables.icons.heroicons.outline.MagnifyingGlass
import com.composables.icons.heroicons.solid.UserCircle
import org.jetbrains.compose.resources.painterResource
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.logo

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RootTopBar(
    title: String = "Xend",
    onAvatarClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onInvitesClick: () -> Unit = {},
    onHiddenSpacesClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    showLogo: Boolean = false,
    showNotification: Boolean = false,
    showSearch: Boolean = true,
    showMenu: Boolean = true,
    showLeading: Boolean = true,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    customActions: (@Composable RowScope.() -> Unit)? = null,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor,
        ),
        title = {
            Text(
                text = title,
                style = titleTextStyle,
            )
        },
        navigationIcon = {
            if (showLeading) {
                if (showLogo) {
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "Xend",
                        modifier = Modifier.size(26.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    IconButton(onClick = onAvatarClick) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Heroicons.Solid.UserCircle,
                                contentDescription = "Avatar",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                }
            }
        },
        actions = {
            if (customActions != null) {
                customActions()
            } else {
                if (showSearch) {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Heroicons.Outline.MagnifyingGlass,
                            contentDescription = "Search",
                        )
                    }
                }
                if (showNotification) {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Heroicons.Outline.Bell,
                            contentDescription = "Notifications",
                        )
                    }
                }
                if (showMenu) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Heroicons.Outline.EllipsisVertical,
                                contentDescription = "More",
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            shape = RoundedCornerShape(12.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 6.dp,
                        ) {
                            DropdownMenuItem(
                                text = { Text("Invites") },
                                onClick = {
                                    menuExpanded = false
                                    onInvitesClick()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Hidden spaces") },
                                onClick = {
                                    menuExpanded = false
                                    onHiddenSpacesClick()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    menuExpanded = false
                                    onSettingsClick()
                                },
                            )
                        }
                    }
                }
            }
        },
    )
}
