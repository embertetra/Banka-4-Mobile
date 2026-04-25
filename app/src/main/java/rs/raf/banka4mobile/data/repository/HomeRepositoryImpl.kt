package rs.raf.banka4mobile.data.repository

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import rs.raf.banka4mobile.data.local.session.SessionManager
import rs.raf.banka4mobile.data.remote.api.BankingApi
import rs.raf.banka4mobile.data.remote.dto.ApiErrorDto
import rs.raf.banka4mobile.data.remote.dto.toDomain
import rs.raf.banka4mobile.domain.model.home.BankAccountDetails
import rs.raf.banka4mobile.domain.model.home.BankAccountSummary
import rs.raf.banka4mobile.domain.model.home.BankCard
import rs.raf.banka4mobile.domain.model.home.BankPayment
import rs.raf.banka4mobile.domain.repository.HomeRepository
import java.io.IOException
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val bankingApi: BankingApi,
    private val sessionManager: SessionManager,
    private val json: Json
) : HomeRepository {

    override suspend fun getAccounts(): Result<List<BankAccountSummary>> {
        return withSessionContext { token, clientId ->
            bankingApi.getAccounts(
                authorization = "Bearer $token",
                clientId = clientId
            ).map { it.toDomain() }
        }
    }

    override suspend fun getAccountDetails(accountNumber: String): Result<BankAccountDetails> {
        return withSessionContext { token, clientId ->
            bankingApi.getAccountDetails(
                authorization = "Bearer $token",
                clientId = clientId,
                accountNumber = accountNumber
            ).toDomain()
        }
    }

    override suspend fun getCards(accountNumber: String): Result<List<BankCard>> {
        return withSessionContext { token, clientId ->
            bankingApi.getCards(
                authorization = "Bearer $token",
                clientId = clientId,
                accountNumber = accountNumber
            ).cards.map { it.toDomain() }
        }
    }

    override suspend fun getPayments(accountNumber: String): Result<List<BankPayment>> {
        return withSessionContext { token, clientId ->
            bankingApi.getPayments(
                authorization = "Bearer $token",
                clientId = clientId,
                accountNumber = accountNumber
            ).data.map { it.toDomain() }
        }
    }

    private suspend fun <T> withSessionContext(
        block: suspend (token: String, clientId: Int) -> T
    ): Result<T> {
        val session = sessionManager.getSession()
            ?: return Result.failure(Exception("Nema aktivne sesije. Prijavi se ponovo."))

        return runCatchingNetwork {
            block(session.token, session.user.id)
        }
    }

    private suspend fun <T> runCatchingNetwork(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: HttpException) {
            Result.failure(Exception(parseApiErrorMessage(e)))
        } catch (_: IOException) {
            Result.failure(Exception("Greska u konekciji sa serverom."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Doslo je do neocekivane greske."))
        }
    }

    private fun parseApiErrorMessage(exception: HttpException): String {
        val fallbackMessage = when (exception.code()) {
            401 -> "Niste autorizovani. Prijavite se ponovo."
            403 -> "Nemate dozvolu za ovu akciju."
            404 -> "Trazeni resurs nije pronadjen."
            else -> "Doslo je do greske na serveru."
        }

        val errorBody = exception.response()?.errorBody()?.string().orEmpty()
        if (errorBody.isBlank()) return fallbackMessage

        return runCatching {
            json.decodeFromString<ApiErrorDto>(errorBody).message
        }.getOrDefault(fallbackMessage)
    }
}


