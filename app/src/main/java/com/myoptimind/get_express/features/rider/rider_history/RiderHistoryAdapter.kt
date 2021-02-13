package com.myoptimind.get_express.features.rider.rider_history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.rider.rider_history.data.RiderHistory
import com.myoptimind.get_express.features.shared.data.idToCartType
import com.myoptimind.get_express.features.shared.toMoneyFormat
import kotlinx.android.synthetic.main.item_customer_request.view.iv_icon
import kotlinx.android.synthetic.main.item_customer_request.view.tv_customer_name
import kotlinx.android.synthetic.main.item_history.view.*

class RiderHistoryAdapter constructor(
    var riderHistoryList: List<RiderHistory>,
    val listener: RiderHistoryListener? = null
): RecyclerView.Adapter<RiderHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_history,parent,false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(riderHistoryList[position],position)
    }

    override fun getItemCount() = riderHistoryList.size

    class ViewHolder constructor(
        private val itemView: View,
        val listener: RiderHistoryListener?
    ): RecyclerView.ViewHolder(itemView) {
        fun bind(riderHistory: RiderHistory, position: Int){


            itemView.setOnClickListener {
                listener?.onSelectItem(riderHistory)
            }
            itemView.box_reject.setOnClickListener {
                listener?.onSelectItem(riderHistory)
            }

            Glide.with(itemView.context)
                    .load(itemView.context.getDrawable(riderHistory.serviceId.idToCartType().drawableId))
                    .into(itemView.iv_icon)

            itemView.tv_customer_name.text = riderHistory.location.label
            itemView.tv_customer_details.text = "${riderHistory.createdAt} | ${riderHistory.totalItems} Items | ${riderHistory.totalPrice?.toMoneyFormat()}"

            Glide.with(itemView.context)
                    .load(riderHistory.icon)
                    .into(itemView.iv_customer_image)

        }
    }

    interface RiderHistoryListener {
        fun onSelectItem(riderHistory: RiderHistory)
    }
}