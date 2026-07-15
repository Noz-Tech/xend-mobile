package com.noztek.xend.feature.settings.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.Camera
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Gift
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.InformationCircle
import com.composables.icons.heroicons.outline.Link
import com.composables.icons.heroicons.outline.LockClosed
import com.composables.icons.heroicons.outline.Pencil
import com.composables.icons.heroicons.outline.User
import com.noztek.xend.core.ui.theme.XendPalette
import com.noztek.xend.core.ui.theme.XendTheme
import org.jetbrains.compose.resources.painterResource
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.couple
import xend.shared.generated.resources.couple_cover

@Composable
fun CoupleSettingsScreen(
    onBackClick: () -> Unit,
) {
    val palette = XendTheme.palette
    var monthsaryEnabled by remember { mutableStateOf(true) }
    var anniversaryEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        CoupleSettingsHeader(
            palette = palette,
            onBackClick = onBackClick,
        )

        CoupleProfileHero(palette = palette)

        CoupleSettingsSection(
            title = "Relationship",
            icon = Heroicons.Outline.Heart,
            palette = palette,
        ) {
            CoupleSettingsRow(
                icon = Heroicons.Outline.CalendarDays,
                title = "Relationship Start Date",
                subtitle = "May 20, 2024",
                palette = palette,
            )
        }

        CelebrationSettingsCard(
            monthsaryEnabled = monthsaryEnabled,
            anniversaryEnabled = anniversaryEnabled,
            onMonthsaryChanged = { monthsaryEnabled = it },
            onAnniversaryChanged = { anniversaryEnabled = it },
            palette = palette,
        )

        CoupleSettingsSection(
            title = "Connection",
            icon = Heroicons.Outline.Link,
            palette = palette,
        ) {
            ConnectionSettingsGroup(palette = palette)
        }

        CoupleSettingsSection(
            title = "Space Management",
            icon = Heroicons.Outline.Gift,
            palette = palette,
        ) {
            SpaceManagementGroup(palette = palette)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Heroicons.Outline.LockClosed,
                contentDescription = null,
                tint = palette.primary.copy(alpha = 0.62f),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Taking a break or breakup is private.\nYour data will be handled with care.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.mutedInk,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun CoupleSettingsHeader(
    palette: XendPalette,
    onBackClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(42.dp),
            shape = CircleShape,
            color = palette.surface,
            shadowElevation = 0.5.dp,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Heroicons.Outline.ArrowLeft,
                    contentDescription = "Back",
                    tint = palette.ink,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Couple Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
            Text(
                text = "Manage your space together \uD83D\uDC9C",
                style = MaterialTheme.typography.bodySmall,
                color = palette.mutedInk,
            )
        }
    }
}

@Composable
private fun CoupleProfileHero(
    palette: XendPalette,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.9f),
            ) {
                Image(
                    painter = painterResource(Res.drawable.couple_cover),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.18f),
                                ),
                            ),
                        ),
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(44.dp),
                    shape = CircleShape,
                    color = palette.surface.copy(alpha = 0.94f),
                    shadowElevation = 4.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Heroicons.Outline.Pencil,
                            contentDescription = "Edit cover",
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Surface(
                    modifier = Modifier
                        .offset(y = (-52).dp)
                        .size(92.dp)
                        .zIndex(1f),
                    shape = CircleShape,
                    color = palette.surface,
                    border = BorderStroke(4.dp, palette.surface),
                    shadowElevation = 4.dp,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.couple),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 32.dp, y = 14.dp)
                        .size(36.dp)
                        .zIndex(2f),
                    shape = CircleShape,
                    color = palette.surface,
                    shadowElevation = 3.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Heroicons.Outline.Camera,
                            contentDescription = "Change photo",
                            tint = palette.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Alex & Sam",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.ink,
                        )
                        Icon(
                            imageVector = Heroicons.Outline.Pencil,
                            contentDescription = "Edit name",
                            tint = palette.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = "Together since May 20, 2024",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.mutedInk,
                    )
                }
            }
        }
    }
}

@Composable
private fun CoupleSettingsSection(
    title: String,
    icon: ImageVector,
    palette: XendPalette,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.primary.copy(alpha = 0.78f),
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.mutedInk,
            )
        }
        content()
    }
}

