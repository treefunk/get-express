package com.myoptimind.getexpress.features.edit_profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.login.data.Address
import kotlinx.android.synthetic.main.item_address.view.*
import kotlinx.android.synthetic.main.item_my_vehicle.view.*

class CustomerAddressAdapter (
        var addresses: List<Address>,
        val listener: AddressListener? = null,
        var activeVehicle: Address? = null
): RecyclerView.Adapter<CustomerAddressAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_address,parent,false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(addresses[position],position)
    }

    override fun getItemCount() = addresses.size

    inner class ViewHolder constructor(
            private val itemView: View,
            private val listener: AddressListener?
    ): RecyclerView.ViewHolder(itemView) {
        fun bind(address: Address, position: Int){
            itemView.tv_address_title.text = address.label
            itemView.tv_address_subtitle.text = address.fullAddress
        }
    }

    interface AddressListener {
        fun onPressed(address: Address, index: Int)
    }
}