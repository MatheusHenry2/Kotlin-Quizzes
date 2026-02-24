package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kotlinquizzes.core.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.core.theme.Purple500
import com.example.kotlinquizzes.core.theme.TextPrimary
import com.example.kotlinquizzes.core.theme.White
import com.example.kotlinquizzes.feature.quiz.domain.model.Question
import com.example.kotlinquizzes.feature.quiz.domain.model.Quiz
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListAction
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListEffect
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun QuizListScreen(
    onNavigateToQuiz: (String) -> Unit = {},
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizListContent(
    state: QuizListState,
    onAction: (QuizListAction) -> Unit,
) {
    Scaffold(
        containerColor = White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Kotlin Quizzes",
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
        bottomBar = {
            QuizBottomBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome back, ${state.userName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> {
                    ErrorContent(
                        message = state.errorMessage,
                        onRetry = { onAction(QuizListAction.RetryClicked) }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                }
                            )
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
    val initial = userName.trim().take(1).ifBlank { "U" }.uppercase()

    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
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
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "</>",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${quiz.questions.size} questions",
                style = MaterialTheme.typography.bodySmall,
                color = Purple500
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
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
            Text("Try again")
        }
    }
}

@Composable
private fun QuizBottomBar() {
    NavigationBar(
        containerColor = White,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.School, contentDescription = "Learn") },
            label = { Text("Learn") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.PersonOutline, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun QuizListScreenPreview() {
    KotlinQuizzesTheme {
        QuizListContent(
            state = QuizListState(
                isLoading = false,
                userName = "Sarah",
                quizzes = listOf(
                    Quiz(
                        id = "kotlin_basics",
                        title = "Kotlin Basics",
                        questions = List(10) { index -> previewQuestion(index) }
                    ),
                    Quiz(
                        id = "data_types",
                        title = "Data Types",
                        questions = List(15) { index -> previewQuestion(index) }
                    ),
                    Quiz(
                        id = "control_flow",
                        title = "Control Flow",
                        questions = List(12) { index -> previewQuestion(index) }
                    ),
                    Quiz(
                        id = "functions",
                        title = "Functions",
                        questions = List(8) { index -> previewQuestion(index) }
                    ),
                    Quiz(
                        id = "oop",
                        title = "Object-Oriented Programming",
                        questions = List(20) { index -> previewQuestion(index) }
                    )
                )
            ),
            onAction = {}
        )
    }
}

private fun previewQuestion(index: Int): Question {
    return Question(
        id = "q_$index",
        text = "Question $index",
        options = emptyList(),
        correctIndex = 0,
        tags = emptyList()
    )
}