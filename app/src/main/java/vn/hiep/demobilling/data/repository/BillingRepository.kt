package vn.hiep.demobilling.data.repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vn.hiep.demobilling.R
import vn.hiep.demobilling.data.datasource.BillingDataSource

class BillingRepository(
    private val billingDataSource: BillingDataSource,
    private val defaultScope: CoroutineScope
) {
    private val notifyMessages: MutableSharedFlow<Int> = MutableSharedFlow()
    private val consumedCountMessage: MutableSharedFlow<List<String>> = MutableSharedFlow()

    /**
     * Sets up the event that we can use to send messages up to the UI to be used in SnackBars.
     * This collects new purchase events from the BillingDataSource, transforming the known SKU
     * strings into useful String messages, and emitting the messages into the game messages flow.
     */
    private fun postMessagesFromBillingFlow() {
        defaultScope.launch {
            try {
                billingDataSource.getNewPurchases().collect { skuList ->
                    // TODO: Handle multi-line purchases better
                    for (sku in skuList) {
                        when (sku) {
                            SKU_SERVICE_1 -> notifyMessages.emit(R.string.sku_service_1)
                            SKU_SERVICE_2 -> notifyMessages.emit(R.string.sku_service_2)
                            SKU_ESSENTIAL_MONTHLY,
                            SKU_ESSENTIAL_YEARLY -> {
                                // this makes sure that upgrades/downgrades to subscriptions are
                                // reflected correctly in our user interface
                                billingDataSource.refreshPurchases()
                                notifyMessages.emit(R.string.sku_essential)
                            }
                            SKU_PREMIUM_MONTHLY,
                            SKU_PREMIUM_YEARLY -> {
                                // this makes sure that upgrades/downgrades to subscriptions are
                                // reflected correctly in our user interface
                                billingDataSource.refreshPurchases()
                                notifyMessages.emit(R.string.sku_premium)
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.d(TAG, "Collection complete")
            }
            Log.d(TAG, "Collection Coroutine Scope Exited")
        }
    }

    /**
     * Automatic support for upgrading/downgrading subscription.
     * @param activity
     * @param sku
     */
    fun buySku(activity: Activity, sku: String) {
        val upgradeFromSkus = ArrayList<String>()
        when (sku) {
            SKU_ESSENTIAL_MONTHLY -> upgradeFromSkus.addAll(
                listOf(SKU_ESSENTIAL_YEARLY, SKU_PREMIUM_MONTHLY, SKU_PREMIUM_YEARLY)
            )
            SKU_ESSENTIAL_YEARLY -> upgradeFromSkus.addAll(
                listOf(SKU_ESSENTIAL_MONTHLY, SKU_PREMIUM_MONTHLY, SKU_PREMIUM_YEARLY)
            )
            SKU_PREMIUM_MONTHLY -> upgradeFromSkus.addAll(
                listOf(SKU_ESSENTIAL_MONTHLY, SKU_ESSENTIAL_YEARLY, SKU_PREMIUM_YEARLY)
            )
            SKU_PREMIUM_YEARLY -> upgradeFromSkus.addAll(
                listOf(SKU_ESSENTIAL_MONTHLY, SKU_ESSENTIAL_YEARLY, SKU_PREMIUM_MONTHLY)
            )
        }
        billingDataSource.launchBillingFlow(activity, sku, upgradeFromSkus.toTypedArray())
    }

    /**
     * Return Flow that indicates whether the sku is currently purchased.
     *
     * @param sku the SKU to get and observe the value for
     * @return Flow that returns true if the sku is purchased.
     */
    fun isPurchased(sku: String): Flow<Boolean> {
        return billingDataSource.isPurchased(sku)
    }

    /**
     * We can buy gas if:
     * 1) We can add at least one unit of gas
     * 2) The billing data source allows us to purchase, which means that the item isn't already
     *    purchased.
     * For other skus, we rely on just the data from the billing data source. For subscriptions,
     * only one can be held at a time, and purchasing one subscription will use the billing feature
     * to upgrade or downgrade the user from the other.
     *
     * @param sku the SKU to get and observe the value for
     * @return Flow<Boolean> that returns true if the sku can be purchased
     */
    fun canPurchase(sku: String): Flow<Boolean> {
        return billingDataSource.canPurchase(sku)
    }

    suspend fun refreshPurchases() {
        billingDataSource.refreshPurchases()
    }

    val billingLifecycleObserver: LifecycleObserver get() = billingDataSource

    // There's lots of information in SkuDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc.
    fun getSkuTitle(sku: String): Flow<String> {
        return billingDataSource.getSkuTitle(sku)
    }

    fun getSkuPrice(sku: String): Flow<String> {
        return billingDataSource.getSkuPrice(sku)
    }

    fun getSkuDescription(sku: String): Flow<String> {
        return billingDataSource.getSkuDescription(sku)
    }

    val messages: Flow<Int> get() = notifyMessages
    val consumedCount: Flow<List<String>> get() = consumedCountMessage

    suspend fun sendMessage(stringId: Int) {
        notifyMessages.emit(stringId)
    }

    val billingFlowInProcess: Flow<Boolean>
        get() = billingDataSource.getBillingFlowInProcess()

    val errorFlow: Flow<String> get() = billingDataSource.getErrorFlow()

    fun debugConsumeS2() {
        CoroutineScope(Dispatchers.Main).launch {
            billingDataSource.consumeInappPurchase(SKU_SERVICE_2)
        }
    }

    companion object {
        // The following SKU strings must match the ones we have in the Google Play developer console.
        // SKUs for non-subscription purchases
        const val SKU_SERVICE_1 = "service_1"
        const val SKU_SERVICE_2 = "service_2"

        // SKU for subscription purchases (infinite gas)
        const val SKU_ESSENTIAL_MONTHLY = "essential_monthly"
        const val SKU_ESSENTIAL_YEARLY = "essential_yearly"
        const val SKU_PREMIUM_MONTHLY = "premium_monthly"
        const val SKU_PREMIUM_YEARLY = "premium_yearly"

        val TAG = BillingRepository::class.simpleName
        val INAPP_SKUS = arrayOf(
            SKU_SERVICE_1, SKU_SERVICE_2
        )
        val SUBSCRIPTION_SKUS = arrayOf(
            SKU_ESSENTIAL_MONTHLY,
            SKU_ESSENTIAL_YEARLY,
            SKU_PREMIUM_MONTHLY,
            SKU_PREMIUM_YEARLY
        )
        val AUTO_CONSUME_SKUS = arrayOf(SKU_SERVICE_2)
    }

    init {
        postMessagesFromBillingFlow()

        // Since both are tied to application lifecycle, we can launch this scope to collect
        // consumed purchases from the billing data source while the app process is alive.
        defaultScope.launch {
            billingDataSource.getConsumedPurchases().collect { listConsumed ->
                Log.d(TAG, "getConsumedPurchases: $listConsumed")
                consumedCountMessage.emit(listConsumed)
            }
        }
    }
}