package com.example.kotlinquizzes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kotlinquizzes.login.LoginScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val QUIZ = "quiz/{quizId}"
    const val RESULT = "result"
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
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.HOME) {
        }

        composable(Routes.QUIZ) {
        }

        composable(Routes.RESULT) {
        }
    }
}
