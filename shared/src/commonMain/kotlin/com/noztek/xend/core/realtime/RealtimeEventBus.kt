package com.noztek.xend.core.realtime

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RealtimeEventBus {
    private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<RealtimeEvent> = _events.asSharedFlow()

    fun publish(type: String, payload: Map<String, String> = emptyMap()) {
        _events.tryEmit(RealtimeEvent(type = type, payload = payload))
    }
}
