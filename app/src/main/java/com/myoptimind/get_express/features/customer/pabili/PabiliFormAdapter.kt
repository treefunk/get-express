package com.myoptimind.get_express.features.customer.pabili

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.data.ItemInPabili
import kotlinx.android.synthetic.main.item_pabili_form.view.*
import kotlinx.android.synthetic.main.item_pabili_form.view.btn_remove
import kotlinx.android.synthetic.main.item_pabili_form.view.et_quantity
import kotlinx.android.synthetic.main.item_pabili_post_form.view.*

class PabiliFormAdapter constructor(
        var pabiliItemList: ArrayList<ItemInPabili>,
        val listener: PabiliFormListener? = null,
        val isPostForm: Boolean = false
) : RecyclerView.Adapter<PabiliFormAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return ViewHolder(inflater.inflate(
                if(isPostForm) R.layout.item_pabili_post_form else R.layout.item_pabili_form,
                parent,
                false
        ), listener)
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

            if(isPostForm){
                itemView.et_pabili_name.text = pabiliItem.itemName
            }else{
                itemView.et_name.addTextChangedListener {
                    pabiliItemList[adapterPosition].itemName = it.toString().trim()
                }
                itemView.et_name.setText(pabiliItem.itemName)
            }

            itemView.et_quantity.addTextChangedListener {
                pabiliItemList[adapterPosition].quantity = it.toString().trim()
            }

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