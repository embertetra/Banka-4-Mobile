package rs.raf.banka4mobile.domain.model.home

data class BankLoan(
    val id: Long,
    val amount: Double,
    val currency: String,
    val loanType: String,
    val monthlyInstallment: Double,
    val status: String
)

