package com.myoptimind.get_express.features.customer.delivery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.data.CartLocation
import com.myoptimind.get_express.features.customer.home.EnterAddressDialog
import com.myoptimind.get_express.features.customer.home.EnterAddressViewModel
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog
import com.myoptimind.get_express.features.edit_profile.ProfileRepository
import com.myoptimind.get_express.features.edit_profile.ProfileViewModel
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import com.myoptimind.get_express.features.shared.SearchPlaceDialog
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.initMultilineEditText
import com.myoptimind.get_express.features.shared.izNotBlank
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.dialog_recipient_delivery.*
import kotlinx.android.synthetic.main.dialog_recipient_delivery.ib_close
import timber.log.Timber

@AndroidEntryPoint
class RecipientDeliveryDialog : BaseDialogFragment() {

    private val enterAddressViewModel by activityViewModels<EnterAddressViewModel>()
    private val viewModel by activityViewModels<ProfileViewModel>()
    private var mapFragment: SupportMapFragment? = null

    private var locationMarker: Marker? = null


    companion object {
        private const val ADDRESS_REQUEST = 300
        private const val ARGS_PERSON_TYPE = "person_type"
        const val DATA_LANDMARK = "data_landmark"


        fun newInstance(personType: PERSON_TYPE): RecipientDeliveryDialog {
            val args = Bundle()
            args.putParcelable(ARGS_PERSON_TYPE, personType as Parcelable)
            val fragment = RecipientDeliveryDialog ()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_recipient_delivery,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()

        ib_close.setOnClickListener { dismiss() }
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        val personType = requireArguments().getParcelable<PERSON_TYPE>(ARGS_PERSON_TYPE)!!

        label_address.text = "${personType.label} Address *"
        label_name.text = "${personType.label} Name *"
        label_contact_number.text = "${personType.label} Contact Number *"

        et_additional_notes.initMultilineEditText()


        et_address.setOnClickListener {
//            showPlacesAutocomplete(ADDRESS_REQUEST)
            showSearchFragment(ADDRESS_REQUEST)
        }

        btn_submit_for_approval.setOnClickListener {

            if(et_address.izNotBlank() && et_name.izNotBlank() && et_contact_number.izNotBlank() && viewModel.selectedPlace.value != null){
                val oldPlace = viewModel.selectedPlace.value!!

                val modifiedPlace = Place.builder()
                    .setName(et_name.text.toString())
                    .setAddress(oldPlace.address)
                    .setPhoneNumber(et_contact_number.text.toString())
                    .setLatLng(oldPlace.latLng)
                    .build()


                viewModel.updateSelectedPlace(null)
                targetFragment?.onActivityResult(
                    targetRequestCode,
                    Activity.RESULT_OK,
                    Intent().apply {
                        putExtra(SelectAddressBottomDialog.EXTRA_ENTER_ADDRESS,modifiedPlace) // PARSE THIS ("yyyy M d")
                        putExtra(DATA_LANDMARK,et_additional_notes.text.toString())
                    }
                )
                this.dismiss()
            }else{
                Toast.makeText(requireContext(),"Please fill in required fields.", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun initObservers() {


        viewModel.selectedPlace.observe(viewLifecycleOwner){ selectedPlace ->
            if(selectedPlace != null){
                et_address.setText(selectedPlace.address)
                initMap(selectedPlace)
            }
        }

        enterAddressViewModel.checkLocationResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    if(result.isLoading){
                        view_loading_recipient_delivery.visibility = View.VISIBLE
                    }else{
                        view_loading_recipient_delivery.visibility = View.GONE
                    }
                    et_address.isEnabled = result.isLoading.not()
                }
                is Result.Success -> {
                    if(result.data != null){
                        et_address.setText(result.data.address)
                        val place = result.data
                        viewModel.updateSelectedPlace(result.data)
                    }
                }
                is Result.Error -> {

                }
                is Result.HttpError -> {
                    if(result.error is ProfileRepository.AreaLaunchingSoonException){
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(result.error.title)
                            .setMessage(result.error.message)
                            .setPositiveButton("OK") { dialog, which ->
                                // Respond to neutral button press
                            }
                            .show()
                    }
                }
            }
        }

        viewModel.addAddressResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {}
                is Result.Success -> {
                    if(result.data != null){
                        Toast.makeText(requireContext(),"Address Successfully added.", Toast.LENGTH_SHORT).show()
//                        this@RecipientDeliveryDialog.dismiss()
                        viewModel.getCustomerProfile()

                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(),result.metaResponse.message, Toast.LENGTH_SHORT).show()
                }
                is Result.HttpError -> {
                    if(result.error is ProfileRepository.AreaLaunchingSoonException){
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(result.error.title)
                            .setMessage(result.error.message)
                            .setPositiveButton("OK") { dialog, which ->
                                // Respond to neutral button press
                            }
                            .show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADDRESS_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = data.extras?.getParcelable<Place>(SearchPlaceDialog.EXTRA_PLACE)!!
                        Timber.d("Place: ${place.name}, ${place.id}")
                        enterAddressViewModel.checkLocationIfValid(
                            Place.builder()
                                .setName(et_address.text.toString()) // overwrite label
                                .setAddress(place.address)
                                .setLatLng(place.latLng)
                                .build()
                        )
                    }
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun showPlacesAutocomplete(requestCode: Int){
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

        var locationBias: LocationBias? = if(RiderTrackingService.latLong.value != null){
            val latLng = RiderTrackingService.latLong.value!!

            val meters = 500.0

            val coef = meters * 0.0000089

            val latMax = latLng.latitude + coef
            val lngMax = latLng.longitude + coef / Math.cos(latLng.latitude * 0.18)

            val latMin = latLng.latitude - coef
            val lngMin = latLng.longitude - coef / Math.cos(latLng.latitude * 0.18)


            /*  val northSide = SphericalUtil.computeOffset(latLng,5000.0,0.0)
              val southSide = SphericalUtil.computeOffset(latLng,5000.0,180.0)
  */

            val northSide = LatLng(latMax,lngMax)
            val southSide = LatLng(latMin,lngMin)
            val bounds = LatLngBounds.builder()
                .include(northSide)
                .include(southSide)
                .build()
            Timber.d("location bias detected.")

            Timber.d("northside: https://www.google.com/maps/place/${northSide.latitude},${northSide.longitude}")
            Timber.d("southside: https://www.google.com/maps/place/${southSide.latitude},${southSide.longitude}")
            Timber.d("path northside to southside: https://www.google.com/maps/dir/?api=1&origin=${northSide.latitude},${northSide.longitude}&destination=${southSide.latitude},${southSide.longitude}")
            RectangularBounds.newInstance(bounds)
        }else{
            null
        }
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountry("PH")
//        intent.build(requireContext())
        startActivityForResult(intent.setLocationBias(locationBias).build(requireContext()), requestCode)
    }

    private fun initMap(place: Place) {

        // INIT MAP
        mapFragment?.getMapAsync { googleMap ->


            googleMap.apply {
                mapType = GoogleMap.MAP_TYPE_NORMAL
                uiSettings.apply {
                    isZoomControlsEnabled = true
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabledDuringRotateOrZoom = true
                }

                if(locationMarker == null){
                    locationMarker = addMarker(
                        MarkerOptions()
                            .title(place.name)
                            .position(place.latLng!!)
                    )
                }else{
                    locationMarker?.position = place.latLng!!
                }
//                cameraPosition = CameraUpdateFactory.newLatLngZoom(pickupLatLong, 15F)
                moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 15F))
            }

        }

    }
}

@Parcelize
enum class PERSON_TYPE(val label: String): Parcelable {
    SENDER("Sender"),
    RECIPIENT("Recipient")
}
