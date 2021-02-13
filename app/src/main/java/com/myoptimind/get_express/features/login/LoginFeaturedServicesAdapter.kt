package com.myoptimind.get_express.features.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.home.data.Service

class LoginFeaturedServicesAdapter(
    var loginFeaturedServices: List<Service>
) : RecyclerView.Adapter<LoginFeaturedServicesAdapter.ViewHolder>() {


    class ViewHolder(private val v: View): RecyclerView.ViewHolder(v) {
        fun bind(loginFeaturedService: Service){
            Glide.with(itemView.context)
                .load(loginFeaturedService.icon)
                .into(itemView.findViewById(R.id.iv_main_image))
            itemView.findViewById<TextView>(R.id.tv_title).text = loginFeaturedService.label.toUpperCase()
            itemView.findViewById<TextView>(R.id.tv_description).text = loginFeaturedService.description

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_login_featured,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(loginFeaturedServices[position])
    }

    override fun getItemCount() = loginFeaturedServices.size
}