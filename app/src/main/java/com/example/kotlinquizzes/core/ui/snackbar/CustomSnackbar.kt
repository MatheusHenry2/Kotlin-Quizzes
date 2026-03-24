package com.example.kotlinquizzes.core.ui.snackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kotlinquizzes.core.theme.Error
import com.example.kotlinquizzes.core.theme.Success
import com.example.kotlinquizzes.core.theme.White

enum class SnackbarType { SUCCESS, ERROR }

@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    snackbarType: SnackbarType,
    modifier: Modifier = Modifier,
) {
    val containerColor = when (snackbarType) {
        SnackbarType.SUCCESS -> Success
        SnackbarType.ERROR -> Error
    }

    Snackbar(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        containerColor = containerColor,
        contentColor = White,
    ) {
        Text(text = snackbarData.visuals.message)
    }
}
