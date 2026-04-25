package rs.raf.banka4mobile.data.remote.dto.exchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeCalculateResponseDto(
    val amount: Double,
    @SerialName("from_currency")
    val fromCurrency: String,
    @SerialName("to_currency")
    val toCurrency: String,
    val total: Double
)