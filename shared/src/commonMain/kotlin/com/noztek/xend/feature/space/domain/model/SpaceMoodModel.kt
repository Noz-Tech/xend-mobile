package com.noztek.xend.feature.space.domain.model

data class SpaceMoodModel(
    val relationshipSpaceId: String,
    val userId: String,
    val displayName: String,
    val moodKey: String?,
    val emoji: String?,
    val label: String?,
    val updatedAtEpochSeconds: Long?,
    val isMe: Boolean,
) {
    val displayMood: String?
        get() {
            val emojiValue = emoji?.takeIf { it.isNotBlank() }
            val labelValue = label?.takeIf { it.isNotBlank() }
            return when {
                emojiValue != null && labelValue != null -> "$emojiValue $labelValue"
                labelValue != null -> labelValue
                emojiValue != null -> emojiValue
                else -> null
            }
        }
}
