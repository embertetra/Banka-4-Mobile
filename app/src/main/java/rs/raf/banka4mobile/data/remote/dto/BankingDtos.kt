package rs.raf.banka4mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountSummaryDto(
    @SerialName("account_number") val accountNumber: String,
    val name: String,
    @SerialName("account_type") val accountType: String,
    @SerialName("account_kind") val accountKind: String,
    val currency: String,
    val balance: Double,
    @SerialName("available_balance") val availableBalance: Double,
    @SerialName("reserved_funds") val reservedFunds: Double
)

@Serializable
data class AccountDetailsDto(
    @SerialName("account_number") val accountNumber: String,
    val name: String,
    @SerialName("client_id") val clientId: Int,
    @SerialName("company_id") val companyId: Int,
    @SerialName("employee_id") val employeeId: Int,
    val balance: Double,
    @SerialName("available_balance") val availableBalance: Double,
    val currency: String,
    val status: String,
    @SerialName("account_type") val accountType: String,
    @SerialName("account_kind") val accountKind: String,
    @SerialName("daily_limit") val dailyLimit: Double,
    @SerialName("monthly_limit") val monthlyLimit: Double,
    @SerialName("daily_spending") val dailySpending: Double,
    @SerialName("monthly_spending") val monthlySpending: Double,
    @SerialName("reserved_funds") val reservedFunds: Double
)

@Serializable
data class AccountCardsResponseDto(
    @SerialName("account_name") val accountName: String,
    @SerialName("account_number") val accountNumber: String,
    val cards: List<CardDto>
)

@Serializable
data class CardDto(
    val id: Long,
    @SerialName("account_name") val accountName: String,
    @SerialName("account_number") val accountNumber: String,
    @SerialName("authorized_person_id") val authorizedPersonId: Int,
    @SerialName("card_brand") val cardBrand: String,
    @SerialName("card_number") val cardNumber: String,
    @SerialName("card_type") val cardType: String,
    @SerialName("expires_at") val expiresAt: String,
    val limit: Double,
    val name: String,
    val status: String
)

@Serializable
data class PaymentsResponseDto(
    val data: List<PaymentDto>,
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
    val total: Int,
    @SerialName("total_pages") val totalPages: Int
)

@Serializable
data class PaymentDto(
    val id: Long,
    val amount: Double,
    @SerialName("created_at") val createdAt: String,
    val currency: String,
    @SerialName("payer_account") val payerAccount: String,
    @SerialName("payment_code") val paymentCode: String,
    val purpose: String,
    @SerialName("recipient_account") val recipientAccount: String,
    @SerialName("recipient_name") val recipientName: String,
    val status: String
)