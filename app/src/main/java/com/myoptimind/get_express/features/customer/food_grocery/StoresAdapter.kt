package com.myoptimind.get_express.features.customer.food_grocery

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
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
            private val view: View,
            private val listener: StoreListener?
    ) : RecyclerView.ViewHolder(view) {
        fun bind(store: Store, position: Int) {

            if(store.image.isBlank().not()){
                Glide.with(view.context)
                        .load(store.image)
                        .into(view.iv_store_image)
                if(store.isStoreAvailable.not()){
                    val colorMatrix = ColorMatrix()
                    colorMatrix.setSaturation(0F)
                    val filter = ColorMatrixColorFilter(colorMatrix)
                    view.iv_store_image.colorFilter = filter
                }else{
                    view.iv_store_image.clearColorFilter()
                }
            }
            view.tv_store_name.text = store.name
            view.setOnClickListener {
                listener?.onPressed(store,position)
            }
        }
    }

    interface StoreListener {
        fun onPressed(store: Store, index: Int)
    }
}