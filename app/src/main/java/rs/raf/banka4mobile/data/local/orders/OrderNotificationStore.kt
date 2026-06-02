package rs.raf.banka4mobile.data.local.orders

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import rs.raf.banka4mobile.domain.model.orders.Order
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.ordersNotificationDataStore by preferencesDataStore(name = "orders_notifications")

@Singleton
class OrderNotificationStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {

    private object Keys {
        fun snapshot(userId: Int) = stringPreferencesKey("orders_snapshot_$userId")
    }

    @Serializable
    data class StoredOrderSnapshot(
        val orderId: Long,
        val status: String,
        val quantity: Int,
        val filledQuantity: Int? = null,
        val remainingQuantity: Int? = null,
        val isDone: Boolean? = null,
        val isPartialFill: Boolean? = null,
        val isCancelled: Boolean? = null
    )

    suspend fun getSnapshot(userId: Int): List<StoredOrderSnapshot> {
        val preferences = context.ordersNotificationDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .first()

        val rawSnapshot = preferences[Keys.snapshot(userId)] ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<StoredOrderSnapshot>>(rawSnapshot)
        }.getOrDefault(emptyList())
    }

    suspend fun saveSnapshot(userId: Int, orders: List<Order>) {
        val snapshot = orders.map { it.toStoredSnapshot() }
        context.ordersNotificationDataStore.edit { preferences ->
            preferences[Keys.snapshot(userId)] = json.encodeToString(snapshot)
        }
    }

    private fun Order.toStoredSnapshot(): StoredOrderSnapshot {
        return StoredOrderSnapshot(
            orderId = id,
            status = status,
            quantity = quantity,
            filledQuantity = filledQuantity,
            remainingQuantity = remainingQuantity,
            isDone = isDone,
            isPartialFill = isPartialFill,
            isCancelled = isCancelled
        )
    }
}