package rs.raf.banka4mobile.domain.model.exchange

data class ExchangeRates(
    val baseCurrency: String,
    val updatedAt: String,
    val nextUpdateAt: String,
    val rates: List<ExchangeRate>
)