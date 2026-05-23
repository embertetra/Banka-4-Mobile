package rs.raf.banka4mobile.data.local.settings

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_settings")

@Singleton
class ThemePreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
    }

    val selectedTheme: Flow<AppThemeOption> =
        context.themeDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val savedTheme = preferences[Keys.THEME]

                when (savedTheme) {
                    AppThemeOption.LIGHT.name -> AppThemeOption.LIGHT
                    AppThemeOption.DARK.name -> AppThemeOption.DARK
                    else -> AppThemeOption.SYSTEM
                }
            }

    suspend fun setTheme(theme: AppThemeOption) {
        context.themeDataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }
}