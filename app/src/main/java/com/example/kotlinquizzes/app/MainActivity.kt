package com.example.kotlinquizzes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.kotlinquizzes.core.navigation.AppNavigation
import com.example.kotlinquizzes.core.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.core.ui.event.UiEventManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var uiEventManager: UiEventManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinQuizzesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                ) {
                    AppNavigation(uiEventManager = uiEventManager)
                }
            }
        }
    }
}
