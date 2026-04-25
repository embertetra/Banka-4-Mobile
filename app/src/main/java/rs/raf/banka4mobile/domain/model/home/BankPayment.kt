package rs.raf.banka4mobile.domain.model.home

data class BankPayment(
    val id: Long,
    val amount: Double,
    val createdAt: String,
    val currency: String,
    val payerAccount: String,
    val paymentCode: String,
    val purpose: String,
    val recipientAccount: String,
    val recipientName: String,
    val status: String
)

