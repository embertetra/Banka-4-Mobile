package rs.raf.banka4mobile.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.data.notifications.OrderNotificationCoordinator
import rs.raf.banka4mobile.domain.model.orders.Order
import rs.raf.banka4mobile.domain.repository.OrdersRepository
import rs.raf.banka4mobile.presentation.orders.OrdersContract.UiState
import rs.raf.banka4mobile.presentation.orders.OrdersContract.UiEvent
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val ordersRepository: OrdersRepository,
    private val orderNotificationCoordinator: OrderNotificationCoordinator
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.ScreenOpened -> fetchOrders()
            UiEvent.RefreshClicked -> fetchOrders()
            UiEvent.TestNotification -> testNotification()
        }
    }

    private fun fetchOrders() {
        if (state.value.isLoading || state.value.isRefreshing) return

        viewModelScope.launch {
            val isInitialLoad = state.value.orders.isEmpty()

            setState {
                copy(
                    isLoading = isInitialLoad,
                    isRefreshing = !isInitialLoad,
                    errorMessage = null
                )
            }

            ordersRepository.getOrders(page = 1, pageSize = 50)
                .onSuccess { ordersPage ->
                    runCatching {
                        orderNotificationCoordinator.processOrders(ordersPage.orders)
                    }.onFailure { error ->
                        Timber.tag("OrderNotification").e(error, "Failed to process order notifications")
                    }

                    setState {
                        copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                            orders = ordersPage.orders.map { it.toUiItem() },
                            totalOrders = ordersPage.total
                        )
                    }
                }
                .onFailure { error ->
                    setState {
                        copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message ?: "Neuspešno učitavanje naloga."
                        )
                    }
                }
        }
    }

    private fun Order.toUiItem(): OrdersContract.OrderItem {
        return OrdersContract.OrderItem(
            id = id,
            type = orderType,
            ticker = ticker,
            listingName = listingName,
            quantity = quantity,
            pricePerUnit = pricePerUnit,
            status = status,
            createdAt = createdAt,
            executionDate = executionDate,
            commissionCharged = commissionCharged,
            assetType = assetType,
            filledQuantity = filledQuantity,
            remainingQuantity = remainingQuantity,
            isDone = isDone,
            isPartialFill = isPartialFill,
            isCancelled = isCancelled,
            cancelReason = cancelReason
        )
    }

    private fun testNotification() {
        viewModelScope.launch {
            val currentOrders = state.value.orders
            if (currentOrders.isNotEmpty()) {
                val testOrder = currentOrders[0].copy(
                    status = "DONE"
                )
                orderNotificationCoordinator.processOrders(
                    listOf(testOrder.toDomain())
                )
            }
        }
    }

    fun OrdersContract.OrderItem.toDomain(): Order {
        return Order(
            id = id,
            orderType = type,
            ticker = ticker,
            listingName = listingName,
            quantity = quantity,
            pricePerUnit = pricePerUnit,
            status = status,
            createdAt = createdAt,
            executionDate = executionDate,
            commissionCharged = commissionCharged,
            assetType = assetType,
            filledQuantity = filledQuantity,
            remainingQuantity = remainingQuantity,
            isDone = isDone,
            isPartialFill = isPartialFill,
            isCancelled = isCancelled,
            cancelReason = cancelReason
        )
    }
}