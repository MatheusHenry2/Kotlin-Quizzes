package com.example.kotlinquizzes.feature.quiz.presentation.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.navigation.BottomNavBar
import com.example.kotlinquizzes.core.navigation.BottomNavDestination
import com.example.kotlinquizzes.core.theme.Gray200
import com.example.kotlinquizzes.core.theme.Gray50
import com.example.kotlinquizzes.core.theme.Gray600
import com.example.kotlinquizzes.core.theme.Purple600
import com.example.kotlinquizzes.core.theme.TextPrimary
import com.example.kotlinquizzes.core.theme.White
import com.example.kotlinquizzes.feature.quiz.domain.model.CategoryMastery
import com.example.kotlinquizzes.feature.quiz.domain.model.LearningInsights
import com.example.kotlinquizzes.feature.quiz.domain.model.WeakTopic
import com.example.kotlinquizzes.core.utils.TestTags
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract.LearningInsightsAction
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract.LearningInsightsEffect
import com.example.kotlinquizzes.feature.quiz.presentation.insights.LearningInsightsContract.LearningInsightsState
import kotlinx.coroutines.flow.collectLatest

private val AccentRed = Color(0xFFF74B6D)
private val AccentRedTint = Color(0x1AF74B6D)
private val TrackGray = Color(0xFFDBDDDD)

@Composable
fun LearningInsightsScreen(
    onNavigateToHome: () -> Unit,
    onOpenDocumentation: (String) -> Unit,
    viewModel: LearningInsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LearningInsightsEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    LearningInsightsContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToHome = onNavigateToHome,
        onOpenDocumentation = onOpenDocumentation,
    )
}

@Composable
internal fun LearningInsightsContent(
    state: LearningInsightsState,
    onAction: (LearningInsightsAction) -> Unit,
    onNavigateToHome: () -> Unit,
    onOpenDocumentation: (String) -> Unit = {},
) {
    Scaffold(
        containerColor = Gray50,
        bottomBar = {
            BottomNavBar(
                current = BottomNavDestination.INSIGHTS,
                onNavigate = { destination ->
                    if (destination == BottomNavDestination.HOME) onNavigateToHome()
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TestTags.INSIGHTS_LOADING),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Purple600)
                    }
                }

                state.errorMessageResId != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag(TestTags.INSIGHTS_ERROR),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(state.errorMessageResId),
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { onAction(LearningInsightsAction.RetryClicked) }) {
                            Text(stringResource(R.string.try_again))
                        }
                    }
                }

                state.insights != null -> {
                    InsightsBody(
                        insights = state.insights,
                        onBackToHome = { onAction(LearningInsightsAction.BackToHomeClicked) },
                        onOpenDocumentation = onOpenDocumentation,
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Purple600)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsBody(
    insights: LearningInsights,
    onBackToHome: () -> Unit,
    onOpenDocumentation: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .testTag(TestTags.INSIGHTS_CONTENT),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        TopSummaryCard(insights = insights)
        MasteryByCategorySection(masteries = insights.masteryByCategory)
        TopicsToImproveSection(
            topics = insights.topicsToImprove,
            onOpenDocumentation = onOpenDocumentation,
        )
        BackToHomeButton(onClick = onBackToHome)
    }
}

@Composable
private fun TopSummaryCard(insights: LearningInsights) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.insights_your_progress).uppercase(),
                color = Gray600,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
            )

            // Two symmetric columns: Accuracy (left) and Total Quizzes (right).
            // Both use weight(1f) so they share the width equally and align
            // at the same vertical baseline regardless of number size.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${insights.accuracyPercent}%",
                        color = Purple600,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag(TestTags.INSIGHTS_ACCURACY),
                    )
                    Text(
                        text = stringResource(R.string.insights_accuracy).uppercase(),
                        color = Gray600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = insights.totalQuizzesCompleted.toString(),
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.insights_total_quizzes).uppercase(),
                        color = Gray600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CountTile(
                    modifier = Modifier.weight(1f),
                    count = insights.totalCorrect,
                    label = stringResource(R.string.insights_correct),
                    accentColor = Purple600,
                )
                CountTile(
                    modifier = Modifier.weight(1f),
                    count = insights.totalIncorrect,
                    label = stringResource(R.string.insights_incorrect),
                    accentColor = AccentRed,
                )
            }
        }
    }
}

@Composable
private fun CountTile(
    modifier: Modifier,
    count: Int,
    label: String,
    accentColor: Color,
) {
    Row(
        modifier = modifier
            .heightIn(min = 84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Gray200)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(accentColor),
        )

        Spacer(Modifier.width(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = count.toString(),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = label.uppercase(),
                color = Gray600,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.25.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MasteryByCategorySection(masteries: List<CategoryMastery>) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
            text = stringResource(R.string.insights_mastery_by_category),
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (masteries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.insights_no_mastery_yet),
                        color = Gray600,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    masteries.forEach { mastery ->
                        MasteryRow(mastery = mastery)
                    }
                }
            }
        }
    }
}

@Composable
private fun MasteryRow(mastery: CategoryMastery) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = prettifyTag(mastery.tag).uppercase(),
                color = Gray600,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
            )
            Text(
                text = "${mastery.masteryPercent}%",
                color = Purple600,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(TrackGray),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(mastery.masteryPercent.coerceIn(0, 100) / 100f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Purple600),
            )
        }
    }
}

