package rs.raf.banka4mobile.data.local.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import rs.raf.banka4mobile.data.local.security.CryptoManager
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import rs.raf.banka4mobile.domain.model.Session
import rs.raf.banka4mobile.domain.model.User

private val Context.dataStore by preferencesDataStore(name = "user_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val USERNAME = stringPreferencesKey("username")
        val IDENTITY_TYPE = stringPreferencesKey("identity_type")
        val PERMISSIONS = stringPreferencesKey("permissions")
        val LAST_LOGIN_EMAIL = stringPreferencesKey("last_login_email")

        val QUICK_TOKEN = stringPreferencesKey("quick_token")
        val QUICK_REFRESH_TOKEN = stringPreferencesKey("quick_refresh_token")
        val QUICK_USER_ID = stringPreferencesKey("quick_user_id")
        val QUICK_EMAIL = stringPreferencesKey("quick_email")
        val QUICK_FIRST_NAME = stringPreferencesKey("quick_first_name")
        val QUICK_LAST_NAME = stringPreferencesKey("quick_last_name")
        val QUICK_USERNAME = stringPreferencesKey("quick_username")
        val QUICK_IDENTITY_TYPE = stringPreferencesKey("quick_identity_type")
        val QUICK_PERMISSIONS = stringPreferencesKey("quick_permissions")
    }

    suspend fun saveSession(session: Session) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TOKEN] = cryptoManager.encrypt(session.token)
            preferences[Keys.REFRESH_TOKEN] = cryptoManager.encrypt(session.refreshToken)
            preferences[Keys.USER_ID] = cryptoManager.encrypt(session.user.id.toString())
            preferences[Keys.EMAIL] = cryptoManager.encrypt(session.user.email)
            preferences[Keys.FIRST_NAME] = cryptoManager.encrypt(session.user.firstName)
            preferences[Keys.LAST_NAME] = cryptoManager.encrypt(session.user.lastName)
            preferences[Keys.USERNAME] = cryptoManager.encrypt(session.user.username)
            preferences[Keys.IDENTITY_TYPE] = cryptoManager.encrypt(session.user.identityType)
            preferences[Keys.PERMISSIONS] = cryptoManager.encrypt(session.user.permissions.joinToString(","))

            preferences[Keys.QUICK_TOKEN] = cryptoManager.encrypt(session.token)
            preferences[Keys.QUICK_REFRESH_TOKEN] = cryptoManager.encrypt(session.refreshToken)
            preferences[Keys.QUICK_USER_ID] = cryptoManager.encrypt(session.user.id.toString())
            preferences[Keys.QUICK_EMAIL] = cryptoManager.encrypt(session.user.email)
            preferences[Keys.QUICK_FIRST_NAME] = cryptoManager.encrypt(session.user.firstName)
            preferences[Keys.QUICK_LAST_NAME] = cryptoManager.encrypt(session.user.lastName)
            preferences[Keys.QUICK_USERNAME] = cryptoManager.encrypt(session.user.username)
            preferences[Keys.QUICK_IDENTITY_TYPE] = cryptoManager.encrypt(session.user.identityType)
            preferences[Keys.QUICK_PERMISSIONS] = cryptoManager.encrypt(session.user.permissions.joinToString(","))

            preferences[Keys.LAST_LOGIN_EMAIL] = cryptoManager.encrypt(session.user.email)
        }
    }

    suspend fun getSession(): Session? {
        val preferences = context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .first()

        return readSession(
            preferences = preferences,
            tokenKey = Keys.TOKEN,
            refreshTokenKey = Keys.REFRESH_TOKEN,
            userIdKey = Keys.USER_ID,
            emailKey = Keys.EMAIL,
            firstNameKey = Keys.FIRST_NAME,
            lastNameKey = Keys.LAST_NAME,
            usernameKey = Keys.USERNAME,
            identityTypeKey = Keys.IDENTITY_TYPE,
            permissionsKey = Keys.PERMISSIONS
        )
    }

    suspend fun getQuickLoginSession(): Session? {
        val preferences = context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .first()

        return readSession(
            preferences = preferences,
            tokenKey = Keys.QUICK_TOKEN,
            refreshTokenKey = Keys.QUICK_REFRESH_TOKEN,
            userIdKey = Keys.QUICK_USER_ID,
            emailKey = Keys.QUICK_EMAIL,
            firstNameKey = Keys.QUICK_FIRST_NAME,
            lastNameKey = Keys.QUICK_LAST_NAME,
            usernameKey = Keys.QUICK_USERNAME,
            identityTypeKey = Keys.QUICK_IDENTITY_TYPE,
            permissionsKey = Keys.QUICK_PERMISSIONS
        )
    }

    private fun readSession(
        preferences: Preferences,
        tokenKey: Preferences.Key<String>,
        refreshTokenKey: Preferences.Key<String>,
        userIdKey: Preferences.Key<String>,
        emailKey: Preferences.Key<String>,
        firstNameKey: Preferences.Key<String>,
        lastNameKey: Preferences.Key<String>,
        usernameKey: Preferences.Key<String>,
        identityTypeKey: Preferences.Key<String>,
        permissionsKey: Preferences.Key<String>
    ): Session? {
        val token = cryptoManager.decryptOrNull(preferences[tokenKey]) ?: return null
        val refreshToken = cryptoManager.decryptOrNull(preferences[refreshTokenKey]) ?: return null
        val userIdRaw = cryptoManager.decryptOrNull(preferences[userIdKey]) ?: return null
        val userId = userIdRaw.toIntOrNull() ?: return null
        val email = cryptoManager.decryptOrNull(preferences[emailKey]) ?: return null
        val firstName = cryptoManager.decryptOrNull(preferences[firstNameKey]) ?: return null
        val lastName = cryptoManager.decryptOrNull(preferences[lastNameKey]) ?: return null
        val username = cryptoManager.decryptOrNull(preferences[usernameKey]) ?: return null
        val identityType = cryptoManager.decryptOrNull(preferences[identityTypeKey]) ?: return null
        val permissionsString = cryptoManager.decryptOrNull(preferences[permissionsKey]).orEmpty()

        val permissions = if (permissionsString.isBlank()) {
            emptyList()
        } else {
            permissionsString.split(",")
        }

        return Session(
            token = token,
            refreshToken = refreshToken,
            user = User(
                id = userId,
                email = email,
                firstName = firstName,
                lastName = lastName,
                username = username,
                identityType = identityType,
                permissions = permissions
            )
        )
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            val lastLoginEmail = preferences[Keys.LAST_LOGIN_EMAIL]
            val quickToken = preferences[Keys.QUICK_TOKEN]
            val quickRefreshToken = preferences[Keys.QUICK_REFRESH_TOKEN]
            val quickUserId = preferences[Keys.QUICK_USER_ID]
            val quickEmail = preferences[Keys.QUICK_EMAIL]
            val quickFirstName = preferences[Keys.QUICK_FIRST_NAME]
            val quickLastName = preferences[Keys.QUICK_LAST_NAME]
            val quickUsername = preferences[Keys.QUICK_USERNAME]
            val quickIdentityType = preferences[Keys.QUICK_IDENTITY_TYPE]
            val quickPermissions = preferences[Keys.QUICK_PERMISSIONS]

            preferences.clear()

            if (lastLoginEmail != null) preferences[Keys.LAST_LOGIN_EMAIL] = lastLoginEmail
            if (quickToken != null) preferences[Keys.QUICK_TOKEN] = quickToken
            if (quickRefreshToken != null) preferences[Keys.QUICK_REFRESH_TOKEN] = quickRefreshToken
            if (quickUserId != null) preferences[Keys.QUICK_USER_ID] = quickUserId
            if (quickEmail != null) preferences[Keys.QUICK_EMAIL] = quickEmail
            if (quickFirstName != null) preferences[Keys.QUICK_FIRST_NAME] = quickFirstName
            if (quickLastName != null) preferences[Keys.QUICK_LAST_NAME] = quickLastName
            if (quickUsername != null) preferences[Keys.QUICK_USERNAME] = quickUsername
            if (quickIdentityType != null) preferences[Keys.QUICK_IDENTITY_TYPE] = quickIdentityType
            if (quickPermissions != null) preferences[Keys.QUICK_PERMISSIONS] = quickPermissions
        }
    }

    suspend fun saveLastLoginEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_LOGIN_EMAIL] = cryptoManager.encrypt(email)
        }
    }

    suspend fun getLastLoginEmail(): String? {
        val preferences = context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .first()

        return cryptoManager.decryptOrNull(preferences[Keys.LAST_LOGIN_EMAIL])
    }
}