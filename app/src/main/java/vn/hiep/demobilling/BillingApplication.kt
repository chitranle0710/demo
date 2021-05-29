package vn.hiep.demobilling

import android.app.Application
import kotlinx.coroutines.GlobalScope
import vn.hiep.demobilling.data.datasource.BillingDataSource
import vn.hiep.demobilling.data.repository.BillingRepository

class BillingApplication: Application() {
    lateinit var appContainer: AppContainer
    // Container of objects shared across the whole app
    inner class AppContainer {
        private val applicationScope = GlobalScope
        private val billingDataSource = BillingDataSource.getInstance(
            this@BillingApplication,
            applicationScope,
            BillingRepository.INAPP_SKUS,
            BillingRepository.SUBSCRIPTION_SKUS,
            BillingRepository.AUTO_CONSUME_SKUS
        )
        val billingRepository = BillingRepository(
            billingDataSource,
            applicationScope
        )
    }

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer()
    }
}
