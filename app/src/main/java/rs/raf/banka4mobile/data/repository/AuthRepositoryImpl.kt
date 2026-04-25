package rs.raf.banka4mobile.data.repository

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import rs.raf.banka4mobile.data.local.session.SessionManager
import rs.raf.banka4mobile.data.remote.api.AuthApi
import rs.raf.banka4mobile.data.remote.dto.ApiErrorDto
import rs.raf.banka4mobile.data.remote.dto.LoginRequestDto
import rs.raf.banka4mobile.data.remote.dto.toDomain
import rs.raf.banka4mobile.domain.model.Session
import rs.raf.banka4mobile.domain.model.User
import rs.raf.banka4mobile.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
    private val json: Json
) : AuthRepository {

    companion object {
        /**
         * MOCK LOGIN
         *
         * Kad je true:
         * - ako uneseš dole definisan mock email i password, login će proći bez API-ja
         * - za sve ostalo radiće normalna postojeća logika
         *
         * Kad završiš testiranje:
         * - samo promeni na false
         */
        private const val ENABLE_MOCK_LOGIN = true

        private const val MOCK_EMAIL = "client@test.com"
        private const val MOCK_PASSWORD = "123456"

        private val MOCK_SESSION = Session(
            token = "mock-access-token",
            refreshToken = "mock-refresh-token",
            user = User(
                id = 1,
                email = MOCK_EMAIL,
                firstName = "Petar",
                lastName = "Petrović",
                username = "petar.petrovic",
                identityType = "client",
                permissions = listOf("MOBILE_ACCESS", "VIEW_SECRET")
            )
        )
    }

    override suspend fun login(email: String, password: String): Result<Session> {
        if (ENABLE_MOCK_LOGIN && email == MOCK_EMAIL && password == MOCK_PASSWORD) {
            return Result.success(MOCK_SESSION)
        }

        return try {
            val response = authApi.login(
                LoginRequestDto(
                    email = email,
                    password = password
                )
            )

            val session = response.toDomain()

            if (session.user.identityType.lowercase() != "client") {
                Result.failure(
                    IllegalStateException("Mobilna aplikacija je dostupna samo klijentima.")
                )
            } else {
                Result.success(session)
            }

        } catch (e: HttpException) {
            Result.failure(Exception("Pogrešan email ili lozinka."))
        } catch (e: IOException) {
            Result.failure(Exception("Greška u konekciji sa serverom."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Došlo je do neočekivane greške."))
        }
    }

    override suspend fun saveSession(session: Session) {
        sessionManager.saveSession(session)
    }

    override suspend fun getSession(): Session? {
        return sessionManager.getSession()
    }

    override suspend fun getSecretMobile(): Result<String> {
        val token = sessionManager.getSession()?.token
            ?: return Result.failure(Exception("Nema aktivne sesije."))

        if (ENABLE_MOCK_LOGIN && token == MOCK_SESSION.token) {
            return Result.success("JBSWY3DPEHPK3PXP")
        }

        return try {
            val response = authApi.getSecretMobile(authorization = "Bearer $token")
            Result.success(response.secret)
        } catch (e: HttpException) {
            val errorMessage = parseApiErrorMessage(e)
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            Result.failure(Exception("Greška u konekciji sa serverom."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Došlo je do neočekivane greške."))
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    private fun parseApiErrorMessage(exception: HttpException): String {
        val fallbackMessage = when (exception.code()) {
            401 -> "Niste autorizovani. Prijavite se ponovo."
            403 -> "Nemate dozvolu za ovu akciju."
            404 -> "Traženi resurs nije pronađen."
            else -> "Došlo je do greške na serveru."
        }

        val errorBody = exception.response()?.errorBody()?.string().orEmpty()
        if (errorBody.isBlank()) return fallbackMessage

        return runCatching {
            json.decodeFromString<ApiErrorDto>(errorBody).message
        }.getOrDefault(fallbackMessage)
    }
}