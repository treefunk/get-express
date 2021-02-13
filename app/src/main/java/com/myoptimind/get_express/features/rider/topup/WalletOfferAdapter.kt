package com.myoptimind.get_express.features.rider.topup

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.rider.topup.data.WalletOffer
import kotlinx.android.synthetic.main.item_top_up.view.*

class WalletOfferAdapter (
    var walletOffers: List<WalletOffer>,
    val listener: WalletOfferListener? = null,
    var selected: Int? = null
): RecyclerView.Adapter<WalletOfferAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_top_up,parent,false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(walletOffers[position],position)
    }

    override fun getItemCount() = walletOffers.size

    inner class ViewHolder constructor(
        private val itemView: View,
        private val listener: WalletOfferListener?
    ): RecyclerView.ViewHolder(itemView) {
        fun bind(walletOffer: WalletOffer, position: Int){
            itemView.tv_wallet_title.text = walletOffer.name
            itemView.tv_wallet_description.text = walletOffer.durationLabel
            itemView.tv_wallet_price.text = walletOffer.price
            itemView.tv_wallet_duration.text = "${walletOffer.durationInHours} hrs"

            if(selected != null && selected == adapterPosition){
                itemView.cb_selected.isChecked = true
                itemView.box_topup.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.yellow_200))
            }else{
                itemView.cb_selected.isChecked = false
                itemView.box_topup.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.background_color_darker))
            }

            itemView.setOnClickListener {
                listener?.onPressed(walletOffer,adapterPosition)
                selected = adapterPosition
                notifyDataSetChanged()
            }
        }
    }

    interface WalletOfferListener {
        fun onPressed(walletOffer: WalletOffer, index: Int)
    }
}