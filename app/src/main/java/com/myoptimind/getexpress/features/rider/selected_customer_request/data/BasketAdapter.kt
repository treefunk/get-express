package com.myoptimind.getexpress.features.rider.selected_customer_request.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.data.CartType
import kotlinx.android.synthetic.main.item_order.view.*
import kotlinx.android.synthetic.main.item_order.view.tv_item_description
import kotlinx.android.synthetic.main.item_order.view.tv_item_name
import kotlinx.android.synthetic.main.item_order.view.tv_item_price
import kotlinx.android.synthetic.main.item_order.view.tv_quantity
import kotlinx.android.synthetic.main.item_pabili.view.*

private const val VIEW_TYPE_ITEM = 100
private const val VIEW_TYPE_PABILI = 200
private const val VIEW_TYPE_DELIVERY = 300

class BasketAdapter constructor(
    var itemList: List<CartItem>,
    var listType: CartType? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ItemViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun bind(item: ItemInFoodGrocery){
            view.tv_item_name.text = item.productName
            view.tv_item_description.text = item.description
            view.tv_item_price.text = item.basePrice.toDouble().toString()
            view.tv_quantity.text = "Qty: ${item.quantity}"
            Glide.with(view.context)
                    .load(item.image)
                    .transform(RoundedCorners(15))
                    .into(view.iv_image)
        }
    }

    inner class PabiliViewHolder(private val view: View): RecyclerView.ViewHolder(view){
        fun bind(item: ItemInPabili){
            view.tv_item_name.text = item.itemName
            view.tv_quantity.text = "x ${item.quantity}"
        }
    }

    inner class DeliveryViewHolder(private val view: View): RecyclerView.ViewHolder(view){
        fun bind(item: BasketForDelivery){
            view.tv_item_name.text = item.category
            view.tv_item_description.text = item.notes
            view.tv_item_price.text = item.price
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            VIEW_TYPE_ITEM -> ItemViewHolder(inflater.inflate(R.layout.item_order,parent,false))
            VIEW_TYPE_PABILI -> PabiliViewHolder(inflater.inflate(R.layout.item_pabili,parent, false))
            VIEW_TYPE_DELIVERY -> DeliveryViewHolder(inflater.inflate(R.layout.item_delivery,parent,false))
            else -> throw Exception("Invalid View Type!")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        when(holder){
            is ItemViewHolder -> holder.bind(item as ItemInFoodGrocery)
            is PabiliViewHolder -> holder.bind(item as ItemInPabili)
            is DeliveryViewHolder -> holder.bind(item as BasketForDelivery)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(listType){
            CartType.GROCERY,CartType.FOOD -> VIEW_TYPE_ITEM
            CartType.PABILI -> VIEW_TYPE_PABILI
            CartType.DELIVERY -> VIEW_TYPE_DELIVERY
            else -> throw Exception("this cart type is not supported!")
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}