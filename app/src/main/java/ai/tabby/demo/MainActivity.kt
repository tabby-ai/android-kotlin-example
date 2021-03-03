package ai.tabby.demo

import ai.tabby.demo.model.CheckoutSession
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    private lateinit var viewModel: SessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WebView.setWebContentsDebuggingEnabled(true)

        supportFragmentManager.addOnBackStackChangedListener(this)
        shouldDisplayHomeUp()

        viewModel = ViewModelProvider(this).get(SessionViewModel::class.java)
        viewModel.data().observe(this, Observer { event ->
            when (event) {
                is Event.Success -> handleAvailableProducts(event.data)
                is Event.Error -> handleError(event.message, event.error)
                Event.Loading -> handleLoading()
            }
        })

        retry.setOnClickListener { viewModel.request() }
    }

    private fun handleError(msg: String, error: Throwable?) {
        progress.gone()
        message.visible()
        retry.visible()
        payLater.gone()
        payInInstallments.gone()
    }

    private fun handleLoading() {
        progress.visible()
        message.gone()
        retry.gone()
        payLater.gone()
        payInInstallments.gone()
    }

    private fun handleAvailableProducts(data: CheckoutSession) {
        progress.gone()
        message.gone()
        retry.gone()

        val products = data.configuration.availableProducts

        fun Button.showAndCheck(field: String) {
            visible()
            isEnabled = products.containsKey(field)
            setOnClickListener {
                supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, CheckoutFragment.newInstance(field), TAG_CHECKOUT)
                    .addToBackStack(TAG_CHECKOUT)
                    .commitAllowingStateLoss()
            }
        }

        payLater.showAndCheck(PAY_LATER)
        payInInstallments.showAndCheck(INSTALLMENTS)
    }

    override fun onBackStackChanged() {
        shouldDisplayHomeUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStack()
        return true
    }

    private fun shouldDisplayHomeUp() {
        supportActionBar?.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }
}

const val TAG_CHECKOUT = "checkout"
const val PAY_LATER = "pay_later"
const val INSTALLMENTS = "installments"

private fun View.visible() {
    visibility = View.VISIBLE
}

private fun View.gone() {
    visibility = View.GONE
}
