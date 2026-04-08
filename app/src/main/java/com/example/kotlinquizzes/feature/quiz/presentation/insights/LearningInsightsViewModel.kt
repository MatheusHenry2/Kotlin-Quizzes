package com.example.kotlinquizzes.feature.quiz.presentation.insights

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.domain.usecase.GetLearningInsightsUseCase
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract.LearningInsightsAction
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract.LearningInsightsEffect
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract.LearningInsightsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class LearningInsightsViewModel @Inject constructor(
    private val getLearningInsights: GetLearningInsightsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LearningInsightsState())
    val state: StateFlow<LearningInsightsState> = _state.asStateFlow()

    private val _effect = Channel<LearningInsightsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadInsights()
    }

    fun onAction(action: LearningInsightsAction) {
        when (action) {
            LearningInsightsAction.RetryClicked -> loadInsights()
            LearningInsightsAction.BackToHomeClicked -> {
                viewModelScope.launch {
                    _effect.send(LearningInsightsEffect.NavigateToHome)
                }
            }
        }
    }

    private fun loadInsights() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessageResId = null) }
            try {
                val insights = getLearningInsights()
                _state.update {
                    it.copy(isLoading = false, insights = insights, errorMessageResId = null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "LearningInsightsViewModel: loadInsights failed", e)
                _state.update {
                    it.copy(isLoading = false, errorMessageResId = R.string.error_failed_load_insights)
                }
            }
        }
    }
}
