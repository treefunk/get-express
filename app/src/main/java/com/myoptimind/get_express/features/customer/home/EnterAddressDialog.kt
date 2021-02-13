package com.myoptimind.get_express.features.customer.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog.Companion.EXTRA_ENTER_ADDRESS
import com.myoptimind.get_express.features.edit_profile.ProfileRepository
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.izNotBlank
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_add_address.*
import timber.log.Timber

@AndroidEntryPoint
class EnterAddressDialog: BaseDialogFragment() {


    private val viewModel by viewModels<EnterAddressViewModel>()

    companion object {

        private const val ADDRESS_REQUEST = 300


        fun newInstance(): EnterAddressDialog {
            val args = Bundle()

            val fragment = EnterAddressDialog ()
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

        label_add_address.text = "Enter Address"
        btn_save_address.text = "CONFIRM ADDRESS"
        initClickListeners()
        initObservers()
    }

    private fun initObservers() {
        viewModel.checkLocationResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        et_enter_address.setText(result.data.address)
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
    }

    private fun initClickListeners() {
        ib_close.setOnClickListener {
            this@EnterAddressDialog.dismiss()
        }
        btn_save_address.setOnClickListener {
            if(et_address_label.izNotBlank() && et_enter_address.izNotBlank() && viewModel.selectedPlace.value != null){
                targetFragment?.onActivityResult(
                        targetRequestCode,
                        Activity.RESULT_OK,
                        Intent().putExtra(EXTRA_ENTER_ADDRESS,viewModel.selectedPlace.value) // PARSE THIS ("yyyy M d")
                )
                this.dismiss()
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

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountry("PH")
                .build(requireContext())
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADDRESS_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Timber.d("Place: ${place.name}, ${place.id}")
                        viewModel.checkLocationIfValid(
                                Place.builder()
                                        .setName(et_address_label.text.toString()) // overwrite label
                                        .setAddress(place.address)
                                        .setLatLng(place.latLng)
                                        .build()
                        )
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
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