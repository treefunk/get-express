package com.myoptimind.get_express.features.customer.whats_new

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.data.AddOn
import com.myoptimind.get_express.features.customer.whats_new.data.WhatsNew
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.item_tray_addon.view.*
import kotlinx.android.synthetic.main.item_whats_new.view.*

class WhatsNewAdapter constructor(
        var whatsNewsList: List<WhatsNew>,
        val listener: WhatsNewListener? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return WhatsNewViewHolder(inflater.inflate(R.layout.item_whats_new,parent,false),listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return (holder as WhatsNewViewHolder).bind(whatsNewsList[position],position)
    }

    override fun getItemCount() = whatsNewsList.size

    inner class WhatsNewViewHolder constructor(
            private val itemView: View,
            private val listener: WhatsNewListener?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(whatsNew: WhatsNew, position: Int){
            itemView.setOnClickListener {
                listener?.onSelectWhatsNew(whatsNew)
            }
            Glide.with(itemView.context)
                    .load(whatsNew.bannerImage)
                    .into(itemView.iv_main_image)

            itemView.tv_type.text = whatsNew.category
            itemView.tv_title.text = whatsNew.title
            itemView.tv_caption.text = if(whatsNew.body.length > 40) whatsNew.body.substring(0,40) + "..." else whatsNew.body

        }
    }

    interface WhatsNewListener {
        fun onSelectWhatsNew(whatsNew: WhatsNew)
    }
}