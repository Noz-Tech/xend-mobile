package com.noztek.xend.feature.challenges.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.challenges.domain.model.ChallengeAssignmentModel
import com.noztek.xend.feature.challenges.domain.model.ChallengeAudience
import com.noztek.xend.feature.challenges.domain.model.ChallengeCategory
import com.noztek.xend.feature.challenges.domain.model.ChallengeSubmissionType
import com.noztek.xend.feature.challenges.domain.model.ChallengeTemplateModel
import com.noztek.xend.feature.challenges.domain.usecase.AcceptChallengeUseCase
import com.noztek.xend.feature.challenges.domain.usecase.CompleteChallengeUseCase
import com.noztek.xend.feature.challenges.domain.usecase.DeclineChallengeUseCase
import com.noztek.xend.feature.challenges.domain.usecase.GetChallengeSubmissionImageUseCase
import com.noztek.xend.feature.challenges.domain.usecase.GetChallengesOverviewUseCase
import com.noztek.xend.feature.challenges.domain.usecase.SendChallengeUseCase
import com.noztek.xend.feature.challenges.presentation.state.ChallengesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChallengesViewModel(
    private val getOverview: GetChallengesOverviewUseCase,
    private val sendChallenge: SendChallengeUseCase,
    private val acceptChallenge: AcceptChallengeUseCase,
    private val declineChallenge: DeclineChallengeUseCase,
    private val completeChallenge: CompleteChallengeUseCase,
    private val getSubmissionImage: GetChallengeSubmissionImageUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(ChallengesUiState())
    val state: StateFlow<ChallengesUiState> = _state.asStateFlow()

    init {
        refresh()
        scope.launch {
            realtimeSignals.spaceRefreshTick.collect { tick ->
                if (tick > 0) refresh()
            }
        }
    }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { getOverview() }
                .onSuccess { overview ->
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            overview = overview,
                            selectedAudience = when {
                                current.overview != null -> current.selectedAudience
                                overview.incoming.isNotEmpty() -> ChallengeAudience.ForYou
                                else -> current.selectedAudience
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = error.message ?: "Failed to load challenges.",
                        )
                    }
                }
        }
    }

    fun onAudienceSelected(audience: ChallengeAudience) {
        _state.update { it.copy(selectedAudience = audience) }
    }

    fun onCategorySelected(category: ChallengeCategory) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun onNoteDraftChanged(value: String) {
        _state.update { it.copy(noteDraft = value) }
    }

    fun onResponseDraftChanged(value: String) {
        _state.update { it.copy(responseDraft = value) }
    }

    fun openSendComposer(template: ChallengeTemplateModel) {
        _state.update {
            it.copy(
                selectedTemplate = template,
                noteDraft = "",
                message = null,
            )
        }
    }

    fun dismissSendComposer() {
        _state.update {
            it.copy(
                selectedTemplate = null,
                noteDraft = "",
            )
        }
    }

    fun openTextCompletion(challenge: ChallengeAssignmentModel) {
        _state.update {
            it.copy(
                selectedCompletionChallenge = challenge,
                responseDraft = "",
                message = null,
            )
        }
    }

    fun prepareImageCompletion(challenge: ChallengeAssignmentModel) {
        _state.update {
            it.copy(
                selectedCompletionChallenge = challenge,
                message = null,
            )
        }
    }

    fun dismissCompletionComposer() {
        _state.update {
            it.copy(
                selectedCompletionChallenge = null,
                responseDraft = "",
            )
        }
    }

    fun onMessageConsumed() {
        _state.update { it.copy(message = null) }
    }

    fun showMessage(message: String) {
        _state.update { it.copy(message = message) }
    }

    fun loadSubmissionImage(challengeId: String) {
        val current = _state.value
        if (current.submissionImages.containsKey(challengeId) || current.loadingSubmissionImageIds.contains(challengeId)) {
            return
        }

        scope.launch {
            _state.update { it.copy(loadingSubmissionImageIds = it.loadingSubmissionImageIds + challengeId) }
            runCatching { getSubmissionImage(challengeId) }
                .onSuccess { image ->
                    _state.update {
                        it.copy(
                            submissionImages = it.submissionImages + (challengeId to image.bitmap),
                            loadingSubmissionImageIds = it.loadingSubmissionImageIds - challengeId,
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            loadingSubmissionImageIds = it.loadingSubmissionImageIds - challengeId,
                        )
                    }
                }
        }
    }

    fun sendSelectedChallenge() {
        val template = _state.value.selectedTemplate ?: return
        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching {
                sendChallenge(
                    templateId = template.templateId,
                    note = _state.value.noteDraft.trim().ifBlank { null },
                )
            }.onSuccess { overview ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        overview = overview,
                        selectedTemplate = null,
                        noteDraft = "",
                        selectedAudience = ChallengeAudience.ForThem,
                        message = "Challenge sent to ${overview.partnerName}.",
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = error.message ?: "Failed to send challenge.",
                    )
                }
            }
        }
    }

    fun accept(challengeId: String) {
        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { acceptChallenge(challengeId) }
                .onSuccess { overview ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            overview = overview,
                            selectedAudience = ChallengeAudience.ForYou,
                            message = "Challenge accepted.",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = error.message ?: "Failed to accept challenge.",
                        )
                    }
                }
        }
    }

    fun decline(challengeId: String) {
        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { declineChallenge(challengeId) }
                .onSuccess { overview ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            overview = overview,
                            selectedAudience = ChallengeAudience.ForYou,
                            message = "Challenge declined.",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = error.message ?: "Failed to decline challenge.",
                        )
                    }
                }
        }
    }

    fun completeWithoutSubmission(challengeId: String) {
        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { completeChallenge(challengeId = challengeId) }
                .onSuccess { overview ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            overview = overview,
                            selectedCompletionChallenge = null,
                            responseDraft = "",
                            selectedAudience = ChallengeAudience.ForYou,
                            message = "Challenge completed.",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = error.message ?: "Failed to complete challenge.",
                        )
                    }
                }
        }
    }

    fun submitTextCompletion() {
        val challenge = _state.value.selectedCompletionChallenge ?: return
        if (challenge.submissionType != ChallengeSubmissionType.Text) return

        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching {
                completeChallenge(
                    challengeId = challenge.challengeId,
                    textResponse = _state.value.responseDraft.trim(),
                )
            }.onSuccess { overview ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        overview = overview,
                        selectedCompletionChallenge = null,
                        responseDraft = "",
                        selectedAudience = ChallengeAudience.ForYou,
                        message = "Challenge completed.",
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = error.message ?: "Failed to submit challenge response.",
                    )
                }
            }
        }
    }

    fun submitImageCompletion(image: PickedImageData) {
        val challenge = _state.value.selectedCompletionChallenge ?: return
        if (challenge.submissionType != ChallengeSubmissionType.Image) return

        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching {
                completeChallenge.submitImage(
                    challengeId = challenge.challengeId,
                    image = image,
                )
            }.onSuccess { overview ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        overview = overview,
                        selectedCompletionChallenge = null,
                        selectedAudience = ChallengeAudience.ForYou,
                        message = "Challenge completed.",
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = error.message ?: "Failed to upload challenge photo.",
                    )
                }
            }
        }
    }
}
