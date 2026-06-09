package com.noztek.xend.feature.invites.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.feature.invites.presentation.viewmodel.InvitePartnerViewModel
import org.koin.compose.koinInject

@Composable
fun InvitePartnerScreen() {
    val vm = koinInject<InvitePartnerViewModel>()
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppTextField(
            value = state.identifier,
            onValueChange = { vm.onIdentifierChanged(it.uppercase()) },
            label = "Partner Identifier",
            singleLine = true,
        )

        AppTextField(
            value = state.note,
            onValueChange = vm::onNoteChanged,
            label = "Note (optional)",
            singleLine = false,
            maxLines = 3,
        )

        if (!state.message.isNullOrBlank()) {
            Text(
                text = state.message!!,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (state.sentInvites.isNotEmpty()) {
            Text(
                text = "Sent Invites",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp),
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.sentInvites) { invite ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = invite.inviteeIdentifierOrUserId,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Status: ${invite.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom,
        ) {
            AppButton(
                text = if (state.isLoading) "Sending..." else "Send Invite",
                onClick = vm::submit,
                enabled = !state.isLoading,
                modifier = Modifier.width(148.dp),
                isLoading = state.isLoading,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
