package rs.raf.banka4mobile.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
    val selectedRoute = resolveBottomBarRoute(currentRoute)

    Scaffold(
        bottomBar = {
            if (currentRoute in routesWithBottomBar) {
                CutoutBottomNavigationBar(
                    selectedRoute = selectedRoute,
                    onNavigate = { route ->
                        val isOnChildScreen =
                            currentRoute?.startsWith(Screen.Cards.route) == true ||
                                    currentRoute == Screen.Loans.route

                        if (isOnChildScreen && route == Screen.Home.route) {
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        } else {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
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
                    onLoginSuccess = { navController.navigate(Screen.Home.route) }
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
                ExchangeScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
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

private fun resolveBottomBarRoute(currentRoute: String?): String? {
    return when {
        currentRoute?.startsWith(Screen.Cards.route) == true -> Screen.Home.route
        currentRoute == Screen.Loans.route -> Screen.Home.route
        else -> currentRoute
    }
}

@Composable
private fun CutoutBottomNavigationBar(
    selectedRoute: String?,
    onNavigate: (String) -> Unit
) {
    val circleRadius = 26.dp
    val cornerRadius = 24.dp
    val circleGap = 6.dp

    val buttons = bottomTabs
    val selectedIndex = buttons.indexOfFirst { it.route == selectedRoute }.let { index ->
        if (index == -1) 0 else index
    }

    var barSize by remember { mutableStateOf(IntSize(0, 0)) }
    val offsetStep = remember(barSize, buttons.size) {
        if (buttons.isEmpty() || barSize.width == 0) 0f else {
            barSize.width.toFloat() / (buttons.size * 2)
        }
    }
    val offset = remember(selectedIndex, offsetStep) {
        offsetStep + selectedIndex * 2 * offsetStep
    }

    val circleRadiusPx = LocalDensity.current.run { circleRadius.toPx().toInt() }
    val offsetTransition = updateTransition(offset, label = "cutout-offset")
    val animation = spring<Float>(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)

    val cutoutOffset by offsetTransition.animateFloat(
        transitionSpec = {
            if (this.initialState == 0f) snap() else animation
        },
        label = "cutout-offset-anim"
    ) { it }

    val circleOffset by offsetTransition.animateIntOffset(
        transitionSpec = {
            if (this.initialState == 0f) snap() else spring(
                animation.dampingRatio,
                animation.stiffness
            )
        },
        label = "circle-offset"
    ) {
        IntOffset(it.toInt() - circleRadiusPx, -circleRadiusPx)
    }

    val barShape = remember(cutoutOffset) {
        BarShape(
            offset = cutoutOffset,
            circleRadius = circleRadius,
            cornerRadius = cornerRadius,
            circleGap = circleGap
        )
    }

    val clearRadiusPx = LocalDensity.current.run { (circleRadius + circleGap).toPx() }

    Box {
        val selectedTab = buttons.getOrNull(selectedIndex)
        if (selectedTab != null) {
            CircleButton(
                modifier = Modifier
                    .offset { circleOffset }
                    .zIndex(1f),
                color = MaterialTheme.colorScheme.surface,
                radius = circleRadius,
                icon = selectedTab.selectedIcon,
                contentDescription = selectedTab.label,
                iconColor = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier
                .onPlaced { barSize = it.size }
                .shadow(6.dp, barShape, clip = false)
                .graphicsLayer {
                    shape = barShape
                    clip = true
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .drawWithContent {
                    drawContent()
                    if (cutoutOffset > 0f) {
                        drawCircle(
                            color = Color.Transparent,
                            radius = clearRadiusPx,
                            center = Offset(cutoutOffset, 0f),
                            blendMode = BlendMode.Clear
                        )
                    }
                },
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            buttons.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        tab.route?.let { route ->
                            if (route != selectedRoute) {
                                onNavigate(route)
                            }
                        }
                    },
                    icon = {
                        val iconAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 0f else 1f,
                            label = "bottom-bar-icon-alpha"
                        )
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            modifier = Modifier.alpha(iconAlpha)
                        )
                    },
                    label = { Text(tab.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

private class BarShape(
    private val offset: Float,
    private val circleRadius: Dp,
    private val cornerRadius: Dp,
    private val circleGap: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(getPath(size, density))
    }

    private fun getPath(size: Size, density: Density): Path {
        val cutoutCenterX = offset
        val cutoutRadius = density.run { (circleRadius + circleGap).toPx() }
        val cornerRadiusPx = density.run { cornerRadius.toPx() }
        val cornerDiameter = cornerRadiusPx * 2

        return Path().apply {
            val cutoutEdgeOffset = cutoutRadius * 1.5f
            val cutoutLeftX = cutoutCenterX - cutoutEdgeOffset
            val cutoutRightX = cutoutCenterX + cutoutEdgeOffset

            moveTo(x = 0f, y = size.height)

            if (cutoutLeftX > 0) {
                val realLeftCornerDiameter = if (cutoutLeftX >= cornerRadiusPx) {
                    cornerDiameter
                } else {
                    cutoutLeftX * 2
                }
                arcTo(
                    rect = Rect(
                        left = 0f,
                        top = 0f,
                        right = realLeftCornerDiameter,
                        bottom = realLeftCornerDiameter
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            }

            lineTo(cutoutLeftX, 0f)

            cubicTo(
                x1 = cutoutCenterX - cutoutRadius,
                y1 = 0f,
                x2 = cutoutCenterX - cutoutRadius,
                y2 = cutoutRadius,
                x3 = cutoutCenterX,
                y3 = cutoutRadius
            )
            cubicTo(
                x1 = cutoutCenterX + cutoutRadius,
                y1 = cutoutRadius,
                x2 = cutoutCenterX + cutoutRadius,
                y2 = 0f,
                x3 = cutoutRightX,
                y3 = 0f
            )

            if (cutoutRightX < size.width) {
                val realRightCornerDiameter = if (cutoutRightX <= size.width - cornerRadiusPx) {
                    cornerDiameter
                } else {
                    (size.width - cutoutRightX) * 2
                }
                arcTo(
                    rect = Rect(
                        left = size.width - realRightCornerDiameter,
                        top = 0f,
                        right = size.width,
                        bottom = realRightCornerDiameter
                    ),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            }

            lineTo(x = size.width, y = size.height)
            close()
        }
    }
}

@Composable
private fun CircleButton(
    modifier: Modifier = Modifier,
    color: Color,
    radius: Dp,
    icon: ImageVector,
    contentDescription: String,
    iconColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(radius * 2)
            .shadow(6.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(color)
    ) {
        AnimatedContent(targetState = icon, label = "bottom-bar-circle") { targetIcon ->
            Icon(targetIcon, contentDescription, tint = iconColor)
        }
    }
}
