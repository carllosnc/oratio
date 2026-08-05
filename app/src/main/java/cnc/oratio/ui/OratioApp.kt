package cnc.oratio.ui

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

@Composable
fun OratioApp(repository: PrayerRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
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
