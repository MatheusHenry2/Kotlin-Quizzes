package com.example.kotlinquizzes.feature.quiz.presentation.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.data.local.DatabaseSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val databaseSeeder: DatabaseSeeder,
) : ViewModel() {

    private val _seedingDone = MutableStateFlow(false)
    val seedingDone: StateFlow<Boolean> = _seedingDone.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                databaseSeeder.seedIfEmpty()
            } catch (e: Exception) {
                Log.e(TAG, "SplashViewModel: seeding failed", e)
            }
            _seedingDone.value = true
        }
    }
}
