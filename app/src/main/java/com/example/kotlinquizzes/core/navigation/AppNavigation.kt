package com.example.kotlinquizzes.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.ui.event.SnackbarMessage
import com.example.kotlinquizzes.core.ui.event.UiEvent
import com.example.kotlinquizzes.core.ui.event.UiEventManager
import com.example.kotlinquizzes.core.ui.snackbar.CustomSnackbar
import com.example.kotlinquizzes.core.ui.snackbar.SnackbarType
import com.example.kotlinquizzes.feature.presentation.login.LoginScreen
import com.example.kotlinquizzes.feature.quiz.presentation.quiz.QuizScreen
import com.example.kotlinquizzes.feature.quiz.presentation.quizlist.QuizListScreen
import com.example.kotlinquizzes.feature.quiz.presentation.quizresults.QuizResultsScreen

@Composable
fun AppNavigation(uiEventManager: UiEventManager) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }
    val context = LocalContext.current

    LaunchedEffect(uiEventManager) {
        uiEventManager.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    currentSnackbarType = event.type
                    val message = when (val msg = event.message) {
                        is SnackbarMessage.Text -> msg.value
                        is SnackbarMessage.Resource -> context.getString(msg.resId)
                    }
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                CustomSnackbar(
                    snackbarData = data,
                    snackbarType = currentSnackbarType,
                )
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationConstants.Routes.LOGIN,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(NavigationConstants.Routes.LOGIN) {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(NavigationConstants.Routes.QUIZ_LIST) {
                            popUpTo(NavigationConstants.Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            composable(NavigationConstants.Routes.QUIZ_LIST) {
                QuizListScreen(
                    onNavigateToQuiz = { quizId ->
                        navController.navigate(NavigationConstants.Routes.quizRoute(quizId))
                    },
                )
            }

            composable(
                route = NavigationConstants.Routes.QUIZ,
                arguments = listOf(
                    navArgument(NavigationConstants.Args.QUIZ_ID) { type = NavType.StringType },
                ),
            ) {
                QuizScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onQuizFinished = { totalQuestions, correctAnswers ->
                        navController.navigate(
                            NavigationConstants.Routes.quizResultsRoute(totalQuestions, correctAnswers)
                        ) {
                            popUpTo(NavigationConstants.Routes.QUIZ_LIST)
                        }
                    },
                )
            }

            composable(
                route = NavigationConstants.Routes.QUIZ_RESULTS,
                arguments = listOf(
                    navArgument(NavigationConstants.Args.TOTAL_QUESTIONS) { type = NavType.IntType },
                    navArgument(NavigationConstants.Args.CORRECT_ANSWERS) { type = NavType.IntType },
                ),
            ) {
                QuizResultsScreen(
                    onNavigateToHome = {
                        uiEventManager.showSuccess(R.string.snackbar_quiz_completed)
                        navController.navigate(NavigationConstants.Routes.QUIZ_LIST) {
                            popUpTo(NavigationConstants.Routes.QUIZ_LIST) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
