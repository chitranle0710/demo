package vn.hiep.demobilling.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import vn.hiep.demobilling.databinding.ItemBillingBinding
import vn.hiep.demobilling.databinding.ItemBillingHeaderBinding
import vn.hiep.demobilling.domain.model.Product

class BillingAdapter(
    private val listProduct: MutableList<Product>
) : RecyclerView.Adapter<BillingAdapter.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 1
        const val TYPE_ITEM = 2
    }

    var onClick: ((product: Product) -> Unit) = {}

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingAdapter.ViewHolder {
        if (viewType == TYPE_HEADER) {
            val binding =
                ItemBillingHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return HeaderViewHolder(binding)
        }
        val binding = ItemBillingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillingAdapter.ViewHolder, position: Int) {
        val product = listProduct[position]
        holder.bind(onClick, product)
    }

    override fun getItemCount(): Int = listProduct.size

    abstract inner class ViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(
            onClick: ((product: Product) -> Unit) = { },
            product: Product
        )
    }

    inner class HeaderViewHolder(private val binding: ItemBillingHeaderBinding) :
        ViewHolder(binding) {
        override fun bind(
            onClick: ((product: Product) -> Unit),
            product: Product
        ) {
            binding.txtIaProduct.text = product.name
        }
    }

    inner class ItemViewHolder(private val binding: ItemBillingBinding) : ViewHolder(binding) {
        override fun bind(
            onClick: ((product: Product) -> Unit),
            product: Product
        ) {
            var text =
                "${product.name}\n\n${product.description ?: "..."}\n${product.price ?: "..."}"
            if (product.quantity > 0) {
                text += "\nQuantity: ${product.quantity}"
            }
            binding.txtProduct.text = text

            binding.cardProduct.isChecked = product.isPurchased || product.quantity > 0

            binding.cardProduct.setOnClickListener {
                onClick.invoke(product)
            }
        }
    }
}
