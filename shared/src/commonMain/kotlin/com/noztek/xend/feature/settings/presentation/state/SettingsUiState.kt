package com.noztek.xend.feature.settings.presentation.state

import com.noztek.xend.feature.auth.domain.model.UserProfileModel

data class SettingsUiState(
    val isLoading: Boolean = false,
    val profile: UserProfileModel? = null,
    val coupleSpaceTitle: String = "Couple Space",
    val coupleSpaceSubtitle: String = "Manage your shared space settings.",
    val message: String? = null,
    val isLoggedOut: Boolean = false,
)
