package com.noztek.xend.core.realtime

import com.noztek.xend.feature.device.domain.usecase.BootstrapSignalSessionsOnAcceptUseCase
import com.noztek.xend.feature.message.domain.usecase.SyncMessagesUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AndroidRealtimeFeatureHost(
    private val eventBus: RealtimeEventBus,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val bootstrapSignalSessions: BootstrapSignalSessionsOnAcceptUseCase,
    private val syncMessages: SyncMessagesUseCase,
) : RealtimeFeatureSignals {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _inviteRefreshTick = MutableStateFlow(0L)
    private val _spaceRefreshTick = MutableStateFlow(0L)
    private val _messageEvents = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 32)

    override val inviteRefreshTick: StateFlow<Long> = _inviteRefreshTick
    override val spaceRefreshTick: StateFlow<Long> = _spaceRefreshTick
    override val messageEvents: SharedFlow<RealtimeEvent> = _messageEvents.asSharedFlow()

    fun start() {
        scope.launch {
            eventBus.events.collect { event ->
                when (event.type) {
                    "realtime_connected" -> {
                        runCatching { syncRelationshipSpaces() }
                        runCatching { bootstrapSignalSessions() }
                        bumpInviteTick()
                        bumpSpaceTick()
                        runCatching { syncMessages() }
                        _messageEvents.tryEmit(RealtimeEvent(type = "message_sync_completed"))
                    }

                    "relationship_invite_received",
                    "relationship_invite_declined",
                    -> {
                        bumpInviteTick()
                    }

                    "relationship_invite_accepted" -> {
                        bumpInviteTick()
                        runCatching { syncRelationshipSpaces() }
                        event.payload["user_id"]
                            ?.takeIf { it.isNotBlank() }
                            ?.let { userId -> runCatching { bootstrapSignalSessions(listOf(userId)) } }
                        bumpSpaceTick()
                        runCatching { syncMessages() }
                    }

                    "daily_checkin_updated" -> {
                        runCatching { syncRelationshipSpaces() }
                        bumpSpaceTick()
                    }

                    "challenge_received",
                    "challenge_accepted",
                    "challenge_declined",
                    -> {
                        bumpSpaceTick()
                    }

                    "challenge_completed" -> {
                        runCatching { syncRelationshipSpaces() }
                        bumpSpaceTick()
                    }

                    "message_created",
                    "message_receipt_updated",
                    "message_reaction_updated",
                    -> {
                        runCatching { syncMessages() }
                        _messageEvents.tryEmit(event)
                    }

                    "typing",
                    "presence_updated",
                    -> {
                        _messageEvents.tryEmit(event)
                    }
                }
            }
        }
    }

    private fun bumpInviteTick() {
        _inviteRefreshTick.value = _inviteRefreshTick.value + 1
    }

    private fun bumpSpaceTick() {
        _spaceRefreshTick.value = _spaceRefreshTick.value + 1
    }
}
