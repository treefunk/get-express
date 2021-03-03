package com.myoptimind.get_express.features.shared

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.*
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import kotlinx.android.synthetic.main.dialog_search_place.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class SearchPlaceDialog: BaseDialogFragment() {

    private lateinit var adapter: SearchPlaceAdapter
    private lateinit var placesClient: PlacesClient
    
    companion object {

        const val EXTRA_PLACE = "EXTRA_PLACE"
        private const val SECONDS_DELAY_BEFORE_QUERY = 0.75
        fun newInstance(): SearchPlaceDialog{
            val args = Bundle()
            val fragment = SearchPlaceDialog()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        placesClient = Places.createClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_search_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
     // Specify the fields to return.
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)

        // Construct a request object, passing the place ID and fields array.

        adapter = SearchPlaceAdapter(ArrayList(), object : SearchPlaceAdapter.SearchPlaceListener {
            override fun onSelectItem(prediction: AutocompletePrediction) {
                val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                placesClient.fetchPlace(request).addOnSuccessListener { response: FetchPlaceResponse ->
                    val place = response.place
                    Timber.v("selected place -> $place")
                    targetFragment?.onActivityResult(
                        targetRequestCode,
                        Activity.RESULT_OK,
                        Intent().putExtra(EXTRA_PLACE, place) // PARSE THIS ("yyyy M d")
                    )
                    hideKeyboard(requireActivity())
                    this@SearchPlaceDialog.dismiss()
                }.addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        Toast.makeText(requireContext(), "Place not found: ${exception.message}",Toast.LENGTH_SHORT).show()
                        val statusCode = exception.statusCode
                    }
                }



            }
        })

        rv_search_place.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )
        rv_search_place.adapter = adapter

        et_search_place.addTextChangedListener(object : TextWatcher {
            private var timer: Timer = Timer()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            queryPredictions(et_search_place.text.toString())
                        }
                    },
                    TimeUnit.SECONDS.toMillis(SECONDS_DELAY_BEFORE_QUERY.toLong())
                )
            }
        })

//        et_search_place.requestFocusFromTouch()
        showSoftKeyboard(et_search_place)
    }

    fun queryPredictions(query: String){


        val token = AutocompleteSessionToken.newInstance()

        // Create a RectangularBounds object.
        val latLng = RiderTrackingService.latLong.value

        val meters = 500.0

        val coef = meters * 0.0000089

        val locationBias = if(latLng != null){
            val latMax = latLng.latitude + coef
            val lngMax = latLng.longitude + coef / Math.cos(latLng.latitude * 0.18)

            val latMin = latLng.latitude - coef
            val lngMin = latLng.longitude - coef / Math.cos(latLng.latitude * 0.18)


            /*  val northSide = SphericalUtil.computeOffset(latLng,5000.0,0.0)
              val southSide = SphericalUtil.computeOffset(latLng,5000.0,180.0)
    */

            val northSide = LatLng(latMax, lngMax)
            val southSide = LatLng(latMin, lngMin)
            val bounds = LatLngBounds.builder()
                .include(northSide)
                .include(southSide)
                .build()
            RectangularBounds.newInstance(bounds)
        }else{
            null
        }



        val request =
            FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(locationBias)
                //.setLocationRestriction(bounds)
                .setOrigin(latLng)
                .setCountries("PH")
/*                .setTypeFilter(TypeFilter.ADDRESS)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setTypeFilter(TypeFilter.REGIONS)*/
                .setSessionToken(token)
                .setQuery(query)
                .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val predictions = ArrayList(response.autocompletePredictions)
                predictions.sortBy { it.distanceMeters }
                adapter.predictions = predictions
                adapter.notifyDataSetChanged()
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Timber.e("Place not found: " + exception.statusCode)
                }
            }

    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

            // here is one more tricky issue
            // imm.showSoftInputMethod doesn't work well
            // and imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0) doesn't work well for all cases too
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }
}