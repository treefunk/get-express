package com.myoptimind.get_express.features.customer.food_grocery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.food_grocery.data.Store
import kotlinx.android.synthetic.main.item_store.view.*

class StoresAdapter constructor(
        var stores: List<Store>,
        val listener: StoreListener? = null,
) : RecyclerView.Adapter<StoresAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_store, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stores[position], position)
    }

    override fun getItemCount() = stores.size

    inner class ViewHolder constructor(
            private val itemView: View,
            private val listener: StoreListener?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(store: Store, position: Int) {
            if(store.image.isBlank().not()){
                Glide.with(itemView.context)
                        .load(store.image)
                        .into(itemView.iv_store_image)

            }
            itemView.tv_store_name.text = store.name
            itemView.setOnClickListener {
                listener?.onPressed(store,position)
            }
        }
    }

    interface StoreListener {
        fun onPressed(store: Store, index: Int)
    }
}