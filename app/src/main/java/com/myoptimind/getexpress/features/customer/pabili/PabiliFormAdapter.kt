package com.myoptimind.getexpress.features.customer.pabili

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.cart.data.ItemInPabili
import com.myoptimind.getexpress.features.customer.food_grocery.StoresAdapter
import com.myoptimind.getexpress.features.customer.food_grocery.data.Store
import kotlinx.android.synthetic.main.item_pabili_form.view.*
import kotlinx.android.synthetic.main.item_store.view.*

class PabiliFormAdapter constructor(
        var pabiliItemList: ArrayList<ItemInPabili>,
        val listener: PabiliFormListener? = null,
) : RecyclerView.Adapter<PabiliFormAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_pabili_form, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pabiliItemList[position], position)
    }

    override fun getItemCount() = pabiliItemList.size

    inner class ViewHolder constructor(
            val itemView: View,
            private val listener: PabiliFormListener?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(pabiliItem: ItemInPabili, position: Int) {
            itemView.et_name.addTextChangedListener {
                pabiliItemList[adapterPosition].itemName = it.toString().trim()
            }

            itemView.et_quantity.addTextChangedListener {
                pabiliItemList[adapterPosition].quantity = it.toString().trim()
            }

            itemView.et_name.setText(pabiliItem.itemName)
            itemView.et_quantity.setText(pabiliItem.quantity)
            itemView.btn_remove.setOnClickListener {
                listener?.onRemove(pabiliItem,adapterPosition)
            }
        }
    }

    interface PabiliFormListener {
        fun onRemove(pabiliItem: ItemInPabili, index: Int)
    }
}