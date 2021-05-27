package vn.hiep.demobilling

import android.content.Intent
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_subscribe.*
import vn.hiep.demobilling.adapter.SubscribeAdapter
import vn.hiep.demobilling.base.BaseActivity
import vn.hiep.demobilling.model.Product
import vn.hiep.demobilling.utils.JSON
import vn.hiep.demobilling.utils.Security
import vn.hiep.demobilling.utils.SharePreference
import java.lang.RuntimeException
import kotlin.collections.ArrayList

class SubscribeActivity : BaseActivity(), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    private val PRODUCT_ID_SUBSRIBE = "vip21"
    private var listProduct: MutableList<Product> = ArrayList()
    private lateinit var purchaseToken: String
    private lateinit var oldProductID: String
    private var purchaseState: Int? = null

    override fun getLayout(): Int = R.layout.activity_subscribe

    override fun onInitViewModel() {
    }

    override fun onClickView() {
        btnSubscribe.setOnClickListener {
//            handlePurchaseWhenStarted()
        }

        btnSubscribeList.setOnClickListener {
            val intent = Intent(this, ListSubscribeActivity::class.java)
            startActivity(intent)
        }
        createList()

    }


    private fun createList() {
        listProduct.add(
            Product(
                price = "5000VND",
                benefit = "AAAAAA",
                productId = "vip21",
                name = "VIP 2021"
            )
        )
        listProduct.add(
            Product(
                price = "5000VND",
                benefit = "AAAAAA",
                productId = "hex_premium",
                name = "Hex Premium"
            )
        )
        val adapter = SubscribeAdapter(listProduct)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter.apply {
            onClick = {
                handlePurchaseWhenStarted(it.productId)
            }
        }
    }

    override fun onInitView() {
        buildBillingClient()
        if (SharePreference.getSubscribeValueFromPref(applicationContext)) {
            btnSubscribe.visibility = View.GONE
        }
        //item not Purchased
        else {
            btnSubscribe.visibility = View.VISIBLE;
        }
    }

    private fun buildBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases().setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(
                        BillingClient.SkuType.SUBS
                    ) { _, p1 ->
                        if (p1.size > 0) {
                            purchaseToken = p1[0].purchaseToken
                            oldProductID = p1[0].skus[0]
                            purchaseState = p1[0].purchaseState
                            Log.d("SubscribeActitivy", "$purchaseToken")
                            handlePurchases(p1)
                        } else {
                            SharePreference.saveSubscribeValueToPref(false, applicationContext)
                        }
                    }
                } else {
                    SharePreference.saveSubscribeValueToPref(false, applicationContext)
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun handlePurchaseWhenStarted(productID: String?) {
        // happens when clicking on the button "Purchase"
        // check ready for status purchase here, if not we will build new connection
        if (billingClient.isReady) {
            initiatePurchase(productID)
        } else {
            billingClient =
                BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                }

                override fun onBillingSetupFinished(p0: BillingResult) {
                    if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                        // start to purchase and query from GG play console
                        initiatePurchase()
                    } else {
                        Log.d("Subscribe", "Can't initiate ${p0.debugMessage}")
                        Toast.makeText(
                            applicationContext,
                            "Error " + p0.debugMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        }
    }

    private fun initiatePurchase(productID: String? = null) {
        if (productID == null) PRODUCT_ID_SUBSRIBE
        // we will add the id of products that are listed in GG Play Console
        val skuList = ArrayList<String>()
        if (productID != null) {
            skuList.add(productID)
        }
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingClient.querySkuDetailsAsync(
            params.build()
        ) { billingResult, listSkuDetails ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (listSkuDetails != null && listSkuDetails.size > 0) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(listSkuDetails[0])
                        .build()
                    billingClient.launchBillingFlow(this, flowParams)
                    // create purchase Dialog here
                    Log.d("SubscribeActitivyToken", "$purchaseToken")

                } else {
                    Toast.makeText(
                        applicationContext,
                        "Subscribe Item not Found",
                        Toast.LENGTH_SHORT
                    )
                        .show();
                    Log.d("SubscribeActivity", "Subscribe Item not Found")

                }
            } else {
                Toast.makeText(
                    applicationContext,
                    " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT
                ).show()
                Log.d("SubscribeActivity", "Error ${billingResult.debugMessage}")
            }
        }
    }

    // this method will use to check purchase result and handle it accordingly.
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        listPurchased: MutableList<Purchase>?
    ) {
        // check if purchase is new
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && listPurchased != null) {
            try {
                handlePurchases(listPurchased)
            } catch (ex: RuntimeException) {
            }
        }
        // check if purchase is already purchased
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            billingClient.queryPurchasesAsync(
                BillingClient.SkuType.SUBS
            ) { _, purchasesList -> handlePurchases(purchasesList.toMutableList()) }
        }
        // check if purchase is cancelled
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("SubscribeActivity", "Subscribe Canceled")
            Toast.makeText(applicationContext, "Subscribe Canceled", Toast.LENGTH_SHORT).show();
        }
        // handle other errors
        else {
            Toast.makeText(
                applicationContext,
                "Error " + billingResult.debugMessage,
                Toast.LENGTH_SHORT
            ).show();
            Log.d("SubscribeActivity", "Error ${billingResult.debugMessage}")
        }
    }

    // this method we will handle, verify and acknowledge purchase
    private fun handlePurchases(purchasesList: List<Purchase>) {
        for (purchase in purchasesList) {
            // check if item is purchase
            if (PRODUCT_ID_SUBSRIBE.equals(purchase.skus) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!Security.verifyValidSignature(purchase.originalJson, purchase.signature)) {
                    // Invalid purchase
                    // show error to user
                    Log.d("SubscribeActivity", "Error : Invalid Subscribe")
                    Toast.makeText(
                        applicationContext,
                        "Error : Invalid Subscribe",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                // else purchase is valid
                // if item is purchased and not acknowledged
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase)
                } else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    if (!SharePreference.getSubscribeValueFromPref(applicationContext)) {
                        SharePreference.saveSubscribeValueToPref(true, applicationContext)
                        Toast.makeText(applicationContext, "Item Subscribed", Toast.LENGTH_SHORT)
                            .show()
                        recreate()
                        Log.d("SubscribeActivity", "Item Subscribed")
                    }
                }
            } else if (PRODUCT_ID_SUBSRIBE.equals(purchase.skus) && purchase.purchaseState == Purchase.PurchaseState.PENDING
            ) {
                Toast.makeText(
                    applicationContext,
                    "Subscribe is Pending. Please complete Transaction", Toast.LENGTH_SHORT
                ).show()
                Log.d("SubscribeActivity", "Subscribe is Pending. Please complete Transaction")

            } else if (PRODUCT_ID_SUBSRIBE.equals(purchase.skus) && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE
            ) {
                SharePreference.saveSubscribeValueToPref(false, applicationContext)
                Toast.makeText(applicationContext, "Subscribe Status Unknown", Toast.LENGTH_SHORT)
                    .show()
                Log.d("SubscribeActivity", "Subscribe Status Unknown")
            }
        }
    }

    private val ackPurchase = AcknowledgePurchaseResponseListener { billingResult ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // if purchase is acknowledged
            // Grant entitlement to the user and restart activity
            SharePreference.saveSubscribeValueToPref(true, applicationContext)
            Log.e("SubscribeActivity", "Item Subscribed")
            Toast.makeText(applicationContext, "Item Subscribed", Toast.LENGTH_SHORT).show()
            // reCreate activity
            recreate()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }
}