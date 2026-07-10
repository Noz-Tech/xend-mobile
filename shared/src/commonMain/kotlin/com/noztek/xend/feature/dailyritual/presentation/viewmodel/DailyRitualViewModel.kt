package com.noztek.xend.feature.dailyritual.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.dailyritual.domain.usecase.GetDailyRitualOverviewUseCase
import com.noztek.xend.feature.dailyritual.domain.usecase.SubmitDailyRitualUseCase
import com.noztek.xend.feature.dailyritual.presentation.state.DailyRitualUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailyRitualViewModel(
    private val getOverview: GetDailyRitualOverviewUseCase,
    private val submitDailyRitual: SubmitDailyRitualUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(DailyRitualUiState())
    val state: StateFlow<DailyRitualUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { getOverview() }
                .onSuccess { overview ->
                    _state.update { it.copy(isLoading = false, overview = overview) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, message = error.message ?: "Failed to load rituals") }
                }
        }
    }

    fun onResponseDraftChanged(value: String) {
        _state.update { it.copy(responseDraft = value) }
    }

    fun openResponseComposer() {
        _state.update { it.copy(isResponseComposerVisible = true, message = null) }
    }

    fun dismissResponseComposer() {
        _state.update { it.copy(isResponseComposerVisible = false, responseDraft = "") }
    }

    fun onMessageConsumed() {
        _state.update { it.copy(message = null) }
    }

    fun showMessage(message: String) {
        _state.update { it.copy(message = message) }
    }

    fun submitTodayRitual(assignmentId: String, textResponse: String? = null) {
        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { submitDailyRitual(assignmentId = assignmentId, textResponse = textResponse) }
                .onSuccess { overview ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            overview = overview,
                            isResponseComposerVisible = false,
                            responseDraft = "",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = error.message ?: "Failed to submit ritual",
                        )
                    }
                }
        }
    }

    fun submitTodayRitualImage(assignmentId: String, image: PickedImageData) {
        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { submitDailyRitual.submitImage(assignmentId = assignmentId, image = image) }
                .onSuccess { overview ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            overview = overview,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = error.message ?: "Failed to submit ritual image",
                        )
                    }
                }
        }
    }
}
