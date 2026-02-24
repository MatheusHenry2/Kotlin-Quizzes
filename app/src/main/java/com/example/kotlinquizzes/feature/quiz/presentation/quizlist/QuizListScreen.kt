package com.example.kotlinquizzes.feature.quiz.presentation.quizlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListContract.QuizListState
import com.example.kotlinquizzes.core.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.core.theme.TextPrimary

@Composable
fun QuizListScreen(viewModel: QuizListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    QuizListContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun QuizListContent(
    state: QuizListContract.QuizListState,
    onAction: (QuizListContract.QuizListAction) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Creating",
            color = TextPrimary,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun QuizListScreenPreview() {
    KotlinQuizzesTheme {
        QuizListContent(
            state = QuizListState(),
            onAction = {},
        )
    }
}
