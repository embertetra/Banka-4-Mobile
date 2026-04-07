package rs.raf.banka4mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import rs.raf.banka4mobile.presentation.cards.CardsScreen
import rs.raf.banka4mobile.presentation.home.HomeScreen
import rs.raf.banka4mobile.presentation.login.LoginScreen
import rs.raf.banka4mobile.presentation.verification.VerificationScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigateToHome() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onOpenCards = { navController.navigate(Screen.Cards.route) }
            )
        }

        composable(Screen.Cards.route) {
            CardsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Verification.route) {
            VerificationScreen(
                onBack = { navController.navigateToHome() }
            )
        }
    }
}

fun NavController.navigateToHome() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Login.route) {
            inclusive = true
        }
    }
}
