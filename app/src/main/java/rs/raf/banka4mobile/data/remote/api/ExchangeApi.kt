package rs.raf.banka4mobile.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import rs.raf.banka4mobile.data.remote.dto.exchange.ExchangeCalculateResponseDto
import rs.raf.banka4mobile.data.remote.dto.exchange.ExchangeRatesResponseDto

interface ExchangeApi {

    @GET("api/exchange/rates")
    suspend fun getExchangeRates(): ExchangeRatesResponseDto

    @GET("api/exchange/calculate")
    suspend fun calculateExchange(
        @Query("amount") amount: Double,
        @Query("from_currency") fromCurrency: String,
        @Query("to_currency") toCurrency: String
    ): ExchangeCalculateResponseDto
}