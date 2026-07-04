package com.noztek.xend.feature.spacesetup.presentation.state

data class SpaceSetupUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val partnerCode: String = "",
    val ownIdentifier: String = "",
    val displayName: String = "",
    val pendingIncomingInvites: Int = 0,
    val pendingSentInvites: Int = 0,
    val message: String? = null,
    val shouldOpenIncomingInvite: Boolean = false,
    val shouldOpenOutgoingInvite: Boolean = false,
    val shouldEnterMain: Boolean = false,
) {
    val isSubmitEnabled: Boolean
        get() = !isLoading && !isSubmitting && partnerCode.isNotBlank()
}
