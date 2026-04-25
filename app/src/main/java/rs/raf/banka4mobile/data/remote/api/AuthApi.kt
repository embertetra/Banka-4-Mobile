package rs.raf.banka4mobile.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import rs.raf.banka4mobile.data.remote.dto.LoginRequestDto
import rs.raf.banka4mobile.data.remote.dto.LoginResponseDto
import rs.raf.banka4mobile.data.remote.dto.SecretResponseDto
import rs.raf.banka4mobile.data.remote.dto.exchange.ExchangeCalculateResponseDto
import rs.raf.banka4mobile.data.remote.dto.exchange.ExchangeRatesResponseDto

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto

    @GET("secret-mobile")
    suspend fun getSecretMobile(
        @Header("Authorization") authorization: String
    ): SecretResponseDto

    @GET("exchange/rates")
    suspend fun getExchangeRates(): ExchangeRatesResponseDto

    @GET("exchange/calculate")
    suspend fun calculateExchange(
        @Query("amount") amount: Double,
        @Query("from_currency") fromCurrency: String,
        @Query("to_currency") toCurrency: String
    ): ExchangeCalculateResponseDto

}