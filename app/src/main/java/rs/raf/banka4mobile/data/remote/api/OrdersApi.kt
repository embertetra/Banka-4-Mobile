package rs.raf.banka4mobile.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface OrdersApi {

    @GET("orders/my")
    suspend fun getMyOrders(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ResponseBody
}