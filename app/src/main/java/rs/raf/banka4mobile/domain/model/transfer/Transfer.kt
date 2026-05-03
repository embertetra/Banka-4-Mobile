package rs.raf.banka4mobile.domain.model.transfer

data class Transfer(
    val transferId: Long,
    val transactionId: Long,
    val fromAccountNumber: String,
    val toAccountNumber: String,
    val initialAmount: Double,
    val finalAmount: Double,
    val commission: Double,
    val exchangeRate: Double,
    val createdAt: String
)