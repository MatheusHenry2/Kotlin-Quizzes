package com.example.kotlinquizzes.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.kotlinquizzes.core.utils.TestTags
import com.example.kotlinquizzes.R
import com.example.kotlinquizzes.core.theme.Gray600
import com.example.kotlinquizzes.core.theme.Purple100
import com.example.kotlinquizzes.core.theme.Purple600
import com.example.kotlinquizzes.core.theme.White

enum class BottomNavDestination { HOME, INSIGHTS }

@Composable
fun BottomNavBar(
    current: BottomNavDestination,
    onNavigate: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = White,
    ) {
        NavigationBarItem(
            modifier = Modifier.testTag(TestTags.BOTTOM_NAV_HOME),
            selected = current == BottomNavDestination.HOME,
            onClick = { if (current != BottomNavDestination.HOME) onNavigate(BottomNavDestination.HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                )
            },
            label = { Text(stringResource(R.string.bottom_nav_home)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Purple600,
                selectedTextColor = Purple600,
                indicatorColor = Purple100,
                unselectedIconColor = Gray600,
                unselectedTextColor = Gray600,
            ),
        )
        NavigationBarItem(
            modifier = Modifier.testTag(TestTags.BOTTOM_NAV_INSIGHTS),
            selected = current == BottomNavDestination.INSIGHTS,
            onClick = { if (current != BottomNavDestination.INSIGHTS) onNavigate(BottomNavDestination.INSIGHTS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                )
            },
            label = { Text(stringResource(R.string.bottom_nav_insights)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Purple600,
                selectedTextColor = Purple600,
                indicatorColor = Purple100,
                unselectedIconColor = Gray600,
                unselectedTextColor = Gray600,
            ),
        )
    }
}
