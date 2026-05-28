package rs.raf.banka4mobile

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import rs.raf.banka4mobile.data.local.settings.AppThemeOption
import rs.raf.banka4mobile.data.local.settings.ThemePreferenceManager
import rs.raf.banka4mobile.navigation.AppNavigation
import rs.raf.banka4mobile.ui.theme.Banka4MobileTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var themePreferenceManager: ThemePreferenceManager

    private fun applySystemBarStyle(useDarkTheme: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                "#00000000".toColorInt(),
                "#00000000".toColorInt(),
                detectDarkMode = { _ -> useDarkTheme }
            ),
            navigationBarStyle = SystemBarStyle.auto(
                "#FFFFFFFF".toColorInt(),
                "#FF000000".toColorInt(),
                detectDarkMode = { _ -> useDarkTheme }
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val isPhone = resources.configuration.smallestScreenWidthDp < 600
        requestedOrientation = if (isPhone) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        applySystemBarStyle(useDarkTheme = false)
        setContent {
            val selectedTheme = themePreferenceManager.selectedTheme
                .collectAsStateWithLifecycle(initialValue = AppThemeOption.SYSTEM)
                .value

            val isSystemDark = isSystemInDarkTheme()

            val useDarkTheme = when (selectedTheme) {
                AppThemeOption.SYSTEM -> isSystemDark
                AppThemeOption.LIGHT -> false
                AppThemeOption.DARK -> true
            }

            SideEffect {
                applySystemBarStyle(useDarkTheme)
            }

            Banka4MobileTheme(
                darkTheme = useDarkTheme
            ) {
                AppNavigation()
            }
        }
    }

}