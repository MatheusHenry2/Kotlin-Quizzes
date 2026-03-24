package com.example.kotlinquizzes.core.ui.event

import androidx.annotation.StringRes
import com.example.kotlinquizzes.core.ui.snackbar.SnackbarType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class SnackbarMessage {
    data class Text(val value: String) : SnackbarMessage()
    data class Resource(@StringRes val resId: Int) : SnackbarMessage()
}

sealed class UiEvent {
    data class ShowSnackbar(
        val message: SnackbarMessage,
        val type: SnackbarType,
    ) : UiEvent()
}

@Singleton
class UiEventManager @Inject constructor() {

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    fun showSuccess(message: String) {
        _events.tryEmit(UiEvent.ShowSnackbar(SnackbarMessage.Text(message), SnackbarType.SUCCESS))
    }

    fun showSuccess(@StringRes resId: Int) {
        _events.tryEmit(UiEvent.ShowSnackbar(SnackbarMessage.Resource(resId), SnackbarType.SUCCESS))
    }

    fun showError(message: String) {
        _events.tryEmit(UiEvent.ShowSnackbar(SnackbarMessage.Text(message), SnackbarType.ERROR))
    }

    fun showError(@StringRes resId: Int) {
        _events.tryEmit(UiEvent.ShowSnackbar(SnackbarMessage.Resource(resId), SnackbarType.ERROR))
    }
}
