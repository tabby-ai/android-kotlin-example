package ai.tabby.demo

import ai.tabby.demo.model.Api
import ai.tabby.demo.model.CheckoutSession
import ai.tabby.demo.model.NetworkService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class SessionViewModel : ViewModel() {
    private var api: Api = NetworkService.retrofitService()

    private val liveData = MutableLiveData<Event>()

    init {
        request()
    }

    fun data(): LiveData<Event> = liveData

    fun request() {
        liveData.postValue(Event.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.checkout(AUTH_TOKEN, SESSION_REQUEST_BODY)
                liveData.postValue(Event.Success(response))
            } catch (e: Exception) {
                e.printStackTrace()
                liveData.postValue(Event.Error("error", e))
            }
        }
    }
}

sealed class Event {
    var isHandled: Boolean = false

    data class Success(val data: CheckoutSession) : Event()
    data class Error(val message: String, val error: Throwable?) : Event()
    object Loading : Event()
}

const val API_KEY = "API_KEY_HERE" // ENTER YOUR KEY HERE
private const val AUTH_TOKEN = "Bearer $API_KEY"

private val SESSION_REQUEST_BODY = """
{
  "lang": "en",
  "merchant_code": "ae",
  "payment": {
    "amount": 340,
    "buyer": {
      "dob": "1987-10-20",
      "email": "successful.payment@tabby.ai",
      "name": "John Doe",
      "phone": "0500000001"
    },
    "buyer_history": {
      "loyalty_level": 10,
      "registered_since": "2019-10-05T17:45:17+00:00",
      "wishlist_count": 421
    },
    "currency": "AED",
    "description": "Tabby Store Order #3",
    "order": {
      "items": [
        {
          "description": "To be displayed in Tabby order information",
          "product_url": "https://tabby.store/p/SKU123",
          "quantity": 1,
          "reference_id": "SKU123",
          "title": "Sample Item #1",
          "unit_price": "300"
        },
        {
          "description": "To be displayed in Tabby order information",
          "product_url": "https://tabby.store/p/SKU124",
          "quantity": 1,
          "reference_id": "SKU124",
          "title": "Sample Item #2",
          "unit_price": "600"
        }
      ],
      "reference_id": "xxxx-xxxxxx-xxxx",
      "shipping_amount": "50",
      "tax_amount": "50"
    },
    "order_history": [
      {
        "amount": "1000",
        "buyer": {
          "name": "John Doe",
          "phone": "+971-505-5566-33"
        },
        "items": [
          {
            "quantity": 4,
            "title": "Sample Item #3",
            "unit_price": "250",
            "reference_id": "item-sku",
            "ordered": 4,
            "captured": 4,
            "shipped": 4,
            "refunded": 1
          }
        ],
        "payment_method": "CoD",
        "purchased_at": "2019-10-05T18:45:17+00:00",
        "shipping_address": {
          "address": "Sample Address #1",
          "city": "Dubai"
        },
        "status": "complete"
      }
    ],
    "shipping_address": {
      "address": "Sample Address #2",
      "city": "Dubai"
    }
  }
}""".trimIndent().toRequestBody("application/json".toMediaType())