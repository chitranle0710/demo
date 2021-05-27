package vn.hiep.demobilling.model

data class Product(
    val orderId: String? = null,
    val productId: String? = null,
    val packageName: String? = null,
    val purchaseTime: Long? = null,
    val purchaseState: Int? = null,
    val purchaseToken: String? = null,
    val quantity: Int? = null,
    val autoRenewing: Boolean? = null,
    val acknowledged: Boolean? = null,
    val name: String? = null,
    val benefit: String? = null,
    val description: String? = null,
    val status: Int? = null,
    val price: String? = null
) {
}