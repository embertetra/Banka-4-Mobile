package rs.raf.banka4mobile.data.repository

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import rs.raf.banka4mobile.data.local.session.SessionManager
import rs.raf.banka4mobile.data.remote.api.BankingApi
import rs.raf.banka4mobile.data.remote.api.ExchangeApi
import rs.raf.banka4mobile.data.remote.dto.ApiErrorDto
import rs.raf.banka4mobile.data.remote.dto.TransferRequestDto
import rs.raf.banka4mobile.data.remote.dto.exchange.toDomain
import rs.raf.banka4mobile.data.remote.dto.toDomain
import rs.raf.banka4mobile.domain.model.exchange.ExchangePurchaseResult
import rs.raf.banka4mobile.domain.repository.ExchangeRepository
import java.io.IOException
import javax.inject.Inject

class ExchangeRepositoryImpl @Inject constructor(
    private val exchangeApi: ExchangeApi,
    private val bankingApi: BankingApi,
    private val sessionManager: SessionManager,
    private val json: Json
) : ExchangeRepository {

    override suspend fun getExchangeRates() = runCatching {
        exchangeApi.getExchangeRates().toDomain()
    }.recoverCatching { throwable ->
        throw mapToReadableException(throwable)
    }

    override suspend fun calculateExchange(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ) = runCatching {
        exchangeApi.calculateExchange(
            amount = amount,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency
        ).toDomain()
    }.recoverCatching { throwable ->
        throw mapToReadableException(throwable)
    }

    override suspend fun purchaseCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ) = runCatching {
        val session = sessionManager.getSession()
            ?: throw IllegalStateException("Nema aktivne sesije. Prijavite se ponovo.")

        val authorization = "Bearer ${session.token}"
        val clientId = session.user.id

        val normalizedFromCurrency = fromCurrency.uppercase()
        val normalizedToCurrency = toCurrency.uppercase()

        if (normalizedFromCurrency == normalizedToCurrency) {
            throw IllegalArgumentException("Valute ne mogu biti iste.")
        }

        val accounts = bankingApi.getAccounts(
            authorization = authorization,
            clientId = clientId
        )

        val fromAccount = accounts.firstOrNull {
            it.currency.uppercase() == normalizedFromCurrency
        } ?: throw IllegalStateException(
            "Nemate račun u valuti $normalizedFromCurrency. Nije moguće izvršiti kupovinu."
        )

        val toAccount = accounts.firstOrNull {
            it.currency.uppercase() == normalizedToCurrency
        } ?: throw IllegalStateException(
            "Nemate račun u valuti $normalizedToCurrency. Nije moguće izvršiti kupovinu."
        )

        if (amount <= 0.0) {
            throw IllegalArgumentException("Iznos mora biti veći od 0.")
        }

        if (amount > fromAccount.availableBalance) {
            throw IllegalStateException(
                "Nemate dovoljno sredstava na računu ${fromAccount.accountNumber}."
            )
        }

        val transfer = bankingApi.createTransfer(
            authorization = authorization,
            clientId = clientId,
            request = TransferRequestDto(
                amount = amount,
                fromAccount = fromAccount.accountNumber,
                toAccount = toAccount.accountNumber
            )
        ).toDomain()

        ExchangePurchaseResult(
            message = "Uspešno ste kupili valutu.",
            spentAmount = transfer.initialAmount,
            spentCurrency = normalizedFromCurrency,
            receivedAmount = transfer.finalAmount,
            receivedCurrency = normalizedToCurrency
        )
    }.recoverCatching { throwable ->
        throw mapToReadableException(throwable)
    }

    private fun mapToReadableException(throwable: Throwable): Exception {
        return when (throwable) {
            is HttpException -> Exception(parseApiErrorMessage(throwable))
            is IOException -> Exception("Greška u konekciji sa serverom.")
            is IllegalArgumentException -> Exception(throwable.message ?: "Neispravni podaci.")
            is IllegalStateException -> Exception(throwable.message ?: "Akcija nije moguća.")
            else -> Exception(throwable.message ?: "Došlo je do neočekivane greške.")
        }
    }

    private fun parseApiErrorMessage(exception: HttpException): String {
        val fallbackMessage = when (exception.code()) {
            400 -> "Podaci nisu ispravni."
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