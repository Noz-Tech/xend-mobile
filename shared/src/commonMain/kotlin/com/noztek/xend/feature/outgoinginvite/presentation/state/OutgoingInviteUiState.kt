package com.noztek.xend.feature.outgoinginvite.presentation.state

import com.noztek.xend.feature.outgoinginvite.domain.model.OutgoingInviteDetailsModel

data class OutgoingInviteUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val invite: OutgoingInviteDetailsModel? = null,
    val message: String? = null,
    val shouldEnterMain: Boolean = false,
    val shouldReturnToSpaceSetup: Boolean = false,
)
