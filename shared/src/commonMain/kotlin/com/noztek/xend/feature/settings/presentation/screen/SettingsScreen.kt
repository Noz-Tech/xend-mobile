package com.noztek.xend.feature.settings.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowRightOnRectangle
import com.composables.icons.heroicons.outline.Bell
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.InformationCircle
import com.composables.icons.heroicons.outline.Link
import com.composables.icons.heroicons.outline.Moon
import com.composables.icons.heroicons.outline.QuestionMarkCircle
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.User
import com.noztek.xend.core.ui.theme.XendPalette
import com.noztek.xend.core.ui.theme.XendTheme
import com.noztek.xend.feature.settings.presentation.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.couple

@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    onCoupleSettingsClick: () -> Unit,
    viewModel: SettingsViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val palette = XendTheme.palette

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLoggedOut()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        SettingsHeader(palette = palette)

        CoupleSpaceSettingsRow(
            name = state.coupleSpaceTitle,
            supportingText = state.coupleSpaceSubtitle,
            palette = palette,
            onClick = onCoupleSettingsClick,
        )

        SettingsSection(
            title = "Preferences",
            palette = palette,
            items = listOf(
                SettingsItem(
                    title = "Notifications",
                    subtitle = "Manage push notifications and reminders",
                    icon = Heroicons.Outline.Bell,
                ),
                SettingsItem(
                    title = "App Appearance",
                    subtitle = "Choose light or dark mode",
                    icon = Heroicons.Outline.Moon,
                ),
                SettingsItem(
                    title = "Daily Reset Time",
                    subtitle = "Set when your day resets",
                    icon = Heroicons.Outline.Clock,
                    trailingText = "12:00 AM",
                ),
                SettingsItem(
                    title = "Mood & Ritual Preferences",
                    subtitle = "Customize moods and ritual topics",
                    icon = Heroicons.Outline.Heart,
                ),
                SettingsItem(
                    title = "Privacy",
                    subtitle = "Manage privacy and data settings",
                    icon = Heroicons.Outline.ShieldCheck,
                ),
            ),
        )

        SettingsSection(
            title = "Account",
            palette = palette,
            items = listOf(
                SettingsItem(
                    title = "Account",
                    subtitle = "Update your profile and account details",
                    icon = Heroicons.Outline.User,
                ),
                SettingsItem(
                    title = "Invite Partner",
                    subtitle = "Share invite link or code",
                    icon = Heroicons.Outline.Link,
                ),
                SettingsItem(
                    title = "Log Out",
                    subtitle = "Sign out from your account",
                    icon = Heroicons.Outline.ArrowRightOnRectangle,
                    onClick = viewModel::logout,
                    isLoading = state.isLoading,
                ),
            ),
        )

        SettingsSection(
            title = "About",
            palette = palette,
            items = listOf(
                SettingsItem(
                    title = "About Xend",
                    subtitle = "Learn more about the app",
                    icon = Heroicons.Outline.InformationCircle,
                ),
                SettingsItem(
                    title = "Help & Support",
                    subtitle = "FAQs, contact us, and support",
                    icon = Heroicons.Outline.QuestionMarkCircle,
                ),
                SettingsItem(
                    title = "Terms & Policies",
                    subtitle = "Terms of service and privacy policy",
                    icon = Heroicons.Outline.DocumentText,
                ),
            ),
        )

        if (!state.message.isNullOrBlank()) {
            Text(
                text = state.message.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = "App version 1.0.0",
            style = MaterialTheme.typography.labelMedium,
            color = palette.softInk,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun SettingsHeader(palette: XendPalette) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.ink,
        )
        Text(
            text = "Manage your account, preferences and more.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.mutedInk,
        )
    }
}

@Composable
private fun CoupleSpaceSettingsRow(
    name: String,
    supportingText: String,
    palette: XendPalette,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        shadowElevation = 0.5.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.couple),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.ink,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = palette.primarySoft.copy(alpha = 0.78f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Heroicons.Outline.Link,
                                contentDescription = null,
                                tint = palette.primary.copy(alpha = 0.72f),
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = "Connected",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.primary.copy(alpha = 0.76f),
                            )
                        }
                    }
                }
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.mutedInk,
                )
            }

            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
                tint = palette.softInk,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    palette: XendPalette,
    items: List<SettingsItem>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.mutedInk,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 4.dp),
        )
        Column {
            items.forEachIndexed { index, item ->
                SettingsRow(item = item, palette = palette)
                if (index != items.lastIndex) {
                    HorizontalDivider(
                        color = palette.outline.copy(alpha = 0.54f),
                        modifier = Modifier.padding(start = 56.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    item: SettingsItem,
    palette: XendPalette,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.onClick != null && !item.isLoading) { item.onClick?.invoke() }
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = palette.mutedInk,
                modifier = Modifier.size(21.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.mutedInk,
            )
        }

        if (item.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = palette.primary,
            )
        } else {
            if (!item.trailingText.isNullOrBlank()) {
                Text(
                    text = item.trailingText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.mutedInk,
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
                tint = palette.softInk,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private data class SettingsItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val trailingText: String? = null,
    val onClick: (() -> Unit)? = null,
    val isLoading: Boolean = false,
)
