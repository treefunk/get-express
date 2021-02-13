package com.myoptimind.get_express.features.customer.food_grocery.selected_store

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.data.AddOn
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.item_tray_addon.view.*

class TrayAddOnAdapter constructor(
        var trayAddOns: List<TrayAddOn>,
        val listener: AddOnTrayListener? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AddOnTrayViewHolder(inflater.inflate(R.layout.item_tray_addon,parent,false),listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return (holder as AddOnTrayViewHolder).bind(trayAddOns[position],position)
    }

    override fun getItemCount() = trayAddOns.size

    inner class AddOnTrayViewHolder constructor(
            private val itemView: View,
            private val listener: AddOnTrayListener?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(trayAddOn: TrayAddOn, position: Int){
            itemView.setOnClickListener {
                listener?.onSelectAddon(trayAddOn)
            }
            itemView.tv_addon_name.text = trayAddOn.addOnDescription
            itemView.tv_addon_quantity.text = " x${trayAddOn.quantity}"
            val price = if(trayAddOn.addons.isNullOrEmpty()) trayAddOn.price.toDouble().toString() else trayAddOn.addons!!.map { it.basePrice.toDouble() }.reduce { acc, d -> acc + d } + trayAddOn.price.toDouble()
            itemView.tv_addon_price.text = price.toString()
        }
    }

    interface AddOnTrayListener {
        fun onSelectAddon(trayAddOn: TrayAddOn)
    }

    @Parcelize
    data class TrayAddOn(
            val cartItemId: String,
            val quantity: String,
            val addOnDescription: String,
            val notes: String,
            val price: String,
            var addons: List<AddOn>? = null
    ): Parcelable
}