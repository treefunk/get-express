package com.myoptimind.getexpress.features.edit_profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.delivery.DeliveryFormFragment
import com.myoptimind.getexpress.features.shared.BaseDialogFragment
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.izNotBlank
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_add_address.*
import kotlinx.android.synthetic.main.dialog_add_address.ib_close
import timber.log.Timber

//14.651763,121.049318

@AndroidEntryPoint
class DialogAddAddress: BaseDialogFragment() {

    private val viewModel by activityViewModels<EditProfileViewModel>()


    companion object {

        private const val ADDRESS_REQUEST = 300
        private const val ARGS_SAVE_ADDRESS = "ARGS_SAVE_ADDRESS"


        fun newInstance(): DialogAddAddress {
            val args = Bundle()

            val fragment = DialogAddAddress ()
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


        initClickListeners()
        initObservers()
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
                        Toast.makeText(requireContext(),"Successfully added.",Toast.LENGTH_SHORT).show()
                        this@DialogAddAddress.dismiss()
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

    private fun initClickListeners() {
        ib_close.setOnClickListener {
            this@DialogAddAddress.dismiss()
        }
        btn_save_address.setOnClickListener {
            if(et_address_label.izNotBlank() && et_enter_address.izNotBlank()){
                viewModel.addAddress(
                        et_address_label.text.toString()
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

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
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