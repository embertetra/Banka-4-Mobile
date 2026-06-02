package rs.raf.banka4mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrdersResponseDto(
    val data: List<OrderDto>,
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
    val total: Int,
    @SerialName("total_pages") val totalPages: Int? = null
)

@Serializable
data class OrderDto(
    @SerialName("order_id") val orderId: Long,
    @SerialName("order_type") val orderType: String,
    val ticker: String,
    @SerialName("listing_name") val listingName: String,
    val quantity: Int,
    @SerialName("price_per_unit") val pricePerUnit: Double,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("execution_date") val executionDate: String? = null,
    @SerialName("commission_charged") val commissionCharged: Boolean,
    @SerialName("asset_type") val assetType: String,
    @SerialName("filled_quantity") val filledQuantity: Int? = null,
    @SerialName("remaining_quantity") val remainingQuantity: Int? = null,
    @SerialName("is_done") val isDone: Boolean? = null,
    @SerialName("is_partial_fill") val isPartialFill: Boolean? = null,
    @SerialName("is_cancelled") val isCancelled: Boolean? = null,
    @SerialName("cancel_reason") val cancelReason: String? = null
)

