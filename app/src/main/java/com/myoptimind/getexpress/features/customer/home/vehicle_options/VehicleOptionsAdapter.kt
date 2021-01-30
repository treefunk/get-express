package com.myoptimind.getexpress.features.customer.home.vehicle_options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.home.vehicle_options.data.VehicleOption
import kotlinx.android.synthetic.main.item_vehicle_option.view.*


class VehicleOptionsAdapter constructor(
        var vehicleTypes: List<VehicleOption>,
        val listener: VehicleOptionListener? = null,
) : RecyclerView.Adapter<VehicleOptionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_vehicle_option, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(vehicleTypes[position], position)
    }

    override fun getItemCount() = vehicleTypes.size

    inner class ViewHolder constructor(
            private val itemView: View,
            private val listener: VehicleOptionListener?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(vehicleType: VehicleOption, position: Int) {
            Glide.with(itemView.context)
                    .load(vehicleType.image)
                    .into(itemView.iv_vehicle_image)
            itemView.tv_vehicle_name.text = vehicleType.name
            itemView.setOnClickListener {
                listener?.onPressed(vehicleType,position)
            }
        }
    }

    interface VehicleOptionListener {
        fun onPressed(vehicleType: VehicleOption, index: Int)
    }
}