package com.noztek.xend.feature.challenges.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.noztek.xend.feature.challenges.domain.model.ChallengeAssignmentModel
import com.noztek.xend.feature.challenges.domain.model.ChallengeAudience
import com.noztek.xend.feature.challenges.domain.model.ChallengeCategory
import com.noztek.xend.feature.challenges.domain.model.ChallengeTemplateModel
import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel

data class ChallengesUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val overview: ChallengesOverviewModel? = null,
    val selectedAudience: ChallengeAudience = ChallengeAudience.ForThem,
    val selectedCategory: ChallengeCategory = ChallengeCategory.All,
    val selectedTemplate: ChallengeTemplateModel? = null,
    val selectedCompletionChallenge: ChallengeAssignmentModel? = null,
    val noteDraft: String = "",
    val responseDraft: String = "",
    val submissionImages: Map<String, ImageBitmap> = emptyMap(),
    val loadingSubmissionImageIds: Set<String> = emptySet(),
    val message: String? = null,
)
