package rs.raf.banka4mobile.domain.repository

import rs.raf.banka4mobile.domain.model.Session

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<Session>

    suspend fun saveSession(session: Session)

    suspend fun getSession(): Session?

    suspend fun getQuickLoginSession(): Session?

    suspend fun saveLastLoginEmail(email: String)

    suspend fun getLastLoginEmail(): String?

    suspend fun getSecretMobile(): Result<String>

    suspend fun logout()
}