@Composable
private fun TopicsToImproveSection(
    topics: List<WeakTopic>,
    onOpenDocumentation: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
            text = stringResource(R.string.insights_topics_to_improve),
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        if (topics.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Gray200),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Text(
                    text = stringResource(R.string.insights_no_weak_topics),
                    color = Gray600,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                topics.forEach { topic ->
                    WeakTopicRow(
                        topic = topic,
                        onOpenDocumentation = onOpenDocumentation,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeakTopicRow(
    topic: WeakTopic,
    onOpenDocumentation: (String) -> Unit,
) {
    val docUrl = tagDocumentationUrl(topic.tag)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Gray200)
            .then(
                if (docUrl != null) {
                    Modifier.clickable { onOpenDocumentation(docUrl) }
                } else {
                    Modifier
                }
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AccentRedTint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prettifyTag(topic.tag),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.insights_error_rate, topic.errorRatePercent),
                color = Gray600,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            // Arrow tinted Purple when there's a doc link, gray otherwise
            tint = if (docUrl != null) Purple600 else Gray600,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun BackToHomeButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Gray200)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
            .testTag(TestTags.INSIGHTS_BACK_HOME),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.back_to_home),
            color = Purple600,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun prettifyTag(tag: String): String =
    tag.replace('_', ' ')
        .replace('-', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { it.uppercase() }
        }

/**
 * Maps a quiz tag to its official Kotlin/Android documentation URL.
 * All links point to kotlinlang.org or developer.android.com.
 */
private fun tagDocumentationUrl(tag: String): String = when (tag.lowercase()) {
    // Kotlin core
    "kotlin", "basics", "variables"    -> "https://kotlinlang.org/docs/basic-syntax.html"
    "null-safety"                       -> "https://kotlinlang.org/docs/null-safety.html"
    "operators"                         -> "https://kotlinlang.org/docs/operator-overloading.html"
    "collections"                       -> "https://kotlinlang.org/docs/collections-overview.html"
    "functional", "lambdas"             -> "https://kotlinlang.org/docs/lambdas.html"
    "scope-functions"                   -> "https://kotlinlang.org/docs/scope-functions.html"
    "idioms"                            -> "https://kotlinlang.org/docs/idioms.html"
    "generics"                          -> "https://kotlinlang.org/docs/generics.html"
    "delegation"                        -> "https://kotlinlang.org/docs/delegation.html"
    "properties"                        -> "https://kotlinlang.org/docs/properties.html"
    "extensions"                        -> "https://kotlinlang.org/docs/extensions.html"
    "sealed"                            -> "https://kotlinlang.org/docs/sealed-classes.html"
    "enum"                              -> "https://kotlinlang.org/docs/enum-classes.html"
    "sequences"                         -> "https://kotlinlang.org/docs/sequences.html"
    "error-handling"                    -> "https://kotlinlang.org/docs/exception-handling.html"
    "dsl"                               -> "https://kotlinlang.org/docs/type-safe-builders.html"
    "multiplatform"                     -> "https://kotlinlang.org/docs/multiplatform.html"
    "oop", "classes"                    -> "https://kotlinlang.org/docs/classes.html"
    "interfaces"                        -> "https://kotlinlang.org/docs/interfaces.html"
    // Coroutines / Flow
    "coroutines", "concurrency"         -> "https://kotlinlang.org/docs/coroutines-guide.html"
    "flow"                              -> "https://kotlinlang.org/docs/flow.html"
    // Android core
    "android", "components"             -> "https://developer.android.com/guide/components/fundamentals"
    "lifecycle"                         -> "https://developer.android.com/topic/libraries/architecture/lifecycle"
    "permissions"                       -> "https://developer.android.com/guide/topics/permissions/overview"
    "notifications"                     -> "https://developer.android.com/develop/ui/views/notifications"
    "deep-links"                        -> "https://developer.android.com/training/app-links"
    "security"                          -> "https://developer.android.com/topic/security/best-practices"
    "gradle"                            -> "https://developer.android.com/studio/build"
    // Jetpack Compose
    "compose"                           -> "https://developer.android.com/jetpack/compose/documentation"
    "state"                             -> "https://developer.android.com/jetpack/compose/state"
    "navigation"                        -> "https://developer.android.com/guide/navigation"
    "animations"                        -> "https://developer.android.com/jetpack/compose/animation/introduction"
    "gestures"                          -> "https://developer.android.com/jetpack/compose/touch-input/gestures"
    "accessibility"                     -> "https://developer.android.com/jetpack/compose/accessibility"
    // Architecture
    "viewmodel", "architecture"         -> "https://developer.android.com/topic/architecture"
    "room", "database"                  -> "https://developer.android.com/training/data-storage/room"
    "hilt", "dependency-injection"      -> "https://developer.android.com/training/dependency-injection/hilt-android"
    "networking", "retrofit"            -> "https://developer.android.com/training/basics/network-ops/connecting"
    "datastore"                         -> "https://developer.android.com/topic/libraries/architecture/datastore"
    "workmanager"                       -> "https://developer.android.com/topic/libraries/architecture/workmanager"
    "firebase"                          -> "https://firebase.google.com/docs/auth/android/start"
    "testing"                           -> "https://developer.android.com/training/testing"
    // Default: Android developer home
    else                                -> "https://developer.android.com/docs"
}
