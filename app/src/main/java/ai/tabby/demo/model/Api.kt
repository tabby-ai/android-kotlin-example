package ai.tabby.demo.model

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface Api {
    @POST("v2/checkout")
    suspend fun checkout(@Header("Authorization") token: String, @Body body: RequestBody): CheckoutSession
}