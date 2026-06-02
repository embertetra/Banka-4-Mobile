package rs.raf.banka4mobile.domain.repository

import rs.raf.banka4mobile.domain.model.orders.OrdersPage

interface OrdersRepository {
    suspend fun getOrders(page: Int = 1, pageSize: Int = 10): Result<OrdersPage>
}