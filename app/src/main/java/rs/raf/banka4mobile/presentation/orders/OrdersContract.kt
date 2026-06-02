package rs.raf.banka4mobile.presentation.orders

interface OrdersContract {

    data class UiState(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null,
        val orders: List<OrderItem> = emptyList(),
        val totalOrders: Int = 0
    )

    data class OrderItem(
        val id: Long,
        val type: String,
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

    sealed class UiEvent {
        data object ScreenOpened : UiEvent()
        data object RefreshClicked : UiEvent()
        data object TestNotification : UiEvent()
    }
}