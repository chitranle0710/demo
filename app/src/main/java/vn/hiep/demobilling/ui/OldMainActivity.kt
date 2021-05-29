package vn.hiep.demobilling.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import kotlinx.android.synthetic.main.activity_main_old.*
import vn.hiep.demobilling.R
import vn.hiep.demobilling.base.BaseActivity
import vn.hiep.demobilling.utils.Security
import vn.hiep.demobilling.utils.SharePreference


class OldMainActivity : BaseActivity(R.layout.activity_main_old), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient

    private val PRODUCT_ID = "producthex"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onClickView()
        buildBillingClient()
    }

    private fun onClickView() {
        btnPurchase.setOnClickListener {
            handlePurchaseWhenStarted()
        }
        btnSubscribe.setOnClickListener {
            val intent = Intent(this, SubscribeActivity::class.java)
            startActivity(intent)
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
                    val queryPurchaseInApp = billingClient.queryPurchases(INAPP)
                    val queryPurchases = queryPurchaseInApp.purchasesList
                    if (queryPurchases != null && queryPurchases.size > 0) {
                        handlePurchases(queryPurchases)
                    } else {
                        SharePreference.savePurchaseValueToPref(false, applicationContext)
                    }
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun handlePurchaseWhenStarted() {
        // happens when clicking on the button "Purchase"
        // check ready for status purchase here, if not we will build new connection
        if (billingClient.isReady) {
            initiatePurchase()
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
                        Log.d("MainActivity", "Can't initiate ${p0.debugMessage}")
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

    private fun initiatePurchase() {
        // we will add the id of products that are listed in GG Play Console
        val skuList = ArrayList<String>()
        skuList.add(PRODUCT_ID)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(INAPP)
        billingClient.querySkuDetailsAsync(
            params.build()
        ) { billingResult, listSkuDetails ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (listSkuDetails != null && listSkuDetails.size > 0) {
                    // create purchase Dialog here
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(listSkuDetails[0])
                        .build()
                    billingClient.launchBillingFlow(this, flowParams)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Purchase Item not Found",
                        Toast.LENGTH_SHORT
                    )
                        .show();
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT
                ).show()
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
            handlePurchases(listPurchased)
        }
        // check if purchase is already purchased
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            val queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP)
            val alreadyPurchases = queryAlreadyPurchasesResult.purchasesList
            if (alreadyPurchases != null) {
                try {
                    handlePurchases(listPurchased!!.toMutableList())
                } catch (ex: RuntimeException) {
                }
            }
        }
        // check if purchase is cancelled
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(applicationContext, "Purchase Canceled", Toast.LENGTH_SHORT).show();
        }
        // handle other errors
        else {
            Toast.makeText(
                applicationContext,
                "Error " + billingResult.debugMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // this method we will handle, verify and acknowledge purchase
    private fun handlePurchases(purchasesList: List<Purchase>) {
        for (purchase in purchasesList) {
            // check if item is purchase
            if (PRODUCT_ID.equals(purchase.skus) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!Security.verifyPurchase(purchase.originalJson, purchase.signature)) {
                    // Invalid purchase
                    // show error to user
                    Toast.makeText(
                        applicationContext,
                        "Error : Invalid Purchase",
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
                    if (!SharePreference.getPurchaseValueFromPref(applicationContext)) {
                        SharePreference.savePurchaseValueToPref(true, applicationContext)
                        Toast.makeText(applicationContext, "Item Purchased", Toast.LENGTH_SHORT)
                            .show()
                        recreate()
                    }
                }
            } else if (PRODUCT_ID.equals(purchase.skus) && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                Toast.makeText(
                    applicationContext,
                    "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT
                ).show()
            } else if (PRODUCT_ID.equals(purchase.skus) && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                SharePreference.savePurchaseValueToPref(false, applicationContext)
                Toast.makeText(applicationContext, "Purchase Status Unknown", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private val ackPurchase = AcknowledgePurchaseResponseListener { billingResult ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // if purchase is acknowledged
            // Grant entitlement to the user and restart activity
            SharePreference.savePurchaseValueToPref(true, applicationContext)
            Toast.makeText(applicationContext, "Item Purchased", Toast.LENGTH_SHORT).show()
            // reCreate activity
            recreate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }
}