package rs.raf.banka4mobile.navigation

import android.net.Uri

sealed class Screen(val route: String) {

    data object Login : Screen("login")

    data object Home : Screen("home")

    data object Cards : Screen("cards") {
        const val ACCOUNT_NUMBER_ARG = "accountNumber"
        val routeWithArg = "$route?$ACCOUNT_NUMBER_ARG={$ACCOUNT_NUMBER_ARG}"

        fun createRoute(accountNumber: String): String {
            return "$route?$ACCOUNT_NUMBER_ARG=${Uri.encode(accountNumber)}"
        }
    }

    data object Verification : Screen("verification")

}