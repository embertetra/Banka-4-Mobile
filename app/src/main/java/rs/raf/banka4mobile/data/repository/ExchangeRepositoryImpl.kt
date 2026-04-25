package rs.raf.banka4mobile.data.repository

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import rs.raf.banka4mobile.data.remote.api.AuthApi
import rs.raf.banka4mobile.data.remote.dto.ApiErrorDto
import rs.raf.banka4mobile.data.remote.dto.exchange.ExchangeCalculateResponseDto
import rs.raf.banka4mobile.data.remote.dto.exchange.ExchangeRateDto
import rs.raf.banka4mobile.data.remote.dto.exchange.ExchangeRatesResponseDto
import rs.raf.banka4mobile.data.remote.dto.exchange.toDomain
import rs.raf.banka4mobile.domain.model.exchange.ExchangePurchaseResult
import rs.raf.banka4mobile.domain.repository.ExchangeRepository
import java.io.IOException
import javax.inject.Inject

class ExchangeRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val json: Json
) : ExchangeRepository {

    companion object {
        /**
         * MOCK MODE ZA MENJAČNICU
         *
         * true  -> koristi mock podatke i mock kupovinu
         * false -> koristi realne API pozive za rates i calculate
         *
         * Napomena:
         * za purchase trenutno ne postoji prikazan API endpoint na slikama,
         * pa će purchase za sada ostati mock i kada je ovo false.
         */
        private const val ENABLE_EXCHANGE_MOCK = true
    }

    override suspend fun getExchangeRates() = runCatching {
        if (ENABLE_EXCHANGE_MOCK) {
            mockRatesResponse().toDomain()
        } else {
            authApi.getExchangeRates().toDomain()
        }
    }.recoverCatching { throwable ->
        throw mapToReadableException(throwable)
    }

    override suspend fun calculateExchange(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ) = runCatching {
        if (ENABLE_EXCHANGE_MOCK) {
            mockCalculate(amount, fromCurrency, toCurrency).toDomain()
        } else {
            authApi.calculateExchange(
                amount = amount,
                fromCurrency = fromCurrency,
                toCurrency = toCurrency
            ).toDomain()
        }
    }.recoverCatching { throwable ->
        throw mapToReadableException(throwable)
    }

    override suspend fun purchaseCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ) = runCatching {
        val calculation = if (ENABLE_EXCHANGE_MOCK) {
            mockCalculate(amount, fromCurrency, toCurrency).toDomain()
        } else {
            /**
             * Ovde trenutno ostavljamo isti calculate poziv kao osnovu za preview kupovine,
             * jer na slikama nije prikazan poseban endpoint za stvarnu kupovinu.
             * Kada dobiješ pravi endpoint, ovde ćemo zameniti ovu logiku.
             */
            authApi.calculateExchange(
                amount = amount,
                fromCurrency = fromCurrency,
                toCurrency = toCurrency
            ).toDomain()
        }

        ExchangePurchaseResult(
            message = "Uspešno ste zamenili valutu.",
            spentAmount = calculation.amount,
            spentCurrency = calculation.fromCurrency,
            receivedAmount = calculation.total,
            receivedCurrency = calculation.toCurrency
        )
    }.recoverCatching { throwable ->
        throw mapToReadableException(throwable)
    }

    private fun mockRatesResponse(): ExchangeRatesResponseDto {
        return ExchangeRatesResponseDto(
            baseCurrency = "RSD",
            nextUpdateAt = "2026-04-15T12:00:00Z",
            updatedAt = "2026-04-15T09:00:00Z",
            rates = listOf(
                ExchangeRateDto(
                    buyRate = 115.62,
                    currency = "EUR",
                    middleRate = 117.3850,
                    sellRate = 119.15
                ),
                ExchangeRateDto(
                    buyRate = 100.19,
                    currency = "USD",
                    middleRate = 101.7150,
                    sellRate = 103.24
                ),
                ExchangeRateDto(
                    buyRate = 125.45,
                    currency = "CHF",
                    middleRate = 127.3600,
                    sellRate = 129.27
                ),
                ExchangeRateDto(
                    buyRate = 132.61,
                    currency = "GBP",
                    middleRate = 134.6300,
                    sellRate = 136.65
                ),
                ExchangeRateDto(
                    buyRate = 0.63,
                    currency = "JPY",
                    middleRate = 0.6400,
                    sellRate = 0.65
                ),
                ExchangeRateDto(
                    buyRate = 71.95,
                    currency = "CAD",
                    middleRate = 73.0450,
                    sellRate = 74.14
                ),
                ExchangeRateDto(
                    buyRate = 69.32,
                    currency = "AUD",
                    middleRate = 70.3750,
                    sellRate = 71.43
                )
            )
        )
    }

    private fun mockCalculate(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): ExchangeCalculateResponseDto {
        val rates = mockRatesResponse().rates

        val total = convertLocally(
            amount = amount,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency,
            rates = rates
        )

        return ExchangeCalculateResponseDto(
            amount = amount,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency,
            total = total
        )
    }

    private fun convertLocally(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        rates: List<ExchangeRateDto>
    ): Double {
        if (fromCurrency == toCurrency) return amount

        if (fromCurrency == "RSD" && toCurrency != "RSD") {
            val targetRate = rates.firstOrNull { it.currency == toCurrency } ?: return 0.0
            return amount / targetRate.sellRate
        }

        if (fromCurrency != "RSD" && toCurrency == "RSD") {
            val sourceRate = rates.firstOrNull { it.currency == fromCurrency } ?: return 0.0
            return amount * sourceRate.buyRate
        }

        val sourceRate = rates.firstOrNull { it.currency == fromCurrency } ?: return 0.0
        val targetRate = rates.firstOrNull { it.currency == toCurrency } ?: return 0.0

        val rsdValue = amount * sourceRate.buyRate
        return rsdValue / targetRate.sellRate
    }

    private fun mapToReadableException(throwable: Throwable): Exception {
        return when (throwable) {
            is HttpException -> {
                val fallbackMessage = when (throwable.code()) {
                    400 -> "Neispravni parametri za preračun valute."
                    404 -> "Traženi kurs nije pronađen."
                    500 -> "Greška na serveru."
                    else -> "Došlo je do greške prilikom rada sa menjačnicom."
                }

                val errorBody = throwable.response()?.errorBody()?.string().orEmpty()

                val parsedMessage = runCatching {
                    if (errorBody.isBlank()) null
                    else json.decodeFromString<ApiErrorDto>(errorBody).message
                }.getOrNull()

                Exception(parsedMessage ?: fallbackMessage)
            }

            is IOException -> Exception("Greška u konekciji sa serverom.")
            is Exception -> Exception(throwable.message ?: "Došlo je do neočekivane greške.")
            else -> Exception("Nepoznata greška.")
        }
    }
}