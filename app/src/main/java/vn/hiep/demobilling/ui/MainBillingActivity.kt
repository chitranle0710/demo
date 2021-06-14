package vn.hiep.demobilling.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_subscribe.*
import vn.hiep.demobilling.BillingApplication
import vn.hiep.demobilling.R
import vn.hiep.demobilling.adapter.BillingAdapter
import vn.hiep.demobilling.base.BaseActivity
import vn.hiep.demobilling.data.repository.BillingRepository
import vn.hiep.demobilling.databinding.ActivityMainBillingBinding
import vn.hiep.demobilling.domain.model.Product
import vn.hiep.demobilling.ui.MainActivityViewModel.MainActivityViewModelFactory
import vn.hiep.demobilling.utils.getContentView
import vn.hiep.demobilling.utils.startActivity


class MainBillingActivity : BaseActivity(R.layout.activity_main_billing) {
    private lateinit var binding: ActivityMainBillingBinding
    private lateinit var viewModel: MainActivityViewModel

    val listProduct: MutableList<Product> = ArrayList()
    val listSubscription: MutableList<Product> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBillingBinding.bind(getContentView()!!)

        setSupportActionBar(binding.toolbar)

        val mainActivityViewModelFactory = MainActivityViewModelFactory(
            (application as BillingApplication).appContainer.billingRepository
        )
        viewModel = ViewModelProvider(this, mainActivityViewModelFactory)
            .get(MainActivityViewModel::class.java)

        createBillingList()
        observeViewModel()
    }

    private fun createBillingList() {
        // add product
        val products = listOf(
            BillingRepository.SKU_SERVICE_1,
            BillingRepository.SKU_SERVICE_2
        )
        // add header
        listProduct.add(
            Product(
                name = getString(R.string.txt_in_app_products)
            )
        )
        products.forEach { productId ->
            listProduct.add(Product(productId))
        }

        // add subscription
        val subscriptions = listOf(
            BillingRepository.SKU_ESSENTIAL_MONTHLY,
            BillingRepository.SKU_ESSENTIAL_YEARLY,
            BillingRepository.SKU_PREMIUM_MONTHLY,
            BillingRepository.SKU_PREMIUM_YEARLY,
        )

        // add header
        listSubscription.add(
            Product(
                name = getString(R.string.txt_in_app_subscription)
            )
        )
        subscriptions.forEach { productId ->
            listSubscription.add(Product(productId))
        }

        val productAdapter = BillingAdapter(listProduct).apply {
            onClick = { product ->
                makePurchase(product.productId)
            }
        }

        val subscriptionAdapter = BillingAdapter(listSubscription).apply {
            onClick = { product ->
                makePurchase(product.productId)
            }
        }

        val adapter = ConcatAdapter(productAdapter, subscriptionAdapter)
        recyclerView.layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0 || position == listProduct.size) 2 else 1
                }
            }
        }

        recyclerView.adapter = adapter
        val allProduct = listProduct + listSubscription
        for (s in allProduct) {
            if (s.productId != null) {
                val detail = viewModel.getSkuDetails(s.productId)
                detail.title.observe { combineProductDetail(detail) }
                detail.description.observe { combineProductDetail(detail) }
                detail.price.observe { combineProductDetail(detail) }
                viewModel.isPurchased(s.productId).observe { isPurchased ->
                    updateProductPurchased(s.productId, isPurchased)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe { stringId ->
            Snackbar.make(binding.root, stringId, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.errorMessage.observe { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.consumedCount.observe { list ->
            val allProduct = listProduct + listSubscription
            for (product in allProduct) {
                if (list.contains(product.productId)) {
                    product.quantity++
                }
            }
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }
    }


    private fun updateProductPurchased(productId: String, isPurchased: Boolean) {
        var updateProduct = listProduct.firstOrNull { it.productId == productId }
        if (updateProduct == null) {
            updateProduct = listSubscription.firstOrNull { it.productId == productId }
        }
        if (updateProduct != null) {
            updateProduct.isPurchased = isPurchased
            binding.recyclerView.adapter!!.notifyDataSetChanged()
        }
    }

    private fun combineProductDetail(skuDetails: MainActivityViewModel.SkuDetails) {
        val title = skuDetails.title.value
        val description = skuDetails.description.value
        val price = skuDetails.price.value
        // don't emit until we have all of our data
        if (null == title || null == description || null == price) {
            return
        }
        var updateProduct = listProduct.firstOrNull { it.productId == skuDetails.productId }
        if (updateProduct == null) {
            updateProduct = listSubscription.firstOrNull { it.productId == skuDetails.productId }
        }
        if (updateProduct != null) {
            updateProduct.name = title
            updateProduct.description = description
            updateProduct.price = price
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun makePurchase(sku: String?) {
        if (sku != null) {
            viewModel.buySku(this, sku)
        } else {
            viewModel.sendMessage(R.string.err_product_id_empty)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_old -> {
                startActivity<OldMainActivity>(this)
                true
            }
            R.id.action_subscribe -> {
                startActivity<SubscribeActivity>(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
    }
}
