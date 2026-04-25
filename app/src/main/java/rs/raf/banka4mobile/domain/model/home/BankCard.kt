package rs.raf.banka4mobile.domain.model.home

data class BankCard(
    val id: Long,
    val accountName: String,
    val accountNumber: String,
    val authorizedPersonId: Int,
    val cardBrand: String,
    val cardNumber: String,
    val cardType: String,
    val expiresAt: String,
    val limit: Double,
    val name: String,
    val status: String
)

