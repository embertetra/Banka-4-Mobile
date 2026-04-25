package rs.raf.banka4mobile.data.remote.dto.exchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRatesResponseDto(
    @SerialName("base_currency")
    val baseCurrency: String,
    @SerialName("next_update_at")
    val nextUpdateAt: String,
    val rates: List<ExchangeRateDto>,
    @SerialName("updated_at")
    val updatedAt: String
)