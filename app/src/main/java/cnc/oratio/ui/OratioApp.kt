package cnc.oratio.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cnc.oratio.data.repository.PrayerRepository
import cnc.oratio.ui.viewmodel.HomeViewModel
import cnc.oratio.ui.viewmodel.RemindersViewModel
import cnc.oratio.ui.viewmodel.ViewModelFactory

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Reminders : Screen("reminders")
    object PrayerDetail : Screen("prayer_detail/{prayerId}") {
        fun createRoute(prayerId: String) = "prayer_detail/$prayerId"
    }
}

// iOS-style cubic bezier easing curve matching iOS UINavigationController
private val IosTransitionEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
private const val IosTransitionDuration = 400

@Composable
fun OratioApp(
    repository: PrayerRepository,
    targetPrayerId: String? = null
) {
    val navController = rememberNavController()
    val viewModelFactory = remember(repository) { ViewModelFactory(repository) }

    LaunchedEffect(targetPrayerId) {
        if (!targetPrayerId.isNullOrEmpty()) {
            navController.navigate(Screen.PrayerDetail.createRoute(targetPrayerId))
        }
    }

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
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            HomeScreen(
                viewModel = homeViewModel,
                onPrayerClick = { prayerId ->
                    navController.navigate(Screen.PrayerDetail.createRoute(prayerId))
                },
                onRemindersClick = {
                    navController.navigate(Screen.Reminders.route)
                }
            )
        }

        composable(Screen.Reminders.route) {
            val remindersViewModel: RemindersViewModel = viewModel(factory = viewModelFactory)
            RemindersScreen(
                viewModel = remindersViewModel,
                onBackClick = { navController.popBackStack() }
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
