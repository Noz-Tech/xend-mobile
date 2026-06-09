package com.noztek.xend.feature.invites.presentation.state

import com.noztek.xend.feature.invites.domain.model.ReceivedInviteModel
import com.noztek.xend.feature.invites.domain.model.SentInviteModel

data class InvitesUiState(
    val isLoading: Boolean = true,
    val inboxInvites: List<ReceivedInviteModel> = emptyList(),
    val sentInvites: List<SentInviteModel> = emptyList(),
    val message: String? = null,
)
