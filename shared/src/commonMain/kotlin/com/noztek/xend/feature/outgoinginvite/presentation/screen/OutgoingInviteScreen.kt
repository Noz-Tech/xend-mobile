package com.noztek.xend.feature.outgoinginvite.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noztek.xend.core.ui.components.AppOutlinedButton
import com.noztek.xend.feature.outgoinginvite.presentation.viewmodel.OutgoingInviteViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.couple_1
import xend.shared.generated.resources.logo
import kotlinx.coroutines.delay

private const val OUTGOING_INVITE_POLL_INTERVAL_MS = 5_000L

@Composable
fun OutgoingInviteScreen(
    onAccepted: () -> Unit,
    onReturnedToSetup: () -> Unit,
    viewModel: OutgoingInviteViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val invite = state.invite

    LaunchedEffect(state.shouldEnterMain) {
        if (!state.shouldEnterMain) return@LaunchedEffect
        viewModel.consumeEnterMain()
        onAccepted()
    }

    LaunchedEffect(state.shouldReturnToSpaceSetup) {
        if (!state.shouldReturnToSpaceSetup) return@LaunchedEffect
        viewModel.consumeReturnToSpaceSetup()
        onReturnedToSetup()
    }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message)
        viewModel.consumeMessage()
    }

    LaunchedEffect(viewModel) {
        while (true) {
            delay(OUTGOING_INVITE_POLL_INTERVAL_MS)
            viewModel.refresh(showLoading = false)
        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Xend",
                modifier = Modifier.height(28.dp),
            )
            Spacer(modifier = Modifier.height(34.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                                    ),
                                ),
                            )
                            .padding(34.dp),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.couple_1),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(140.dp)
                                .width(140.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = "Your invite is on its way",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = invite?.let {
                            "We're waiting for @${it.inviteeIdentifier.uppercase()} to accept and enter your private space."
                        } ?: "Loading your invite...",
                        style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                    )
                    if (invite != null) {
                        Spacer(modifier = Modifier.height(26.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(horizontal = 20.dp, vertical = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "@${invite.inviteeIdentifier.uppercase()}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "As soon as they accept, you'll both be brought into your shared space.",
                                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                            if (!invite.note.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${invite.note}\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "You can leave this screen. Xend will bring you into home as soon as your invite is accepted.",
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.56f),
            )
            Spacer(modifier = Modifier.height(18.dp))
            AppOutlinedButton(
                text = "Cancel Invite",
                onClick = viewModel::cancelInvite,
                modifier = Modifier.fillMaxWidth(),
                enabled = invite != null && !state.isSubmitting,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp),
        )
    }
}
