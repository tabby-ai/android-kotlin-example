package ai.tabby.demo.model

import com.google.gson.annotations.SerializedName

class CheckoutSession(
    val id: String,
    val configuration: Configuration
)

class Configuration(
    @SerializedName("available_products")
    val availableProducts: Map<String, Any>
)
