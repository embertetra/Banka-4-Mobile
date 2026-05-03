package rs.raf.banka4mobile.data.repository

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import rs.raf.banka4mobile.data.local.session.SessionManager
import rs.raf.banka4mobile.data.remote.api.BankingApi
import rs.raf.banka4mobile.data.remote.dto.ApiErrorDto
import rs.raf.banka4mobile.data.remote.dto.TransferRequestDto
import rs.raf.banka4mobile.data.remote.dto.toDomain
import rs.raf.banka4mobile.domain.model.transfer.Transfer
import rs.raf.banka4mobile.domain.model.transfer.TransferHistory
import rs.raf.banka4mobile.domain.repository.TransferRepository
import java.io.IOException
import javax.inject.Inject

class TransferRepositoryImpl @Inject constructor(
    private val bankingApi: BankingApi,
    private val sessionManager: SessionManager,
    private val json: Json
) : TransferRepository {

    override suspend fun getTransfers(
        page: Int,
        pageSize: Int
    ): Result<TransferHistory> {
        return withSessionContext { token, clientId ->
            bankingApi.getTransfers(
                authorization = "Bearer $token",
                clientId = clientId,
                page = page,
                pageSize = pageSize
            ).toDomain()
        }
    }

    override suspend fun createTransfer(
        amount: Double,
        fromAccount: String,
        toAccount: String
    ): Result<Transfer> {
        return withSessionContext { token, clientId ->
            bankingApi.createTransfer(
                authorization = "Bearer $token",
                clientId = clientId,
                request = TransferRequestDto(
                    amount = amount,
                    fromAccount = fromAccount,
                    toAccount = toAccount
                )
            ).toDomain()
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
            Result.failure(Exception("Greška u konekciji sa serverom."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Došlo je do neočekivane greške."))
        }
    }

    private fun parseApiErrorMessage(exception: HttpException): String {
        val fallbackMessage = when (exception.code()) {
            400 -> "Podaci za transfer nisu ispravni."
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