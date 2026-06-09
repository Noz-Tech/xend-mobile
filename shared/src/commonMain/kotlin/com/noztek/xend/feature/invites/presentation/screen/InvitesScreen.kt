package com.noztek.xend.feature.invites.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppOutlinedButton
import com.noztek.xend.feature.invites.presentation.viewmodel.InvitesViewModel
import org.koin.compose.koinInject

@Composable
fun InvitesScreen() {
    val vm = koinInject<InvitesViewModel>()
    val state by vm.state.collectAsState()
    val pendingInboxInvites = state.inboxInvites.filter { it.status == "pending" }
    val pendingSentInvites = state.sentInvites.filter { it.status == "pending" }

    LaunchedEffect(Unit) { vm.refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.isLoading && pendingInboxInvites.isEmpty() && pendingSentInvites.isEmpty()) {
            Text("Loading invites...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }
        if (!state.message.isNullOrBlank()) {
            Text(state.message!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (pendingInboxInvites.isEmpty() && pendingSentInvites.isEmpty()) {
            Text("No invites.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pendingInboxInvites.isNotEmpty()) {
                item {
                    Text(
                        text = "Incoming Invites",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                items(pendingInboxInvites) { invite ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(invite.inviterDisplayName, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "@${invite.inviterIdentifier}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (!invite.note.isNullOrBlank()) {
                            Text(
                                text = invite.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            AppButton(
                                text = "Accept",
                                onClick = { vm.acceptInvite(invite.inviteId) },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            AppOutlinedButton(
                                text = "Decline",
                                onClick = { vm.declineInvite(invite.inviteId) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            if (pendingSentInvites.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Sent Invites",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(pendingSentInvites) { invite ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(invite.inviteeIdentifierOrUserId, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "Status: ${invite.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
