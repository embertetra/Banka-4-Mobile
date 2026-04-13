package rs.raf.banka4mobile.data.remote.dto.exchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateDto(
    @SerialName("buy_rate")
    val buyRate: Double,
    val currency: String,
    @SerialName("middle_rate")
    val middleRate: Double,
    @SerialName("sell_rate")
    val sellRate: Double
)