package rs.raf.banka4mobile.navigation

sealed class Screen(val route: String) {

    data object Login : Screen("login")

    data object Home : Screen("home")

    data object Cards : Screen("cards")

    data object Verification : Screen("verification")

}