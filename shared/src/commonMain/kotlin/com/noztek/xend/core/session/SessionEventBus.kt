package com.noztek.xend.core.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class SessionEventType {
    Expired,
}

data class SessionEvent(
    val type: SessionEventType,
    val reason: String,
)

class SessionEventBus {
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    fun publish(type: SessionEventType, reason: String) {
        _events.tryEmit(SessionEvent(type = type, reason = reason))
    }
}
