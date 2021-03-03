package com.myoptimind.get_express.features.shared

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import timber.log.Timber

open class BaseDialogFragment: DialogFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.grey_200_alpha20)))
        dialog?.findViewById<ImageButton>(R.id.ib_close)?.setOnClickListener {
            this@BaseDialogFragment.dismiss()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
        )
    }



    internal fun showSearchFragment(requestCode: Int){
        val searchFragment = SearchPlaceDialog.newInstance()
        searchFragment.setTargetFragment(this,requestCode)
        searchFragment.show(parentFragmentManager,"SEARCH_PLACE")
    }

}

class AutoCompletePlacesAdapter(
    context: Context,
    predictions: List<AutocompletePrediction>
): ArrayAdapter<AutocompletePrediction> (context,0,predictions){
    val predictionsFilter = object: Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            TODO("Not yet implemented")
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            TODO("Not yet implemented")
        }
    }
}
