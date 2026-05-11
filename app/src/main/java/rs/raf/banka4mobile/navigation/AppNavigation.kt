package rs.raf.banka4mobile.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import rs.raf.banka4mobile.presentation.cards.CardsScreen
import rs.raf.banka4mobile.presentation.exchange.ExchangeScreen
import rs.raf.banka4mobile.presentation.home.HomeScreen
import rs.raf.banka4mobile.presentation.loan.LoanScreen
import rs.raf.banka4mobile.presentation.login.LoginScreen
import rs.raf.banka4mobile.presentation.profile.ProfileScreen
import rs.raf.banka4mobile.presentation.transfers.TransferScreen
import rs.raf.banka4mobile.presentation.verification.VerificationScreen

private data class BottomTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String?
)

private val bottomTabs = listOf(
    BottomTab(
        label = "Transakcije",
        selectedIcon = Icons.Filled.SwapHoriz,
        unselectedIcon = Icons.Outlined.SwapHoriz,
        route = Screen.Transfers.route
    ),
    BottomTab(
        label = "Menjačnica",
        selectedIcon = Icons.Filled.MonetizationOn,
        unselectedIcon = Icons.Outlined.MonetizationOn,
        route = Screen.Exchange.route
    ),
    BottomTab(
        label = "Računi",
        selectedIcon = Icons.Filled.AccountBalance,
        unselectedIcon = Icons.Outlined.AccountBalance,
        route = Screen.Home.route
    ),
    BottomTab(
        label = "Verifikacija",
        selectedIcon = Icons.Filled.VerifiedUser,
        unselectedIcon = Icons.Outlined.VerifiedUser,
        route = Screen.Verification.route
    ),
    BottomTab(
        label = "Profil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        route = Screen.Profile.route
    )
)

private val routesWithBottomBar = setOf(
    Screen.Home.route,
    Screen.Cards.route,
    Screen.Cards.routeWithArg,
    Screen.Loans.route,
    Screen.Transfers.route,
    Screen.Verification.route,
    Screen.Exchange.route,
    Screen.Profile.route
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in routesWithBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { navController.navigateToHome() }
                )
            }

            composable(Screen.Transfers.route) {
                TransferScreen()
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onOpenCards = { accountNumber ->
                        navController.navigate(Screen.Cards.createRoute(accountNumber))
                    },
                    onOpenLoans = {
                        navController.navigate(Screen.Loans.route)
                    }
                )
            }

            composable(Screen.Loans.route) {
                LoanScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Cards.routeWithArg,
                arguments = listOf(
                    navArgument(Screen.Cards.ACCOUNT_NUMBER_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                CardsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Verification.route) {
                VerificationScreen()
            }

            composable(Screen.Exchange.route) {
                ExchangeScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogoutSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Column {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        )

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.2.dp
        ) {
            bottomTabs.forEach { tab ->
                val isSelected = currentRoute == tab.route
                val animatedScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = spring(),
                    label = "bottom-tab-scale"
                )

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (tab.route != null && tab.route != currentRoute) {
                            onNavigate(tab.route)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) {
                                tab.selectedIcon
                            } else {
                                tab.unselectedIcon
                            },
                            contentDescription = tab.label,
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = animatedScale,
                                    scaleY = animatedScale
                                )
                                .background(Color.Transparent)
                        )
                    },
                    label = { Text(tab.label) }
                )
            }
        }
    }
}

fun NavController.navigateToHome() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Login.route) {
            inclusive = true
        }
        launchSingleTop = true
    }
}