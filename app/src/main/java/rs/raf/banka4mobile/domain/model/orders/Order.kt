package rs.raf.banka4mobile.domain.model.orders

data class Order(
    val id: Long,
    val orderType: String,
    val ticker: String,
    val listingName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val status: String,
    val createdAt: String,
    val executionDate: String?,
    val commissionCharged: Boolean,
    val assetType: String,
    val filledQuantity: Int? = null,
    val remainingQuantity: Int? = null,
    val isDone: Boolean? = null,
    val isPartialFill: Boolean? = null,
    val isCancelled: Boolean? = null,
    val cancelReason: String? = null
)