package cnc.oratio.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cnc.oratio.data.repository.PrayerRepository

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PrayerDetail : Screen("prayer_detail/{prayerId}") {
        fun createRoute(prayerId: String) = "prayer_detail/$prayerId"
    }
}

// iOS-style cubic bezier easing curve (ease-in-out curve matching iOS UINavigationController)
private val IosTransitionEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
private const val IosTransitionDuration = 400

@Composable
fun OratioApp(repository: PrayerRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(IosTransitionDuration, easing = IosTransitionEasing)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(IosTransitionDuration, easing = IosTransitionEasing)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(IosTransitionDuration, easing = IosTransitionEasing)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(IosTransitionDuration, easing = IosTransitionEasing)
            )
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                repository = repository,
                onPrayerClick = { prayerId ->
                    navController.navigate(Screen.PrayerDetail.createRoute(prayerId))
                }
            )
        }

        composable(
            route = Screen.PrayerDetail.route,
            arguments = listOf(
                navArgument("prayerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val prayerId = backStackEntry.arguments?.getString("prayerId") ?: ""
            PrayerDetailScreen(
                prayerId = prayerId,
                repository = repository,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
