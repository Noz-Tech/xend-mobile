package com.noztek.xend.feature.incominginvite.presentation.state

import com.noztek.xend.feature.incominginvite.domain.model.IncomingInviteDetailsModel

data class IncomingInviteUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val invite: IncomingInviteDetailsModel? = null,
    val message: String? = null,
    val shouldEnterMain: Boolean = false,
    val shouldReturnToSpaceSetup: Boolean = false,
)
