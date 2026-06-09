package com.noztek.xend.feature.message.domain.model

data class ChatConversationHeaderModel(
    val title: String,
    val subtitle: String? = null,
    val partnerUserId: String? = null,
)
