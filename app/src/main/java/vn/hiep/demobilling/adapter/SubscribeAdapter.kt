package vn.hiep.demobilling.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_subscribe.view.*
import vn.hiep.demobilling.R
import vn.hiep.demobilling.domain.model.Product

class SubscribeAdapter(
    private val listProduct: MutableList<Product>
) : RecyclerView.Adapter<SubscribeAdapter.ViewHolder>() {

    var onClick: ((product: Product) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscribeAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_subscribe, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SubscribeAdapter.ViewHolder, position: Int) {
        holder.bind(onClick, listProduct, position)
    }

    override fun getItemCount(): Int = listProduct.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            onClick: ((product: Product) -> Unit) = { },
            listSku: MutableList<Product>,
            position: Int
        ) {
            itemView.btnBuy.setOnClickListener { onClick.invoke(listSku[position]) }
            itemView.tvNameProduct.text = listSku[position].name
            itemView.tvPrice.text = listSku[position].price
        }
    }
}