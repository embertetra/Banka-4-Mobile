package rs.raf.banka4mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferRequestDto(
    val amount: Double,
    @SerialName("from_account")
    val fromAccount: String,
    @SerialName("to_account")
    val toAccount: String
)

@Serializable
data class TransferResponseDto(
    val commission: Double,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("exchange_rate")
    val exchangeRate: Double,
    @SerialName("final_amount")
    val finalAmount: Double,
    @SerialName("from_account_number")
    val fromAccountNumber: String,
    @SerialName("initial_amount")
    val initialAmount: Double,
    @SerialName("to_account_number")
    val toAccountNumber: String,
    @SerialName("transaction_id")
    val transactionId: Long,
    @SerialName("transfer_id")
    val transferId: Long
)

@Serializable
data class TransfersResponseDto(
    val data: List<TransferResponseDto>,
    val page: Int,
    @SerialName("page_size")
    val pageSize: Int,
    val total: Int,
    @SerialName("total_pages")
    val totalPages: Int
)