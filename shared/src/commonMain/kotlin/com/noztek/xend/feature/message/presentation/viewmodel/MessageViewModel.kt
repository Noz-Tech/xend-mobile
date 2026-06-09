package com.noztek.xend.feature.message.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeEvent
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.feature.message.domain.usecase.LoadConversationEntryUseCase
import com.noztek.xend.feature.message.domain.usecase.RetryConversationMessageUseCase
import com.noztek.xend.feature.message.domain.usecase.SendConversationMessageUseCase
import com.noztek.xend.feature.message.domain.usecase.SendConversationTypingUseCase
import com.noztek.xend.feature.message.domain.usecase.ToggleConversationReactionUseCase
import com.noztek.xend.feature.message.presentation.state.MessageUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

class MessageViewModel(
    private val loadConversationEntry: LoadConversationEntryUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
    private val retryConversationMessage: RetryConversationMessageUseCase,
    private val sendConversationMessage: SendConversationMessageUseCase,
    private val sendConversationTyping: SendConversationTypingUseCase,
    private val toggleConversationReaction: ToggleConversationReactionUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(MessageUiState())
    val state: StateFlow<MessageUiState> = _state.asStateFlow()
    private var activeConversationId: String? = null

    init {
        scope.launch {
            realtimeSignals.messageEvents.collect { event ->
                val conversationId = activeConversationId ?: return@collect
                when (event.type) {
                    "message_created",
                    "message_receipt_updated",
                    "message_reaction_updated",
                    -> refresh(conversationId)

                    "typing",
                    "presence_updated",
                    -> handleRealtimeEvent(conversationId, event)
                }
            }
        }
    }

    fun load(conversationId: String) {
        activeConversationId = conversationId
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching {
                loadConversationEntry(conversationId)
            }.onSuccess { entry ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        currentUserId = entry.currentUserId,
                        header = entry.header,
                        items = entry.messages,
                        presenceLabel = entry.presenceLabel,
                        isTyping = false,
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, message = error.message ?: "Failed to load messages") }
            }
        }
    }

    fun send(conversationId: String, text: String, replyToMessageId: String?, onSent: () -> Unit = {}) {
        scope.launch {
            _state.update { it.copy(sending = true, message = null) }
            runCatching {
                sendConversationMessage(conversationId, text, replyToMessageId)
            }.onSuccess { messages ->
                _state.update { it.copy(items = messages, sending = false) }
                onSent()
            }.onFailure { error ->
                _state.update { it.copy(sending = false, message = error.message ?: "Failed to send message") }
            }
        }
    }

    fun refresh(conversationId: String) {
        activeConversationId = conversationId
        load(conversationId)
    }

    fun sendTyping(conversationId: String, isTyping: Boolean) {
        sendConversationTyping(conversationId, isTyping)
    }

    fun handleRealtimeEvent(conversationId: String, event: RealtimeEvent) {
        val partnerUserId = state.value.header?.partnerUserId ?: return
        when (event.type) {
            "typing" -> {
                if (event.payload["conversation_id"] != conversationId) return
                if (event.payload["sender_user_id"] != partnerUserId) return
                _state.update { it.copy(isTyping = event.payload["is_typing"] == "true") }
            }
            "presence_updated" -> {
                if (event.payload["user_id"] != partnerUserId) return
                val isOnline = event.payload["is_online"] == "true"
                val lastSeen = event.payload["updated_at"]?.toLongOrNull()
                _state.update {
                    it.copy(
                        presenceLabel = formatPresenceLabel(isOnline, lastSeen),
                        isTyping = if (isOnline) it.isTyping else false,
                    )
                }
            }
        }
    }

    fun retry(conversationId: String, messageId: String) {
        scope.launch {
            _state.update { it.copy(retryingMessageId = messageId, message = null) }
            runCatching {
                retryConversationMessage(conversationId, messageId)
            }.onSuccess { messages ->
                _state.update { it.copy(items = messages, retryingMessageId = null) }
            }.onFailure { error ->
                _state.update { it.copy(retryingMessageId = null, message = error.message ?: "Failed to retry message") }
            }
        }
    }

    fun toggleReaction(messageId: String, emoji: String, remove: Boolean, onError: (String) -> Unit = {}) {
        scope.launch {
            runCatching { toggleConversationReaction(messageId, emoji, remove) }
                .onFailure { onError(it.message ?: "Failed to update reaction") }
        }
    }

    private fun formatPresenceLabel(isOnline: Boolean, lastSeenEpochSeconds: Long?): String {
        if (isOnline) return "online"
        if (lastSeenEpochSeconds == null) return "offline"

        val now = Clock.System.now().epochSeconds
        val deltaSeconds = (now - lastSeenEpochSeconds).coerceAtLeast(0)
        return when {
            deltaSeconds < 60 -> "last seen just now"
            deltaSeconds < 3600 -> "last seen ${deltaSeconds / 60}m ago"
            deltaSeconds < 86400 -> "last seen ${deltaSeconds / 3600}h ago"
            else -> {
                val day = Instant.fromEpochSeconds(lastSeenEpochSeconds).toString().substring(0, 10)
                "last seen $day"
            }
        }
    }
}
