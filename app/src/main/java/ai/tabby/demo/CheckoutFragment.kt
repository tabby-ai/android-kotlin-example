package ai.tabby.demo

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_checkout.*


const val SELECTED_PRODUCT = "selected"

class CheckoutFragment : Fragment() {

    companion object {
        fun newInstance(product: String): CheckoutFragment {
            return CheckoutFragment().apply {
                arguments = Bundle().apply {
                    putString(SELECTED_PRODUCT, product)
                }
            }
        }
    }

    private val product by lazy { requireArguments().getString(SELECTED_PRODUCT)!! }
    private lateinit var viewModel: SessionViewModel
    private var uploadMessageCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity!!.run {
            ViewModelProvider(this).get(SessionViewModel::class.java)
        }

        viewModel.data().observe(this, Observer { event ->
            when(event) {
                is Event.Success -> {
                    val url = makeCheckoutUrl(event.data.id)
                    openWebView(url)
                }

                Event.Loading,
                is Event.Error -> handleError()
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        WebView.setWebContentsDebuggingEnabled(true)
        webview.settings.javaScriptEnabled = true
        webview.settings.allowFileAccess = true
        webview.settings.domStorageEnabled = true

        webview.addJavascriptInterface(TabbyAppListener(), "tabbyMobileSDK")

        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

        webview.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                uploadMessageCallback = filePathCallback
                openImageChooser()

                return true
            }

            private fun openImageChooser() {
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(i, "Image Chooser"),
                    1
                )
            }
        }

        webview.setOnKeyListener { _, _, keyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK && !webview.canGoBack()) {
                false
            } else if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == MotionEvent.ACTION_UP) {
                webview.goBack()
                true
            } else true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = uploadMessageCallback

        if (requestCode != 1 || callback == null) return
        val results = mutableListOf<Uri>()
        if (resultCode == RESULT_OK) {
            if (data != null) {
                val dataString = data.dataString
                val clipData = data.clipData
                if (clipData != null) {
                    results.clear()
                    for (i in 0 .. clipData.itemCount) {
                        val item = clipData.getItemAt(i);
                        results[i] = item.uri
                    }
                }

                if (dataString != null) {
                    results.clear()
                    results.add(Uri.parse(dataString))
                }
            }
        }
        callback.onReceiveValue(results.toTypedArray())
        uploadMessageCallback = null
    }

    private fun handleError() {
        Toast.makeText(activity, getString(R.string.error_message), Toast.LENGTH_LONG).show()
        parentFragmentManager.popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
    }

    override fun onResume() {
        super.onResume()
        webview.onResume()
    }

    override fun onPause() {
        super.onPause()
        webview.onPause()
    }

    private fun makeCheckoutUrl(id: String): String {
        return Uri.parse("https://checkout.tabby.ai/").buildUpon()
            .appendQueryParameter("apiKey", API_KEY)
            .appendQueryParameter("sessionId", id)
            .appendQueryParameter("product", product)
            .build()
            .toString()
    }

    private fun openWebView(url: String) {
        webview.loadUrl(url)
    }

    private inner class TabbyAppListener {
        @JavascriptInterface
        fun postMessage(msg: String) {
            when(msg) {
                "authorized" -> {
                    print("authorized !!!")
                    // PAYMENT IS AUTHORIZED, SAVE ORDER, etc.
                    Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                }
                "rejected" -> {
                    print("rejected !!!")
                    Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                }
                "close" -> {
                    print("close !!!")
                    parentFragmentManager.popBackStack()
                    Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

