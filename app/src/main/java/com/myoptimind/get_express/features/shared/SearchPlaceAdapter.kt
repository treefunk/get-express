package com.myoptimind.get_express.features.shared

import android.text.style.CharacterStyle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.myoptimind.get_express.R
import kotlinx.android.synthetic.main.item_place.view.*
import java.math.RoundingMode
import java.text.DecimalFormat


class SearchPlaceAdapter constructor(
    var predictions: List<AutocompletePrediction>,
    val listener: SearchPlaceListener? = null
): RecyclerView.Adapter<SearchPlaceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_place,parent,false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(predictions[position],position)
    }

    override fun getItemCount() = predictions.size

    class ViewHolder constructor(
        private val itemView: View,
        val listener: SearchPlaceListener?
    ): RecyclerView.ViewHolder(itemView) {
        fun bind(prediction: AutocompletePrediction, position: Int){
            val distanceInKm = if(prediction.distanceMeters != null){
                val km = (prediction.distanceMeters!! / 1000.0)
/*                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.CEILING
                df.format(km).toString()*/
                "%.2f".format(km)
            }else{
                null
            }
            val label = StringBuilder()
            label.append(prediction.getPrimaryText(null).toString())
            if(distanceInKm != null)
                label.append(" ($distanceInKm km)")

            itemView.tv_label.text = label
            itemView.tv_address.text = prediction.getSecondaryText(null).toString()
            itemView.setOnClickListener {
                listener?.onSelectItem(
                    prediction
                )
            }
        }
    }

    interface SearchPlaceListener {
        fun onSelectItem(prediction: AutocompletePrediction)
    }
}