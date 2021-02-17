package com.myoptimind.get_express.features.edit_profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.Address
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.izNotBlank
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_add_address.*
import kotlinx.android.synthetic.main.dialog_add_address.ib_close
import timber.log.Timber

//14.651763,121.049318

@AndroidEntryPoint
class AddAddressDialog: BaseDialogFragment() {

    private val viewModel by activityViewModels<ProfileViewModel>()


    companion object {

        private const val ADDRESS_REQUEST = 300
        private const val ARGS_SAVE_ADDRESS = "ARGS_SAVE_ADDRESS"
        private const val ARGS_ADDRESS = "ARGS_ADDRESS"


        fun newInstance(address: Address? = null): AddAddressDialog {
            val args = Bundle()
            args.putParcelable(ARGS_ADDRESS,address)
            val fragment = AddAddressDialog ()
            fragment.arguments = args
            return fragment
        }
    }
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_add_address,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val address = requireArguments().getParcelable<Address>(ARGS_ADDRESS)
        initClickListeners(address)
        initObservers()
        if(address != null){
            btn_save_address.text = "Update Address"
            label_add_address.text = "Edit Address"
            viewModel.updateSelectedPlace(Place.builder()
                .setLatLng(LatLng(address.latitude.toDouble(),address.longitude.toDouble()))
                .setAddress(address.fullAddress)
                .build())

//            et_enter_address.setText(address.fullAddress)

            et_address_label.setText(address.label)

        }else{
            viewModel.updateSelectedPlace(null)
        }
        

    }

    private fun initObservers() {
        viewModel.selectedPlace.observe(viewLifecycleOwner){ selectedPlace ->
            if(selectedPlace != null){
                et_enter_address.setText(selectedPlace.address)
            }
        }

        viewModel.addAddressResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {}
                is Result.Success -> {
                    if(result.data != null){
                        Toast.makeText(requireContext(),result.data.meta.message,Toast.LENGTH_SHORT).show()
                        this@AddAddressDialog.dismiss()
                        viewModel.getCustomerProfile()

                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(),result.metaResponse.message,Toast.LENGTH_SHORT).show()
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

    private fun initClickListeners(address: Address?) {
        ib_close.setOnClickListener {
            this@AddAddressDialog.dismiss()
        }
        btn_save_address.setOnClickListener {
            if(et_address_label.izNotBlank() && et_enter_address.izNotBlank()){
                val addressId = address?.id ?: "0"
                viewModel.addAddress(
                        et_address_label.text.toString(),
                        addressId
                )
            }else{
                Toast.makeText(requireContext(),"Please fill in required fields.", Toast.LENGTH_LONG).show()
            }
        }

        et_enter_address.setOnClickListener {
            showPlacesAutocomplete(ADDRESS_REQUEST)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADDRESS_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Timber.d("Place: ${place.name}, ${place.id}")
                        if (requestCode == ADDRESS_REQUEST) {
//                            et_enter_address.setText(place.address)
                            viewModel.updateSelectedPlace(place)
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Timber.d(status.statusMessage)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}