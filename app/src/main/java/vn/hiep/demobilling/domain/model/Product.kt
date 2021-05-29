package vn.hiep.demobilling.domain.model

data class Product(
    val productId: String? = null,
    val orderId: String? = null,
    val packageName: String? = null,
    val purchaseTime: Long? = null,
    val purchaseState: Int? = null,
    val purchaseToken: String? = null,
    var quantity: Int = 0,
    val autoRenewing: Boolean? = null,
    val acknowledged: Boolean? = null,
    var name: String? = null,
    val benefit: String? = null,
    var description: String? = null,
    val status: Int? = null,
    var price: String? = null,
    var isPurchased: Boolean = false
) {
}