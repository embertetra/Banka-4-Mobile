package rs.raf.banka4mobile.data.repository

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import okhttp3.ResponseBody
import rs.raf.banka4mobile.data.auth.AuthSessionCoordinator
import rs.raf.banka4mobile.data.local.session.SessionManager
import rs.raf.banka4mobile.data.remote.api.OrdersApi
import rs.raf.banka4mobile.data.remote.dto.ApiErrorDto
import rs.raf.banka4mobile.data.remote.dto.OrdersResponseDto
import rs.raf.banka4mobile.data.remote.dto.toDomain
import rs.raf.banka4mobile.domain.model.orders.OrdersPage
import rs.raf.banka4mobile.domain.repository.OrdersRepository
import java.io.IOException
import javax.inject.Inject

class OrdersRepositoryImpl @Inject constructor(
    private val ordersApi: OrdersApi,
    private val sessionManager: SessionManager,
    private val json: Json,
    private val authSessionCoordinator: AuthSessionCoordinator
) : OrdersRepository {

    override suspend fun getOrders(page: Int, pageSize: Int): Result<OrdersPage> {
        return withSessionContext { token ->
            val responseBody = ordersApi.getMyOrders(
                authorization = "Bearer $token",
                page = page,
                pageSize = pageSize
            )

            responseBody.toOrdersPage(page, pageSize)
        }
    }

    private fun ResponseBody.toOrdersPage(page: Int, pageSize: Int): OrdersPage {
        val bodyString = string().trim()

        if (bodyString.isEmpty() || bodyString == "null") {
            return OrdersResponseDto(
                data = emptyList(),
                page = page,
                pageSize = pageSize,
                total = 0,
                totalPages = 1
            ).toDomain()
        }

        return json.decodeFromString<OrdersResponseDto>(bodyString).toDomain()
    }

    private suspend fun <T> withSessionContext(
        block: suspend (token: String) -> T
    ): Result<T> {
        val session = sessionManager.getSession()
            ?: return Result.failure(Exception("Nema aktivne sesije. Prijavi se ponovo."))

        return runCatchingNetwork {
            block(session.token)
        }
    }

    private suspend fun <T> runCatchingNetwork(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: HttpException) {
            if (e.code() == 401) {
                authSessionCoordinator.handleUnauthorized()
            }
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