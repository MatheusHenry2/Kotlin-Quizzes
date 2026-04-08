package com.example.kotlinquizzes.core.navigation

object NavigationConstants {

    object Args {
        const val QUIZ_ID = "quizId"
        const val TOTAL_QUESTIONS = "totalQuestions"
        const val CORRECT_ANSWERS = "correctAnswers"
    }

    object Routes {
        const val LOGIN = "login"
        const val SPLASH = "splash"
        const val QUIZ_LIST = "quiz_list"
        const val INSIGHTS = "insights"
        const val QUIZ = "quiz/{${Args.QUIZ_ID}}"
        const val QUIZ_RESULTS =
            "quiz_results/{${Args.TOTAL_QUESTIONS}}/{${Args.CORRECT_ANSWERS}}"

        fun quizRoute(quizId: String) = "quiz/$quizId"
        fun quizResultsRoute(totalQuestions: Int, correctAnswers: Int) =
            "quiz_results/$totalQuestions/$correctAnswers"
    }
}
