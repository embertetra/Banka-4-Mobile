package rs.raf.banka4mobile.data.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import rs.raf.banka4mobile.R
import rs.raf.banka4mobile.domain.model.orders.Order
import rs.raf.banka4mobile.data.local.orders.OrderNotificationStore.StoredOrderSnapshot
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class OrderChangeType {
    PENDING,
    APPROVED,
    DECLINED,
    DONE,
    PARTIAL_FILL,
    AUTOMATIC_CANCEL
}

@Singleton
class OrderNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "orders_status_updates"
        private const val CHANNEL_NAME = "Order updates"
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikacije kada se order izvrši, delimično izvrši ili otkaže."
                enableVibration(true)
                enableLights(true)
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showIfAllowed(order: Order, changeType: OrderChangeType): Boolean {
        if (!canPostNotifications()) {
            Timber.tag("OrderNotification").d("Cannot post notifications - permission missing")
            return false
        }

        ensureChannel()

        val title = when (changeType) {
            OrderChangeType.DONE -> "Order izvršen"
            OrderChangeType.PARTIAL_FILL -> "Order delimično izvršen"
            OrderChangeType.AUTOMATIC_CANCEL -> "Order automatski otkazan"
            OrderChangeType.PENDING -> "Order na odobrenju"
            OrderChangeType.APPROVED -> "Order odobren"
            OrderChangeType.DECLINED -> "Order odbijen"
        }

        val body = buildString {
            append("${order.ticker} • ${order.listingName}")
            append("\nKoličina: ${order.quantity}")
            append("\nStatus: ${order.status}")
            order.cancelReason?.takeIf { it.isNotBlank() }?.let {
                append("\nRazlog: $it")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("${order.ticker} — ${order.status}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(order.id.toInt(), notification)
            Timber.tag("OrderNotification").d("Notification sent successfully")
            true
        } catch (e: SecurityException) {
            Timber.tag("OrderNotification").e("SecurityException: ${e.message}")
            false
        }
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

/* --- helper / detection functions --- */

internal fun StoredOrderSnapshot.toChangeType(): OrderChangeType? {
    return when {
        status.matchesPendingStatus() -> OrderChangeType.PENDING
        status.matchesApprovedStatus() -> OrderChangeType.APPROVED
        status.matchesDeclinedStatus() -> OrderChangeType.DECLINED
        isDone == true || status.matchesDoneStatus() -> OrderChangeType.DONE
        isPartialFill == true || status.matchesPartialFillStatus() -> OrderChangeType.PARTIAL_FILL
        isCancelled == true || status.matchesCancelledStatus() -> OrderChangeType.AUTOMATIC_CANCEL
        filledQuantity != null && quantity > 0 && filledQuantity >= quantity -> OrderChangeType.DONE
        filledQuantity != null && quantity > 0 && filledQuantity in 1 until quantity -> OrderChangeType.PARTIAL_FILL
        else -> null
    }
}

internal fun Order.toChangeType(): OrderChangeType? {
    return when {
        status.matchesPendingStatus() -> OrderChangeType.PENDING
        status.matchesApprovedStatus() -> OrderChangeType.APPROVED
        status.matchesDeclinedStatus() -> OrderChangeType.DECLINED
        isDone == true || status.matchesDoneStatus() -> OrderChangeType.DONE
        isPartialFill == true || status.matchesPartialFillStatus() -> OrderChangeType.PARTIAL_FILL
        isCancelled == true || status.matchesCancelledStatus() -> OrderChangeType.AUTOMATIC_CANCEL
        filledQuantity != null && quantity > 0 && filledQuantity >= quantity -> OrderChangeType.DONE
        filledQuantity != null && quantity > 0 && filledQuantity in 1 until quantity -> OrderChangeType.PARTIAL_FILL
        else -> null
    }
}

private fun String.matchesDoneStatus(): Boolean {
    return uppercase() in setOf("DONE", "FILLED", "EXECUTED", "COMPLETED", "SETTLED")
}

private fun String.matchesPartialFillStatus(): Boolean {
    return uppercase() in setOf("PARTIAL", "PARTIAL_FILL", "PARTIALLY_FILLED", "PARTIALLY_EXECUTED")
}

private fun String.matchesCancelledStatus(): Boolean {
    return uppercase() in setOf(
        "CANCELLED",
        "CANCELED",
        "AUTO_CANCELLED",
        "AUTO_CANCELED",
        "EXPIRED"
    )
}

private fun String.matchesApprovedStatus(): Boolean {
    return uppercase() in setOf("APPROVED", "ACCEPTED", "AUTHORISED", "AUTHORIZED")
}

private fun String.matchesDeclinedStatus(): Boolean {
    return uppercase() in setOf("DECLINED", "REJECTED", "DENIED")
}

private fun String.matchesPendingStatus(): Boolean {
    return uppercase() in setOf("PENDING", "AWAITING_APPROVAL", "ON_HOLD", "FOR_APPROVAL")
}