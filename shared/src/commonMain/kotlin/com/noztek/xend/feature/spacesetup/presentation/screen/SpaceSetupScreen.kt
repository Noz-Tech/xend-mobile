package com.noztek.xend.feature.spacesetup.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.DocumentDuplicate
import com.composables.icons.heroicons.outline.PaperAirplane
import com.composables.icons.heroicons.outline.QrCode
import com.composables.icons.heroicons.solid.Check
import com.composables.icons.heroicons.solid.ClipboardDocumentCheck
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.core.ui.qr.generateQrCodeImageBitmap
import com.noztek.xend.core.ui.scan.rememberInviteQrScannerLauncher
import com.noztek.xend.core.ui.share.rememberTextShareLauncher
import com.noztek.xend.feature.spacesetup.presentation.viewmodel.SpaceSetupViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.logo

private const val SPACE_SETUP_POLL_INTERVAL_MS = 5_000L

@Composable
fun SpaceSetupScreen(
    onOpenInvites: () -> Unit = {},
    viewModel: SpaceSetupViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val shareText = rememberTextShareLauncher()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val qrCodeSizePx = remember(density) { with(density) { 180.dp.roundToPx() } }
    var isInviteCodeCopied by remember { mutableStateOf(false) }
    val qrCodeBitmap = remember(state.ownIdentifier, qrCodeSizePx) {
        generateQrCodeImageBitmap(
            content = state.ownIdentifier,
            sizePx = qrCodeSizePx,
        )
    }
    val inviteCodeColor by animateColorAsState(
        targetValue = if (isInviteCodeCopied) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onBackground
        },
        label = "inviteCodeColor",
    )
    val launchInviteQrScanner = rememberInviteQrScannerLauncher(
        onScanned = viewModel::onPartnerCodeChanged,
        onUnavailable = { message ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
        },
    )

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LaunchedEffect(viewModel) {
        while (true) {
            delay(SPACE_SETUP_POLL_INTERVAL_MS)
            viewModel.refresh(showLoading = false)
        }
    }

    LaunchedEffect(isInviteCodeCopied) {
        if (!isInviteCodeCopied) return@LaunchedEffect
        delay(2_000)
        isInviteCodeCopied = false
    }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message)
        viewModel.consumeMessage()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Xend",
                modifier = Modifier.height(28.dp),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Create your space together",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Share your invite code or enter your partner's code to start your private space.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            )

            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    InviteQrPanel(
                        ownIdentifier = state.ownIdentifier,
                        qrCodeBitmap = qrCodeBitmap,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = state.ownIdentifier.ifBlank { "Loading..." },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = inviteCodeColor,
                        )
                        AnimatedVisibility(
                            visible = isInviteCodeCopied && state.ownIdentifier.isNotBlank(),
                            enter = scaleIn(
                                initialScale = 0.75f,
                                animationSpec = spring(),
                            ),
                            exit = scaleOut(
                                targetScale = 0.8f,
                                animationSpec = spring(),
                            ),
                        ) {
                            Icon(
                                imageVector = Heroicons.Solid.ClipboardDocumentCheck,
                                contentDescription = null,
                                tint = inviteCodeColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    if (state.displayName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your partner can use this code to connect with you.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        InviteActionButton(
                            icon = Heroicons.Outline.DocumentDuplicate,
                            label = "Copy",
                            enabled = state.ownIdentifier.isNotBlank(),
                            onClick = {
                                scope.launch {
                                    clipboardManager.setText(AnnotatedString(state.ownIdentifier))
                                    isInviteCodeCopied = true
                                }
                            },
                        )
                        InviteActionButton(
                            icon = Heroicons.Outline.PaperAirplane,
                            label = "Send",
                            enabled = state.ownIdentifier.isNotBlank(),
                            onClick = {
                                shareText(buildInviteShareText(state.ownIdentifier))
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
                OrDivider()
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ScanInviteButton(
                        onClick = launchInviteQrScanner,
                    )
                    AppTextField(
                        value = state.partnerCode,
                        onValueChange = viewModel::onPartnerCodeChanged,
                        label = "Partner invite code",
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                AppButton(
                    text = "Send Invite",
                    onClick = viewModel::submitPartnerCode,
                    enabled = state.isSubmitEnabled,
                    isLoading = state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp),
        )
    }
}

@Composable
private fun ScanInviteButton(
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(50.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f),
                shape = RoundedCornerShape(14.dp),
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Heroicons.Outline.QrCode,
            contentDescription = "Scan invite code",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun InviteQrPanel(
    ownIdentifier: String,
    qrCodeBitmap: ImageBitmap?,
) {
    Box(
        modifier = Modifier
            .size(212.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (qrCodeBitmap != null) {
            Image(
                bitmap = qrCodeBitmap,
                contentDescription = "Invite code QR",
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Heroicons.Outline.QrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                )
                Text(
                    text = if (ownIdentifier.isBlank()) "Preparing your code..." else "QR preview unavailable on this device yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
        }
    }
}

@Composable
private fun InviteActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                ),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.7f else 0.42f),
        )
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        )
        Text(
            text = "or",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.52f),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        )
    }
}

private fun buildInviteShareText(code: String): String {
    return "Join me on Xend. Use my invite code: $code"
}
