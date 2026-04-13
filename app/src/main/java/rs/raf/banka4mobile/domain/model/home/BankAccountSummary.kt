package rs.raf.banka4mobile.domain.model.home

data class BankAccountSummary(
    val accountNumber: String,
    val name: String,
    val accountType: String,
    val accountKind: String,
    val currency: String,
    val balance: Double,
    val availableBalance: Double,
    val reservedFunds: Double
)

