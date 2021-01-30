package com.myoptimind.getexpress.features.rider.customer_requests_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.rider.customer_requests_list.data.CustomerRequest
import com.myoptimind.getexpress.features.shared.data.idToCartType
import kotlinx.android.synthetic.main.item_customer_request.view.*

class CustomerRequestAdapter constructor(
        var customerRequests: List<CustomerRequest>,
        val listener: CustomerRequestListener? = null
): RecyclerView.Adapter<CustomerRequestAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_customer_request,parent,false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(customerRequests[position],position)
    }

    override fun getItemCount() = customerRequests.size

    class ViewHolder constructor(
            private val itemView: View,
            val listener: CustomerRequestListener?
    ): RecyclerView.ViewHolder(itemView) {
        fun bind(customerRequest: CustomerRequest, position: Int){

            itemView.cl_customer_request.setOnClickListener {
                listener?.onSelectItem(customerRequest)
            }

            itemView.box_accept.setOnClickListener {
                listener?.onPressButton(true,customerRequest)
            }

            itemView.box_reject.setOnClickListener {
                listener?.onPressButton(false,customerRequest)
            }

            itemView.tv_price.text = "| ${customerRequest.orderInfo.grandTotal}"

            itemView.tv_customer_name.text = customerRequest.customer.fullName
            itemView.tv_customer_details.text = customerRequest.orderInfo.pickupLocationLabel
            Glide.with(itemView.context)
                    .load(itemView.context.getDrawable(customerRequest.cartMeta.cartTypeId.idToCartType().drawableId))
                    .into(itemView.iv_icon)
            Glide.with(itemView.context)
                    .load(customerRequest.customer.profilePicture)
                    .into(itemView.iv_customer_image)
        }
    }

    interface CustomerRequestListener {
        fun onSelectItem(customerRequest: CustomerRequest)
        fun onPressButton(accepted: Boolean, customerRequest: CustomerRequest)
    }
}