package com.noztek.xend.core.realtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

interface RealtimeFeatureSignals {
    val inviteRefreshTick: StateFlow<Long>
    val spaceRefreshTick: StateFlow<Long>
    val moodRefreshTick: StateFlow<Long>
    val messageEvents: Flow<RealtimeEvent>
}

class NoopRealtimeFeatureSignals : RealtimeFeatureSignals {
    private val tick = MutableStateFlow(0L)

    override val inviteRefreshTick: StateFlow<Long> = tick
    override val spaceRefreshTick: StateFlow<Long> = tick
    override val moodRefreshTick: StateFlow<Long> = tick
    override val messageEvents: Flow<RealtimeEvent> = emptyFlow()
}
