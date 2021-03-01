package com.myoptimind.get_express.features.customer.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.data.CartItem
import com.myoptimind.get_express.features.customer.cart.data.ItemInFoodGrocery
import com.myoptimind.get_express.features.shared.data.CartType
import com.myoptimind.get_express.features.shared.toMoneyFormat
import kotlinx.android.synthetic.main.item_edit_grocery.view.*

class CartItemsAdapter constructor(
        var items: List<CartItem>,
        var listType: CartType? = null,
        val listener: CartItemListener? = null,
        var izEnabled: Boolean = true
) : RecyclerView.Adapter<CartItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_edit_grocery, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder constructor(
            private val itemView: View,
            private val listener: CartItemListener?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: CartItem, position: Int) {
            when(listType){
                CartType.CAR -> { }
                CartType.GROCERY,CartType.FOOD -> {
                    val product = item as ItemInFoodGrocery
                    if(product.image.isBlank().not()){
                        Glide.with(itemView.context)
                                .load(product.image)
                                .into(itemView.iv_image)
                    }
                    itemView.tv_item_name.text = product.productName
                    var description = if(product.addons.size > 0) "${product.description}\n${product.addons.map { addOn -> addOn.productName }.joinToString(",")}"
                                      else product.description

                    if(product.notes.isNotBlank()){
                        description += "\nNotes: " + product.notes
                    }

                    itemView.tv_item_description.text = description
                    itemView.tv_item_price.text = product.basePrice.toMoneyFormat()
                    itemView.et_quantity.setText(product.quantity)

                    itemView.btn_plus.isEnabled = izEnabled
                    itemView.btn_minus.isEnabled = izEnabled

                    itemView.btn_plus.setOnClickListener { listener?.onPressed(product,true) }
                    itemView.btn_minus.setOnClickListener { listener?.onPressed(product,false) }

                }
                CartType.PABILI -> { }
                CartType.DELIVERY -> {

                }
                null -> {

                }
            }

        }
    }

    interface CartItemListener {
        fun onPressed(cartItem: CartItem, isIncrement: Boolean)
    }
}