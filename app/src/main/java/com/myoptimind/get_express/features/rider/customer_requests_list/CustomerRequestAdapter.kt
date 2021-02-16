package com.myoptimind.get_express.features.rider.customer_requests_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.rider.customer_requests_list.data.CustomerRequest
import com.myoptimind.get_express.features.shared.data.idToCartType
import com.myoptimind.get_express.features.shared.toMoneyFormat
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
            private val view: View,
            val listener: CustomerRequestListener?
    ): RecyclerView.ViewHolder(view) {
        fun bind(customerRequest: CustomerRequest, position: Int){

            view.cl_customer_request.setOnClickListener {
                listener?.onSelectItem(customerRequest)
            }

            view.box_accept.setOnClickListener {
                listener?.onPressButton(true,customerRequest,position)
            }

            view.box_reject.setOnClickListener {
                listener?.onPressButton(false,customerRequest,position)
            }

            view.tv_price.text = "${customerRequest.orderInfo.grandTotal.toMoneyFormat()}"

            view.tv_customer_name.text = customerRequest.customer.fullName
            view.tv_customer_details.text = customerRequest.orderInfo.pickupLocationLabel
            Glide.with(view.context)
                    .load(view.context.getDrawable(customerRequest.cartMeta.cartTypeId.idToCartType().drawableId))
                    .into(view.iv_icon)
            Glide.with(view.context)
                    .load(customerRequest.customer.profilePicture)
                    .into(view.iv_customer_image)
        }
    }

    interface CustomerRequestListener {
        fun onSelectItem(customerRequest: CustomerRequest)
        fun onPressButton(accepted: Boolean, customerRequest: CustomerRequest, index: Int)
    }
}