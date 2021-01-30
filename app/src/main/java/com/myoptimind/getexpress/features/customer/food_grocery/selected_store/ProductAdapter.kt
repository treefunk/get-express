package com.myoptimind.getexpress.features.customer.food_grocery.selected_store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.food_grocery.data.Product
import com.myoptimind.getexpress.features.shared.data.CartType
import kotlinx.android.synthetic.main.item_store_grocery.view.*


private const val VIEW_TYPE_GROCERY = 767


class ProductAdapter constructor(
        var products: List<Product>,
        var cartType: CartType,
        val listener: ProductListener? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(cartType){
            CartType.GROCERY -> GroceryViewHolder(inflater.inflate(R.layout.item_store_grocery, parent, false), listener)
            CartType.FOOD -> GroceryViewHolder(inflater.inflate(R.layout.item_store_food,parent,false),listener)
            else -> throw Exception("Cart type not supported!")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when(cartType){
            CartType.GROCERY,CartType.FOOD -> (holder as GroceryViewHolder).bind(products[position],position)
            else -> throw Exception("cart type not supported.")
        }
    }

    override fun getItemCount() = products.size

    inner class GroceryViewHolder constructor(
            private val itemView: View,
            private val listener: ProductListener?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind (product: Product, position: Int) {
            Glide.with(itemView.context)
                    .load(product.image)
                    .into(itemView.iv_product_image)

            itemView.tv_product_name.text = " ${product.productName} id: ${product.id}"
            itemView.tv_product_price.text = product.basePrice
            itemView.setOnClickListener {
                listener?.onPressProduct(product)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(cartType){
            CartType.GROCERY,CartType.FOOD -> VIEW_TYPE_GROCERY
            else -> throw Exception("Cart type not supported")
        }
    }

    interface ProductListener {
        fun onPressProduct(product: Product)
    }
}