package com.example.kotlinquizzes.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.login.LoginContract.Effect
import com.example.kotlinquizzes.login.LoginContract.Intent
import com.example.kotlinquizzes.login.LoginContract.State
import com.example.kotlinquizzes.ui.theme.Dimens
import com.example.kotlinquizzes.ui.theme.KotlinQuizzesTheme
import com.example.kotlinquizzes.ui.theme.Purple500
import com.example.kotlinquizzes.ui.theme.TextPrimary
import com.example.kotlinquizzes.ui.theme.White
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Effect.NavigateToHome -> onNavigateToHome()
                is Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    LoginContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginContent(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.login_title),
                        fontSize = Dimens.FontMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(Intent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = White,
                        titleContentColor = TextPrimary,
                    ),
            )
        },
        containerColor = White,
    ) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Dimens.PaddingDefault),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(Dimens.PaddingXLarge))

            Text(
                text = stringResource(R.string.login_welcome),
                fontSize = Dimens.FontXLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

            Text(
                text = stringResource(R.string.login_subtitle),
                fontSize = Dimens.FontDefault,
                fontWeight = FontWeight.Normal,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = Dimens.LineHeightDefault,
                modifier = Modifier.padding(horizontal = Dimens.PaddingDefault),
            )

            Spacer(modifier = Modifier.weight(1f))

            GoogleSignInButton(
                onClick = { onIntent(Intent.GoogleSignInClicked) },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
        }
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.height(Dimens.ButtonHeight),
        shape = RoundedCornerShape(Dimens.ButtonCornerRadius),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Purple500,
                contentColor = White,
                disabledContainerColor = Purple500.copy(alpha = 0.7f),
                disabledContentColor = White.copy(alpha = 0.7f),
            ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.IconSmall),
                color = White,
                strokeWidth = Dimens.StrokeSmall,
            )
        } else {
            Text(
                text = stringResource(R.string.login_google_button),
                fontSize = Dimens.FontDefault,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    KotlinQuizzesTheme {
        LoginContent(
            state = State(),
            onIntent = {},
        )
    }
}
