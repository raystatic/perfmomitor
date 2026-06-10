package com.example.androidapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidapp.ui.fast.FastFeedScreen
import com.example.androidapp.ui.home.HomeScreen
import com.example.androidapp.ui.slow.SlowFeedScreen

object Routes {
    const val HOME = "home"
    const val SLOW_FEED = "slow_feed"
    const val FAST_FEED = "fast_feed"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateSlow = { navController.navigate(Routes.SLOW_FEED) },
                onNavigateFast = { navController.navigate(Routes.FAST_FEED) },
            )
        }
        composable(Routes.SLOW_FEED) {
            SlowFeedScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.FAST_FEED) {
            FastFeedScreen(onBack = { navController.popBackStack() })
        }
    }
}
