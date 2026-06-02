package rs.raf.banka4mobile.data.notifications

import rs.raf.banka4mobile.data.local.orders.OrderNotificationStore
import rs.raf.banka4mobile.data.local.session.SessionManager
import rs.raf.banka4mobile.domain.model.orders.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderNotificationCoordinator @Inject constructor(
    private val sessionManager: SessionManager,
    private val notificationStore: OrderNotificationStore,
    private val notificationManager: OrderNotificationManager
) {

    suspend fun processOrders(orders: List<Order>) {
        val session = sessionManager.getSession() ?: return
        val userId = session.user.id

        val previousSnapshot = notificationStore.getSnapshot(userId)
            .associateBy { it.orderId }

        if (previousSnapshot.isEmpty()) {
            notificationStore.saveSnapshot(userId, orders)
            return
        }

        orders.forEach { order ->
            val previousOrder = previousSnapshot[order.id] ?: return@forEach
            val previousChangeType = previousOrder.toChangeType()
            val currentChangeType = order.toChangeType()

            if (currentChangeType != null && currentChangeType != previousChangeType) {
                notificationManager.showIfAllowed(order, currentChangeType)
            }
        }

        notificationStore.saveSnapshot(userId, orders)
    }
}