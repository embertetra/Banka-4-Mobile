package rs.raf.banka4mobile

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import rs.raf.banka4mobile.data.local.settings.AppThemeOption
import rs.raf.banka4mobile.data.local.settings.ThemePreferenceManager
import rs.raf.banka4mobile.navigation.AppNavigation
import rs.raf.banka4mobile.ui.theme.Banka4MobileTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferenceManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val isPhone = resources.configuration.smallestScreenWidthDp < 600
        requestedOrientation = if (isPhone) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        enableEdgeToEdge()

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

            Banka4MobileTheme(
                darkTheme = useDarkTheme
            ) {
                AppNavigation()
            }
        }
    }
}