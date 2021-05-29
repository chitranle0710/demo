package vn.hiep.demobilling.ui

import android.app.Activity
import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vn.hiep.demobilling.base.BaseViewModel
import vn.hiep.demobilling.data.repository.BillingRepository

class MainActivityViewModel(private val repo: BillingRepository) : BaseViewModel() {

    val messages: LiveData<Int> get() = repo.messages.asLiveData()
    val consumedCount: LiveData<List<String>> get() = repo.consumedCount.asLiveData()
    val errorMessage: LiveData<String> get() = repo.errorFlow.asLiveData()

    fun debugConsumeS2() {
        repo.debugConsumeS2()
    }

    val billingLifecycleObserver: LifecycleObserver
        get() = repo.billingLifecycleObserver

    class SkuDetails internal constructor(val sku: String, repo: BillingRepository) {
        val productId = sku
        val title = repo.getSkuTitle(sku).asLiveData()
        val description = repo.getSkuDescription(sku).asLiveData()
        val price = repo.getSkuPrice(sku).asLiveData()
    }

    fun getSkuDetails(sku: String): SkuDetails {
        return SkuDetails(sku, repo)
    }

    fun canBuySku(sku: String): LiveData<Boolean> {
        return repo.canPurchase(sku).asLiveData()
    }

    fun isPurchased(sku: String): LiveData<Boolean> {
        return repo.isPurchased(sku).asLiveData()
    }

    /**
     * Starts a billing flow for purchasing gas.
     * @param activity
     * @return whether or not we were able to start the flow
     */
    fun buySku(activity: Activity, sku: String) {
        repo.buySku(activity, sku)
    }

    val billingFlowInProcess: LiveData<Boolean>
        get() = repo.billingFlowInProcess.asLiveData()

    fun sendMessage(message: Int) {
        viewModelScope.launch {
            repo.sendMessage(message)
        }
    }

    companion object {
        val TAG = MainActivityViewModel::class.simpleName
    }

    class MainActivityViewModelFactory(private val repo: BillingRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                return MainActivityViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
