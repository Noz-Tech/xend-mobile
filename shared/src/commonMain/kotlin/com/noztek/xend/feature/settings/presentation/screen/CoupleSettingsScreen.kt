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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.composables.icons.heroicons.outline.LockClosed
import com.composables.icons.heroicons.outline.Pencil
import com.composables.icons.heroicons.outline.User
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.core.ui.media.rememberImagePickerLauncher
import com.noztek.xend.core.ui.theme.XendPalette
import com.noztek.xend.core.ui.theme.XendTheme
import com.noztek.xend.feature.settings.presentation.viewmodel.CoupleSettingsViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Clock

@Composable
fun CoupleSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: CoupleSettingsViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val palette = XendTheme.palette
    var isNameDialogOpen by remember { mutableStateOf(false) }
    var isStartDateDialogOpen by remember { mutableStateOf(false) }
    val coupleName = state.space?.name?.takeIf { it.isNotBlank() } ?: "Couple Space"
    val relationshipStartDate = state.space?.relationshipStartDate.orEmpty()
    val relationshipStartDateText = formatRelationshipStartDate(
        isoDate = relationshipStartDate,
        fallbackEpochSeconds = state.space?.createdAtEpochSeconds,
    )
    val coverPhotoPicker = rememberImagePickerLauncher(
        onPicked = viewModel::uploadCover,
    )
    val couplePhotoPicker = rememberImagePickerLauncher(
        onPicked = viewModel::uploadCouplePhoto,
    )

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

        CoupleProfileHero(
            coupleName = coupleName,
            coverPhoto = state.coverPhoto,
            couplePhoto = state.couplePhoto,
            isLoadingCoverPhoto = state.isLoadingCoverPhoto,
            isUploadingCoverPhoto = state.isUploadingCoverPhoto,
            isUploadingCouplePhoto = state.isUploadingCouplePhoto,
            relationshipStartDateText = relationshipStartDateText,
            palette = palette,
            onEditCoverClick = coverPhotoPicker,
            onEditPhotoClick = couplePhotoPicker,
            onEditNameClick = { isNameDialogOpen = true },
        )

        RelationshipSettingsGroup(
            coupleName = coupleName,
            relationshipStartDateText = relationshipStartDateText,
            palette = palette,
            onNameClick = { isNameDialogOpen = true },
            onStartDateClick = { isStartDateDialogOpen = true },
        )

        CelebrationSettingsCard(
            monthsaryEnabled = state.space?.celebrateMonthsary ?: true,
            anniversaryEnabled = state.space?.celebrateAnniversary ?: true,
            savingKey = state.savingCelebrationKey,
            onMonthsaryChanged = { viewModel.saveCelebrationSettings(celebrateMonthsary = it) },
            onAnniversaryChanged = { viewModel.saveCelebrationSettings(celebrateAnniversary = it) },
            palette = palette,
        )

        state.message?.takeIf { it.isNotBlank() }?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = palette.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = viewModel::consumeMessage),
                textAlign = TextAlign.Center,
            )
        }

        SpaceManagementGroup(palette = palette)

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

    if (isNameDialogOpen) {
        CoupleNameDialog(
            currentName = coupleName,
            isSaving = state.isSavingName,
            palette = palette,
            onDismiss = { isNameDialogOpen = false },
            onSave = { name ->
                viewModel.saveName(name)
                isNameDialogOpen = false
            },
        )
    }
    if (isStartDateDialogOpen) {
        RelationshipStartDateDialog(
            currentDate = relationshipStartDate,
            isSaving = state.isSavingRelationshipStartDate,
            palette = palette,
            onDismiss = { isStartDateDialogOpen = false },
            onSave = { date ->
                viewModel.saveRelationshipStartDate(date)
                isStartDateDialogOpen = false
            },
        )
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
    coupleName: String,
    coverPhoto: ImageBitmap?,
    couplePhoto: ImageBitmap?,
    isLoadingCoverPhoto: Boolean,
    isUploadingCoverPhoto: Boolean,
    isUploadingCouplePhoto: Boolean,
    relationshipStartDateText: String,
    palette: XendPalette,
    onEditCoverClick: () -> Unit,
    onEditPhotoClick: () -> Unit,
    onEditNameClick: () -> Unit,
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
                if (coverPhoto != null) {
                    Image(
                        bitmap = coverPhoto,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else if (isLoadingCoverPhoto) {
                    CoupleCoverLoadingPlaceholder(palette = palette)
                } else {
                    CoupleCoverPlaceholder(palette = palette)
                }
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
                    onClick = onEditCoverClick,
                    shape = CircleShape,
                    color = palette.surface.copy(alpha = 0.94f),
                    shadowElevation = 4.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUploadingCoverPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = palette.primary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Heroicons.Outline.Pencil,
                                contentDescription = "Edit cover",
                                tint = palette.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
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
                    if (couplePhoto != null) {
                        Image(
                            bitmap = couplePhoto,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        CouplePhotoPlaceholder(palette = palette)
                    }
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 32.dp, y = 14.dp)
                        .size(36.dp)
                        .zIndex(2f),
                    onClick = onEditPhotoClick,
                    shape = CircleShape,
                    color = palette.surface,
                    shadowElevation = 3.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUploadingCouplePhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = palette.primary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Heroicons.Outline.Camera,
                                contentDescription = "Change photo",
                                tint = palette.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
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
                        modifier = Modifier.clickable(onClick = onEditNameClick),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = coupleName,
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
                        text = "Together since $relationshipStartDateText",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.mutedInk,
                    )
                }
            }
        }
    }
}

