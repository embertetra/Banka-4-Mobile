package rs.raf.banka4mobile.domain.repository

import rs.raf.banka4mobile.domain.model.exchange.ExchangeCalculation
import rs.raf.banka4mobile.domain.model.exchange.ExchangePurchaseResult
import rs.raf.banka4mobile.domain.model.exchange.ExchangeRates

interface ExchangeRepository {

    suspend fun getExchangeRates(): Result<ExchangeRates>

    suspend fun calculateExchange(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Result<ExchangeCalculation>

    suspend fun purchaseCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Result<ExchangePurchaseResult>
}