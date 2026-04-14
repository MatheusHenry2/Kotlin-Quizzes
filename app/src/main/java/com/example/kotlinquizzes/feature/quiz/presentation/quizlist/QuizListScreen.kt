package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.navigation.BottomNavBar
import com.example.kotlinquizzes.core.navigation.BottomNavDestination
import com.example.kotlinquizzes.core.theme.PurpleSoft
import com.example.kotlinquizzes.core.theme.PurpleSubtitle
import com.example.kotlinquizzes.core.theme.TextPrimary
import com.example.kotlinquizzes.core.theme.White
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.example.kotlinquizzes.core.utils.TestTags
import com.example.kotlinquizzes.core.theme.Gray600
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListAction
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListEffect
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun QuizListScreen(
    onNavigateToQuiz: (String) -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    viewModel: QuizListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is QuizListEffect.NavigateToQuiz -> onNavigateToQuiz(effect.quizId)
            }
        }
    }

    QuizListContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToInsights = onNavigateToInsights,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuizListContent(
    state: QuizListState,
    onAction: (QuizListAction) -> Unit,
    onNavigateToInsights: () -> Unit,
) {
    Scaffold(
        containerColor = White,
        bottomBar = {
            BottomNavBar(
                current = BottomNavDestination.HOME,
                onNavigate = { destination ->
                    if (destination == BottomNavDestination.INSIGHTS) onNavigateToInsights()
                },
            )
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.quiz_list_title),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    AvatarCircle(
                        userName = state.userName,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        },

    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.welcome_back, state.userName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.testTag(TestTags.WELCOME_TEXT)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TestTags.QUIZ_LIST_LOADING),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessageResId != null -> {
                    ErrorContent(
                        message = stringResource(state.errorMessageResId),
                        onRetry = { onAction(QuizListAction.RetryClicked) },
                        modifier = Modifier.testTag(TestTags.QUIZ_LIST_ERROR)
                    )
                }

                else -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onAction(QuizListAction.RefreshPulled) },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TestTags.QUIZ_LIST_CONTENT)
                    ) {
                        if (state.quizzes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag(TestTags.QUIZ_LIST_EMPTY),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.quiz_list_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Gray600,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(
                                    items = state.quizzes,
                                    key = { it.id }
                                ) { quiz ->
                                    QuizListItem(
                                        quiz = quiz,
                                        onClick = {
                                            onAction(QuizListAction.QuizClicked(quiz.id))
                                        },
                                        modifier = Modifier.testTag(
                                            TestTags.QUIZ_LIST_ITEM_PREFIX + quiz.id
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarCircle(
    userName: String,
    modifier: Modifier = Modifier,
) {
    val fallbackInitial = stringResource(R.string.default_user_initial)
    val initial = userName.trim().take(1).ifBlank { fallbackInitial }.uppercase()
    val avatarDescription = stringResource(R.string.user_profile_picture_description, userName)

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag(TestTags.AVATAR_CIRCLE)
            .semantics { contentDescription = avatarDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuizListItem(
    quiz: Quiz,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PurpleSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = stringResource(R.string.quiz_item_icon_description),
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(R.string.questions_count, quiz.questions.size),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    color = PurpleSubtitle
                ),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.try_again))
        }
    }
}
