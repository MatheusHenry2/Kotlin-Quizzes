package com.example.kotlinquizzes.feature.quiz.presentation.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.theme.Gray50
import com.example.kotlinquizzes.core.theme.Purple100
import com.example.kotlinquizzes.core.theme.Purple500
import com.example.kotlinquizzes.core.theme.Purple600
import com.example.kotlinquizzes.core.theme.TextPrimary
import com.example.kotlinquizzes.core.theme.White
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 3000L

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    var progressTarget by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = SPLASH_DURATION_MS.toInt(), easing = LinearEasing),
        label = "splash-progress",
    )

    LaunchedEffect(Unit) {
        progressTarget = 1f
        delay(SPLASH_DURATION_MS)
        onSplashFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Gray50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BrandLogo()
                Spacer(modifier = Modifier.height(40.dp))
                BrandTitle()
            }

            BottomLoadingBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .padding(bottom = 96.dp),
            )
        }
    }
}

@Composable
private fun BrandLogo() {
    Box(
        modifier = Modifier
            .size(176.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = TextPrimary.copy(alpha = 0.05f),
                spotColor = TextPrimary.copy(alpha = 0.05f),
            )
            .clip(RoundedCornerShape(32.dp))
            .background(White),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Purple100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = Purple600,
                modifier = Modifier.size(56.dp),
            )
        }
    }
}

@Composable
private fun BrandTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.app_brand_name),
            color = TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1.5).sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Purple600.copy(alpha = 0.2f)),
        )
    }
}

@Composable
private fun BottomLoadingBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(3.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(color = Color(0xFFDBDDDD)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(3.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Purple600, Purple500),
                    ),
                ),
        )
    }
}
