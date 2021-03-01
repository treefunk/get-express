package com.myoptimind.get_express.features.customer.food_grocery.selected_store

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.food_grocery.data.Product
import com.myoptimind.get_express.features.shared.data.CartType
import com.myoptimind.get_express.features.shared.toMoneyFormat
import kotlinx.android.synthetic.main.item_store.view.*
import kotlinx.android.synthetic.main.item_store_food.view.*
import kotlinx.android.synthetic.main.item_store_grocery.view.*
import kotlinx.android.synthetic.main.item_store_grocery.view.ib_add_to_cart
import kotlinx.android.synthetic.main.item_store_grocery.view.iv_product_image
import kotlinx.android.synthetic.main.item_store_grocery.view.tv_product_name
import kotlinx.android.synthetic.main.item_store_grocery.view.tv_product_price


private const val VIEW_TYPE_GROCERY = 767


class ProductAdapter constructor(
        var products: List<Product>,
        var cartType: CartType,
        val forViewingOnly: Boolean,
        var izEnabled: Boolean = true,
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

                if(product.isOutStock){
                    val colorMatrix = ColorMatrix()
                    colorMatrix.setSaturation(0F)
                    val filter = ColorMatrixColorFilter(colorMatrix)
                    itemView.ib_add_to_cart.colorFilter = filter
                }else{
                    itemView.ib_add_to_cart.clearColorFilter()
                }

            itemView.tv_product_name.text = product.productName
            itemView.tv_product_price.text = product.basePrice.toMoneyFormat()

            if(forViewingOnly){
                itemView.ib_add_to_cart.visibility = View.INVISIBLE
            }

            itemView.isEnabled = izEnabled
            itemView.ib_add_to_cart.isEnabled = izEnabled

            itemView.setOnClickListener {
                listener?.onPressProduct(product,false)
            }

            itemView.ib_add_to_cart.setOnClickListener {
                listener?.onPressProduct(product,true)
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
        fun onPressProduct(product: Product, isInstant: Boolean)
    }
}