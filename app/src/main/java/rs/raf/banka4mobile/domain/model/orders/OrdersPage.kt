package rs.raf.banka4mobile.domain.model.orders

data class OrdersPage(
    val orders: List<Order>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
)