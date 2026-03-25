package rs.raf.banka4mobile.data.repository

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import rs.raf.banka4mobile.data.local.session.SessionManager
import rs.raf.banka4mobile.data.remote.api.AuthApi
import rs.raf.banka4mobile.data.remote.dto.ApiErrorDto
import rs.raf.banka4mobile.data.remote.dto.LoginRequestDto
import rs.raf.banka4mobile.data.remote.dto.toDomain
import rs.raf.banka4mobile.domain.model.Session
import rs.raf.banka4mobile.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
    private val json: Json
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Session> {
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