@Composable
private fun CelebrationSettingsCard(
    monthsaryEnabled: Boolean,
    anniversaryEnabled: Boolean,
    onMonthsaryChanged: (Boolean) -> Unit,
    onAnniversaryChanged: (Boolean) -> Unit,
    palette: XendPalette,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                SettingsIconBox(
                    icon = Heroicons.Outline.Gift,
                    iconColor = Color(0xFFE84C83),
                    background = Color(0xFFFFEEF5),
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = "Celebrations",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.ink,
                    )
                    Text(
                        text = "Choose what you want to celebrate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.mutedInk,
                    )
                }
            }
            ToggleRow(
                icon = Heroicons.Outline.Heart,
                iconColor = Color(0xFFE84C83),
                iconBackground = Color(0xFFFFEEF5),
                title = "Monthsary",
                subtitle = "Celebrate every month",
                checked = monthsaryEnabled,
                onCheckedChange = onMonthsaryChanged,
                palette = palette,
            )
            HorizontalDivider(color = palette.outline.copy(alpha = 0.62f), modifier = Modifier.padding(start = 54.dp))
            ToggleRow(
                icon = Heroicons.Outline.Gift,
                iconColor = Color(0xFFE59822),
                iconBackground = Color(0xFFFFF4DF),
                title = "Anniversary",
                subtitle = "Celebrate every year",
                checked = anniversaryEnabled,
                onCheckedChange = onAnniversaryChanged,
                palette = palette,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.InformationCircle,
                    contentDescription = null,
                    tint = palette.primary.copy(alpha = 0.72f),
                    modifier = Modifier.size(17.dp),
                )
                Text(
                    text = "We'll remind you on your special days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.primary.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun CoupleSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    palette: XendPalette,
    iconColor: Color = palette.primary.copy(alpha = 0.78f),
    iconBackground: Color = palette.primarySoft,
    titleColor: Color = palette.ink,
    containerColor: Color = palette.surface,
    chevronColor: Color = palette.softInk,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        shadowElevation = 0.5.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsIconBox(
                icon = icon,
                iconColor = iconColor,
                background = iconBackground,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = titleColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.mutedInk,
                )
            }
            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
                tint = chevronColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ConnectionSettingsGroup(palette: XendPalette) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column {
            StatusRow(palette = palette)
            HorizontalDivider(
                color = palette.outline.copy(alpha = 0.62f),
                modifier = Modifier.padding(start = 66.dp),
            )
            PartnerProfileRow(palette = palette)
        }
    }
}

@Composable
private fun SpaceManagementGroup(palette: XendPalette) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column {
            ManagementActionRow(
                icon = Heroicons.Outline.LockClosed,
                title = "Take a Break",
                subtitle = "Pause your space for a while",
                palette = palette,
                iconColor = Color(0xFFE59822).copy(alpha = 0.42f),
                iconBackground = Color(0xFFFFF4DF),
            )
            HorizontalDivider(
                color = palette.outline.copy(alpha = 0.62f),
                modifier = Modifier.padding(start = 66.dp),
            )
            ManagementActionRow(
                icon = Heroicons.Outline.Heart,
                title = "Breakup",
                subtitle = "End your space permanently",
                palette = palette,
                iconColor = Color(0xFFE33163).copy(alpha = 0.42f),
                iconBackground = Color(0xFFFFE8F0),
                titleColor = Color(0xFFB41638).copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun ManagementActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    palette: XendPalette,
    iconColor: Color,
    iconBackground: Color,
    titleColor: Color = palette.ink,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsIconBox(
            icon = icon,
            iconColor = iconColor,
            background = iconBackground,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = titleColor.copy(alpha = 0.58f),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.mutedInk.copy(alpha = 0.62f),
            )
        }
        Icon(
            imageVector = Heroicons.Outline.LockClosed,
            contentDescription = "Locked",
            tint = palette.softInk.copy(alpha = 0.62f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun StatusRow(palette: XendPalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsIconBox(
            icon = Heroicons.Outline.Link,
            iconColor = palette.primary.copy(alpha = 0.78f),
            background = palette.primarySoft,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "Space Status",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
            Text(
                text = "You're connected",
                style = MaterialTheme.typography.bodySmall,
                color = palette.mutedInk,
            )
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color(0xFFEAF8ED),
            border = BorderStroke(1.dp, Color(0xFFCFEFD6)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2DBE4F)),
                )
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E9F3E),
                )
            }
        }
    }
}

@Composable
private fun PartnerProfileRow(palette: XendPalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsIconBox(
            icon = Heroicons.Outline.User,
            iconColor = palette.primary.copy(alpha = 0.78f),
            background = palette.primarySoft,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "Partner Profile",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
            Text(
                text = "View your partner's profile",
                style = MaterialTheme.typography.bodySmall,
                color = palette.mutedInk,
            )
        }
        Image(
            painter = painterResource(Res.drawable.couple),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            tint = palette.softInk,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    iconColor: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    palette: XendPalette,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsIconBox(
            icon = icon,
            iconColor = iconColor,
            background = iconBackground,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.mutedInk,
            )
        }
        CompactToggle(
            checked = checked,
            onCheckedChange = onCheckedChange,
            palette = palette,
        )
    }
}

@Composable
private fun CompactToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    palette: XendPalette,
) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(24.dp)
            .clip(CircleShape)
            .background(if (checked) palette.primary else palette.outline.copy(alpha = 0.72f))
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(palette.surface),
        )
    }
}

@Composable
private fun SettingsIconBox(
    icon: ImageVector,
    iconColor: Color,
    background: Color,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(21.dp),
        )
    }
}
