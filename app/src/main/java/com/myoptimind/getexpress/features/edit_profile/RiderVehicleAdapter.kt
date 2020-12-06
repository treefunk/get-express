package com.myoptimind.getexpress.features.edit_profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.login.data.RiderVehicle
import kotlinx.android.synthetic.main.item_my_vehicle.view.*

class RiderVehicleAdapter(
        var myVehicles: List<RiderVehicle>,
        val listener: RiderVehicleListener? = null,
        var activeVehicle: RiderVehicle? = null
): RecyclerView.Adapter<RiderVehicleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_my_vehicle,parent,false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(myVehicles[position],position)
    }

    override fun getItemCount() = myVehicles.size

    inner class ViewHolder constructor(
            private val itemView: View,
            private val listener: RiderVehicleListener?
    ): RecyclerView.ViewHolder(itemView) {
        fun bind(myVehicle: RiderVehicle, position: Int){

            if(activeVehicle != null && activeVehicle == myVehicle){
                itemView.cb_is_active_vehicle.isChecked = true
                itemView.tv_default_label.visibility = View.VISIBLE
            }else{
/*                if(myVehicle.isActive == "0"){
                    itemView.iv_end_icon.visibility = View.GONE
                    itemView.label_pending_approval.visibility = View.VISIBLE
                }else{

                }*/
                    if(myVehicle.isActive == "0"){
                        itemView.setOnClickListener {
                            listener?.onPressed(myVehicle, position)
                        }
                    }
                itemView.cb_is_active_vehicle.isChecked = false
                itemView.tv_default_label.visibility = View.GONE
            }
            itemView.tv_vehicle_name.text = myVehicle.vehicleType
            itemView.tv_vehicle_description.text = "${myVehicle.vehicleModel} | ${myVehicle.plateNumber}"
        }
    }

    interface RiderVehicleListener {
        fun onPressed(riderVehicle: RiderVehicle, index: Int)
    }
}