@Composable
private fun CoupleCoverPlaceholder(palette: XendPalette) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        palette.primarySoft,
                        Color(0xFFFFF5F8),
                        Color(0xFFF6F3FF),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Heroicons.Outline.Heart,
            contentDescription = null,
            tint = palette.primary.copy(alpha = 0.42f),
            modifier = Modifier.size(34.dp),
        )
    }
}

@Composable
private fun CoupleCoverLoadingPlaceholder(palette: XendPalette) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        palette.surfaceSoft,
                        palette.primarySoft,
                        palette.surfaceRaised,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(26.dp),
            color = palette.primary,
            strokeWidth = 2.5.dp,
        )
    }
}

@Composable
private fun CouplePhotoPlaceholder(palette: XendPalette, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.primarySoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Heroicons.Outline.Heart,
            contentDescription = null,
            tint = palette.primary.copy(alpha = 0.68f),
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun CoupleNameDialog(
    currentName: String,
    isSaving: Boolean,
    palette: XendPalette,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var draft by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        title = {
            Text(
                text = "Couple Name",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
        },
        text = {
            AppTextField(
                value = draft,
                onValueChange = { draft = it.take(32) },
                label = "Babe, Honey, Lovebirds...",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(draft.trim()) },
                enabled = draft.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = Color.White,
                ),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = palette.mutedInk,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RelationshipStartDateDialog(
    currentDate: String,
    isSaving: Boolean,
    palette: XendPalette,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val todayMillis = remember { Clock.System.now().toEpochMilliseconds() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.toUtcMillisOrNull() ?: todayMillis,
    )
    val selectedMillis = datePickerState.selectedDateMillis
    val isValid = selectedMillis != null && selectedMillis <= todayMillis

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    selectedMillis?.toIsoDateOrNull()?.let(onSave)
                },
                enabled = isValid && !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = Color.White,
                ),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = palette.mutedInk,
                )
            }
        },
        colors = androidx.compose.material3.DatePickerDefaults.colors(
            containerColor = palette.surface,
        ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Relationship Start Date",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.ink,
                    )
                },
            )
            if (selectedMillis != null && selectedMillis > todayMillis) {
                Text(
                    text = "Choose today or an earlier date.",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.primary,
                )
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
    savingKey: String?,
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
            ToggleRow(
                icon = Heroicons.Outline.Heart,
                iconColor = palette.primary.copy(alpha = 0.82f),
                iconBackground = palette.primarySoft,
                title = "Monthsary",
                subtitle = "Celebrate every month",
                checked = monthsaryEnabled,
                enabled = savingKey != CELEBRATION_MONTHSARY_KEY,
                onCheckedChange = onMonthsaryChanged,
                palette = palette,
            )
            HorizontalDivider(color = palette.outline.copy(alpha = 0.62f), modifier = Modifier.padding(start = 54.dp))
            ToggleRow(
                icon = Heroicons.Outline.Gift,
                iconColor = palette.orange.copy(alpha = 0.86f),
                iconBackground = palette.orangeSoft,
                title = "Anniversary",
                subtitle = "Celebrate every year",
                checked = anniversaryEnabled,
                enabled = savingKey != CELEBRATION_ANNIVERSARY_KEY,
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
private fun RelationshipSettingsGroup(
    coupleName: String,
    relationshipStartDateText: String,
    palette: XendPalette,
    onNameClick: () -> Unit,
    onStartDateClick: () -> Unit,
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
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Relationship",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink,
                )
                Text(
                    text = "Set your couple name and start date.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.mutedInk,
                )
            }
            Column {
                RelationshipSettingsRow(
                    icon = Heroicons.Outline.Pencil,
                    title = "Couple Name",
                    subtitle = coupleName,
                    palette = palette,
                    onClick = onNameClick,
                )
                HorizontalDivider(
                    color = palette.outline.copy(alpha = 0.62f),
                    modifier = Modifier.padding(start = 52.dp),
                )
                RelationshipSettingsRow(
                    icon = Heroicons.Outline.CalendarDays,
                    title = "Relationship Start Date",
                    subtitle = relationshipStartDateText,
                    palette = palette,
                    onClick = onStartDateClick,
                )
            }
        }
    }
}

