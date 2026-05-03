package rs.raf.banka4mobile.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import rs.raf.banka4mobile.data.remote.dto.AccountCardsResponseDto
import rs.raf.banka4mobile.data.remote.dto.AccountDetailsDto
import rs.raf.banka4mobile.data.remote.dto.AccountSummaryDto
import rs.raf.banka4mobile.data.remote.dto.PaymentsResponseDto
import rs.raf.banka4mobile.data.remote.dto.TransferRequestDto
import rs.raf.banka4mobile.data.remote.dto.TransferResponseDto
import rs.raf.banka4mobile.data.remote.dto.TransfersResponseDto

interface BankingApi {

    @GET("clients/{clientId}/accounts")
    suspend fun getAccounts(
        @Header("Authorization") authorization: String,
        @Path("clientId") clientId: Int
    ): List<AccountSummaryDto>

    @GET("clients/{clientId}/accounts/{accountNumber}")
    suspend fun getAccountDetails(
        @Header("Authorization") authorization: String,
        @Path("clientId") clientId: Int,
        @Path("accountNumber") accountNumber: String
    ): AccountDetailsDto

    @GET("clients/{clientId}/accounts/{accountNumber}/cards")
    suspend fun getCards(
        @Header("Authorization") authorization: String,
        @Path("clientId") clientId: Int,
        @Path("accountNumber") accountNumber: String
    ): AccountCardsResponseDto

    @GET("clients/{clientId}/accounts/{accountNumber}/payments")
    suspend fun getPayments(
        @Header("Authorization") authorization: String,
        @Path("clientId") clientId: Int,
        @Path("accountNumber") accountNumber: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): PaymentsResponseDto

    @GET("clients/{clientId}/transfers")
    suspend fun getTransfers(
        @Header("Authorization") authorization: String,
        @Path("clientId") clientId: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): TransfersResponseDto

    @POST("clients/{clientId}/transfers")
    suspend fun createTransfer(
        @Header("Authorization") authorization: String,
        @Path("clientId") clientId: Int,
        @Body request: TransferRequestDto
    ): TransferResponseDto
}