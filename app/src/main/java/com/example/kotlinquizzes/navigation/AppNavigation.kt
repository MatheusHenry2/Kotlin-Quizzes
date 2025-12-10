package com.example.kotlinquizzes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kotlinquizzes.login.LoginScreen
import com.example.kotlinquizzes.quizlist.QuizListScreen

object Routes {
    const val LOGIN = "login"
    const val QUIZ_LIST = "quiz_list"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.QUIZ_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.QUIZ_LIST) {
            QuizListScreen()
        }
    }
}