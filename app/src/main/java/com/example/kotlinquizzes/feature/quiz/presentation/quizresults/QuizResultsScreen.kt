package com.example.kotlinquizzes.feature.quiz.presentation.quizresults

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.kotlinquizzes.core.utils.TestTags
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.theme.Gray50
import com.example.kotlinquizzes.core.theme.Gray600
import com.example.kotlinquizzes.core.theme.Purple100
import com.example.kotlinquizzes.core.theme.Purple600
import com.example.kotlinquizzes.core.theme.Success
import com.example.kotlinquizzes.core.theme.Error
import com.example.kotlinquizzes.core.theme.White
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContract.QuizResultsAction
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContract.QuizResultsEffect
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsContract.QuizResultsState
import kotlinx.coroutines.delay

@Composable
fun QuizResultsScreen(
    onNavigateToHome: () -> Unit,
    viewModel: QuizResultsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                QuizResultsEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    QuizResultsContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuizResultsContent(
    state: QuizResultsState,
    onAction: (QuizResultsAction) -> Unit,
) {
    var showCards by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showCards = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.quiz_results_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    val closeResultsDesc = stringResource(R.string.close_results)
                    IconButton(
                        onClick = { onAction(QuizResultsAction.BackToHomeClicked) },
                        modifier = Modifier.semantics {
                            contentDescription = closeResultsDesc
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.size(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                ),
            )
        },
        containerColor = White,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .testTag(TestTags.RESULTS_CONTENT),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            val congratsAccessibility = stringResource(R.string.congratulations_accessibility)
            Text(
                text = stringResource(R.string.congratulations),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Purple600,
                modifier = Modifier.semantics {
                    contentDescription = congratsAccessibility
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.quiz_completed_message),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            val finalScoreAccessibility = stringResource(R.string.final_score_accessibility, state.scorePercentage)
            AnimatedVisibility(
                visible = showCards,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(500),
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Gray50)
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.final_score),
                            style = MaterialTheme.typography.titleMedium,
                            color = Gray600,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${state.scorePercentage}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Purple600,
                            modifier = Modifier.semantics {
                                contentDescription = finalScoreAccessibility
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val correctAccessibility = stringResource(R.string.correct_answers) + ": ${state.correctAnswers}"
            val incorrectAccessibility = stringResource(R.string.incorrect_answers) + ": ${state.incorrectAnswers}"
            AnimatedVisibility(
                visible = showCards,
                enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(700, delayMillis = 200),
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Gray50)
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.correct_answers),
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${state.correctAnswers}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Success,
                                modifier = Modifier.semantics {
                                    contentDescription = correctAccessibility
                                },
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Gray50)
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.incorrect_answers),
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${state.incorrectAnswers}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Error,
                                modifier = Modifier.semantics {
                                    contentDescription = incorrectAccessibility
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val backToHomeAccessibility = stringResource(R.string.back_to_home_accessibility)
            Button(
                onClick = { onAction(QuizResultsAction.BackToHomeClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = backToHomeAccessibility
                    },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple100,
                    contentColor = Purple600,
                ),
            ) {
                Text(
                    text = stringResource(R.string.back_to_home),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
