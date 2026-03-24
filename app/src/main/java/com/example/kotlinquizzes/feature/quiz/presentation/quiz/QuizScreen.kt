package com.example.kotlinquizzes.feature.quiz.presentation.quiz

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.theme.Gray200
import com.example.kotlinquizzes.core.theme.Gray600
import com.example.kotlinquizzes.core.theme.Purple100
import com.example.kotlinquizzes.core.theme.Purple600
import com.example.kotlinquizzes.core.theme.White
import com.example.kotlinquizzes.feature.quiz.domain.model.Question
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContract.QuizAction
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContract.QuizEffect
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizContract.QuizState

@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    onQuizFinished: (totalQuestions: Int, correctAnswers: Int) -> Unit = { _, _ -> },
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val view = LocalView.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                QuizEffect.NavigateBack -> onNavigateBack()
                is QuizEffect.QuizFinished -> onQuizFinished(effect.totalQuestions, effect.correctAnswers)
                QuizEffect.HapticFeedback -> {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
        }
    }

    QuizContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizContent(
    state: QuizState,
    onAction: (QuizAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.quiz_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    val closeQuizDesc = stringResource(R.string.close_quiz)
                    IconButton(
                        onClick = { onAction(QuizAction.CloseClicked) },
                        modifier = Modifier.semantics {
                            contentDescription = closeQuizDesc
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    // Spacer to balance the close button for centered title
                    Spacer(modifier = Modifier.size(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                ),
            )
        },
        containerColor = White,
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Purple600)
                }
            }

            state.errorMessageResId != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(state.errorMessageResId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            state.currentQuestion != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    ProgressSection(
                        currentIndex = state.currentQuestionIndex,
                        total = state.totalQuestions,
                        progress = state.progress,
                    )

                    AnimatedContent(
                        targetState = state.currentQuestionIndex,
                        transitionSpec = {
                            (slideInHorizontally { it } + fadeIn())
                                .togetherWith(slideOutHorizontally { -it } + fadeOut())
                        },
                        label = "question_transition",
                        modifier = Modifier.weight(1f),
                    ) { targetIndex ->
                        val question = state.questions.getOrNull(targetIndex) ?: return@AnimatedContent
                        QuestionSection(
                            question = question,
                            selectedOptionIndex = state.selectedOptionIndex,
                            onOptionSelected = { onAction(QuizAction.OptionSelected(it)) },
                        )
                    }

                    NextButton(
                        enabled = state.selectedOptionIndex != null,
                        isLastQuestion = state.isLastQuestion,
                        onClick = { onAction(QuizAction.NextClicked) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressSection(
    currentIndex: Int,
    total: Int,
    progress: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        val progressAccessibility = stringResource(R.string.question_progress_accessibility, currentIndex + 1, total)
        Text(
            text = stringResource(R.string.question_progress, currentIndex + 1, total),
            style = MaterialTheme.typography.bodySmall,
            color = Gray600,
            modifier = Modifier.semantics {
                contentDescription = progressAccessibility
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Purple600,
            trackColor = Gray200,
        )
    }
}

@Composable
private fun QuestionSection(
    question: Question,
    selectedOptionIndex: Int?,
    onOptionSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        val questionAccessibility = stringResource(R.string.question_accessibility, question.text)
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics { contentDescription = questionAccessibility },
        )
        Spacer(modifier = Modifier.height(24.dp))

        question.options.forEachIndexed { index, option ->
            val optionAccessibility = stringResource(R.string.option_accessibility, index + 1, option)
            OptionCard(
                text = option,
                isSelected = selectedOptionIndex == index,
                onClick = { onOptionSelected(index) },
                modifier = Modifier.semantics {
                    contentDescription = optionAccessibility
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun OptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) Purple600 else Gray200
    val containerColor = if (isSelected) Purple100 else White

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Purple600,
                    unselectedColor = Gray600,
                ),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun NextButton(
    enabled: Boolean,
    isLastQuestion: Boolean,
    onClick: () -> Unit,
) {
    val buttonAccessibility = if (isLastQuestion) stringResource(R.string.finish_quiz_accessibility) else stringResource(R.string.next_question_accessibility)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .height(56.dp)
            .semantics { contentDescription = buttonAccessibility },
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple600,
            disabledContainerColor = Gray200,
        ),
    ) {
        Text(
            text = if (isLastQuestion) stringResource(R.string.finish) else stringResource(R.string.next),
            style = MaterialTheme.typography.titleMedium,
            color = White,
        )
    }
}
