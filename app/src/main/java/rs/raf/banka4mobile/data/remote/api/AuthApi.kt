package rs.raf.banka4mobile.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import rs.raf.banka4mobile.data.remote.dto.LoginRequestDto
import rs.raf.banka4mobile.data.remote.dto.LoginResponseDto
import rs.raf.banka4mobile.data.remote.dto.RefreshTokenRequestDto
import rs.raf.banka4mobile.data.remote.dto.SecretResponseDto

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto

    @POST("auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequestDto
    ): LoginResponseDto

    @GET("secret-mobile")
    suspend fun getSecretMobile(
        @Header("Authorization") authorization: String
    ): SecretResponseDto
}