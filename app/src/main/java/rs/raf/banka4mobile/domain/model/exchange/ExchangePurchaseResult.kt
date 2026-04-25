package rs.raf.banka4mobile.domain.model.exchange

data class ExchangePurchaseResult(
    val message: String,
    val spentAmount: Double,
    val spentCurrency: String,
    val receivedAmount: Double,
    val receivedCurrency: String
)