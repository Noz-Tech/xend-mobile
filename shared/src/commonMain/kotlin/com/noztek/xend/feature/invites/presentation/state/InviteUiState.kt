package com.noztek.xend.feature.invites.presentation.state

import com.noztek.xend.feature.invites.domain.model.SentInviteModel

data class InviteUiState(
    val identifier: String = "",
    val note: String = "",
    val isLoading: Boolean = false,
    val inviteId: String? = null,
    val message: String? = null,
    val sentInvites: List<SentInviteModel> = emptyList(),
)