@Composable
private fun RelationshipSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    palette: XendPalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsIconBox(
            icon = icon,
            iconColor = palette.primary.copy(alpha = 0.78f),
            background = palette.primarySoft,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
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
        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            tint = palette.softInk,
            modifier = Modifier.size(20.dp),
        )
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
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsGroupHeader(
                title = "Space Management",
                subtitle = "Locked options for changing your space.",
                palette = palette,
            )
            Column {
                ManagementActionRow(
                    icon = Heroicons.Outline.LockClosed,
                    title = "Take a Break",
                    subtitle = "Pause your space for a while",
                    palette = palette,
                    iconColor = palette.orange.copy(alpha = 0.42f),
                    iconBackground = palette.orangeSoft.copy(alpha = 0.62f),
                )
                HorizontalDivider(
                    color = palette.outline.copy(alpha = 0.62f),
                    modifier = Modifier.padding(start = 52.dp),
                )
                ManagementActionRow(
                    icon = Heroicons.Outline.Heart,
                    title = "Breakup",
                    subtitle = "End your space permanently",
                    palette = palette,
                    iconColor = palette.primary.copy(alpha = 0.42f),
                    iconBackground = palette.primarySoft.copy(alpha = 0.62f),
                    titleColor = palette.primary.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun SettingsGroupHeader(
    title: String,
    subtitle: String,
    palette: XendPalette,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
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
            .padding(vertical = 12.dp),
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
            verticalArrangement = Arrangement.spacedBy(3.dp),
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
private fun ToggleRow(
    icon: ImageVector,
    iconColor: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
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
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            palette = palette,
        )
    }
}

@Composable
private fun CompactToggle(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    palette: XendPalette,
) {
    val trackColor = when {
        !enabled -> palette.outline.copy(alpha = 0.42f)
        checked -> palette.primary
        else -> palette.outline.copy(alpha = 0.72f)
    }
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(24.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
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

private fun formatRelationshipStartDate(isoDate: String, fallbackEpochSeconds: Long?): String {
    val date = parseIsoDateOrNull(isoDate)
        ?: fallbackEpochSeconds?.let { epoch ->
            Instant.fromEpochSeconds(epoch)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        }
        ?: return "Set date"
    return "${date.monthDisplayName()} ${date.day}, ${date.year}"
}

private fun parseIsoDateOrNull(value: String): LocalDate? {
    return runCatching { LocalDate.parse(value) }.getOrNull()
}

private fun String.toUtcMillisOrNull(): Long? {
    return parseIsoDateOrNull(this)
        ?.atStartOfDayIn(TimeZone.UTC)
        ?.toEpochMilliseconds()
}

private fun Long.toIsoDateOrNull(): String? {
    return runCatching {
        Instant.fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.UTC)
            .date
            .toString()
    }.getOrNull()
}

private fun LocalDate.monthDisplayName(): String = when (month) {
    Month.JANUARY -> "January"
    Month.FEBRUARY -> "February"
    Month.MARCH -> "March"
    Month.APRIL -> "April"
    Month.MAY -> "May"
    Month.JUNE -> "June"
    Month.JULY -> "July"
    Month.AUGUST -> "August"
    Month.SEPTEMBER -> "September"
    Month.OCTOBER -> "October"
    Month.NOVEMBER -> "November"
    Month.DECEMBER -> "December"
}

private const val CELEBRATION_MONTHSARY_KEY = "monthsary"
private const val CELEBRATION_ANNIVERSARY_KEY = "anniversary"
