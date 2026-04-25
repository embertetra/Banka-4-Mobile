package rs.raf.banka4mobile.domain.model.home

data class BankAccountDetails(
    val accountNumber: String,
    val name: String,
    val clientId: Int,
    val companyId: Int,
    val employeeId: Int,
    val balance: Double,
    val availableBalance: Double,
    val currency: String,
    val status: String,
    val accountType: String,
    val accountKind: String,
    val dailyLimit: Double,
    val monthlyLimit: Double,
    val dailySpending: Double,
    val monthlySpending: Double,
    val reservedFunds: Double
)

