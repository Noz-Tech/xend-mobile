package com.noztek.xend.core.notify

interface PushTokenProvider {
    suspend fun getTokenOrNull(): String?
}
