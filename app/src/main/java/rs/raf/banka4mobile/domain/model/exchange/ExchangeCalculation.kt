package rs.raf.banka4mobile.domain.model.exchange

data class ExchangeCalculation(
    val amount: Double,
    val fromCurrency: String,
    val toCurrency: String,
    val total: Double
)