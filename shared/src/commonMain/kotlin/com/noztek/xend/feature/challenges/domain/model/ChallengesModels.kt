package com.noztek.xend.feature.challenges.domain.model

import androidx.compose.ui.graphics.ImageBitmap

data class ChallengesOverviewModel(
    val relationshipSpaceId: String,
    val partnerName: String,
    val templates: List<ChallengeTemplateModel>,
    val incoming: List<ChallengeAssignmentModel>,
    val sent: List<ChallengeAssignmentModel>,
    val history: List<ChallengeAssignmentModel>,
)

data class ChallengeTemplateModel(
    val templateId: String,
    val slug: String,
    val title: String,
    val description: String,
    val category: ChallengeCategory,
    val submissionType: ChallengeSubmissionType,
    val rewardPoints: Int,
    val expiryLabel: String?,
)

data class ChallengeAssignmentModel(
    val challengeId: String,
    val templateId: String,
    val title: String,
    val description: String,
    val category: ChallengeCategory,
    val submissionType: ChallengeSubmissionType,
    val senderDisplayName: String,
    val receiverDisplayName: String,
    val rewardPoints: Int,
    val note: String?,
    val status: ChallengeStatus,
    val assignedLevel: Int,
    val expiresAtLabel: String?,
    val createdAtLabel: String,
    val canAccept: Boolean,
    val canDecline: Boolean,
    val canComplete: Boolean,
    val submittedByMe: Boolean,
    val submissionTextResponse: String?,
    val hasSubmissionImage: Boolean,
)

data class ChallengeSubmissionImageModel(
    val challengeId: String,
    val bitmap: ImageBitmap,
)

enum class ChallengeAudience {
    ForYou,
    ForThem,
}

enum class ChallengeCategory {
    All,
    Soft,
    Desire,
    Private,
    Bold,
    Devotion,
}

enum class ChallengeSubmissionType {
    None,
    Text,
    Image,
}

enum class ChallengeStatus {
    Sent,
    Accepted,
    Completed,
    Declined,
    Expired,
    Cancelled,
}
