package rs.raf.banka4mobile.domain.model.exchange

data class ExchangeRate(
    val currencyCode: String,
    val currencyName: String,
    val buyRate: Double,
    val middleRate: Double,
    val sellRate: Double
)