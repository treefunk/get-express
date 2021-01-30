package com.myoptimind.getexpress.features.customer.delivery

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.cart.CartViewModel
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_delivery_form.*
import timber.log.Timber


@AndroidEntryPoint
class DeliveryFormFragment: TitleOnlyFragment() {

    companion object {
        private const val RECEIVER_ADDRESS_REQUEST = 200
        private const val SENDER_ADDRESS_REQUEST = 300
    }

    override fun getTitle() = "Get Delivery"
    private val viewModel by activityViewModels<DeliveryViewModel>()
    private val cartViewModel by activityViewModels<CartViewModel>()
    private val args by navArgs<DeliveryFormFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_delivery_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        viewModel.getDeliveryFormDetails(args.vehicleTypeId)
    }

    private fun initObservers() {
        viewModel.deliveryFormDetails.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
                        val data = result.data.data
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, data.categories)
                        label_weight_description.text = data.vehicleWeightCapacityText
                        label_weight_description.setTypeface(label_weight_description.typeface, Typeface.ITALIC)
                        et_delivery_category.setAdapter(adapter)
                        et_delivery_category.setOnClickListener {
                            et_delivery_category.showDropDown()
                        }
                        et_delivery_category.setOnItemClickListener { _, _, index, id ->
                            val selectedCategory = data.categories[index]
                            et_delivery_category.tag = selectedCategory.toString()
                            Timber.v("vehicle id selected: ${et_delivery_category.tag}")
                            et_delivery_category.setText(selectedCategory, false)
                        }

                        tv_sender_address.setOnClickListener {
                            showPlacesAutocomplete(SENDER_ADDRESS_REQUEST)
                        }
                        tv_sender_name.setOnClickListener {
                            showPlacesAutocomplete(SENDER_ADDRESS_REQUEST)
                        }
                        iv_sender_icon.setOnClickListener {
                            showPlacesAutocomplete(SENDER_ADDRESS_REQUEST)
                        }

                        tv_receiver_address.setOnClickListener {
                            showPlacesAutocomplete(RECEIVER_ADDRESS_REQUEST)
                        }

                        tv_receiver_name.setOnClickListener {
                            showPlacesAutocomplete(RECEIVER_ADDRESS_REQUEST)
                        }

                        iv_receiver_icon.setOnClickListener {
                            showPlacesAutocomplete(RECEIVER_ADDRESS_REQUEST)
                        }


                        btn_getdelivery.setOnClickListener {

                            if(viewModel.senderAddress == null || viewModel.receiverAddress == null){
                                Toast.makeText(requireContext(),"Please select pickup and destination locations",Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            if(et_delivery_category.text.toString().isEmpty()){
                                Toast.makeText(requireContext(),"Please select a category",Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            val results = FloatArray(1)
                            val startLatLng = viewModel.senderAddress!!.latLng
                            val endLatLng = viewModel.receiverAddress!!.latLng


                            Location.distanceBetween(startLatLng!!.latitude, startLatLng.longitude,
                                    endLatLng!!.latitude, endLatLng.longitude,
                                    results)

                            val distance = results[0].toString()

                            cartViewModel.createDelivery(
                                    args.cartId,
                                    et_delivery_instructions.text.toString(),
                                    et_delivery_category.text.toString(),
                                    distance
                            )

                            Timber.v("sender address : ${viewModel.senderAddress!!.address!!}")
                            Timber.v("receiver address : ${viewModel.receiverAddress!!.address!!}")
                            Timber.v("distance: ${distance}")

                            DeliveryFormFragmentDirections.actionDeliveryFormFragmentToCustomerCartFragment(viewModel.senderAddress,viewModel.receiverAddress).also {
                                findNavController().navigate(it)
                            }
                        }


                    }
                }
                is Result.Error -> {
                    Timber.e(result.metaResponse.message)
                }
                is Result.HttpError -> {
                    Timber.e(result.error.message)
                }
            }
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
        if (requestCode == RECEIVER_ADDRESS_REQUEST || requestCode == SENDER_ADDRESS_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Timber.d("Place: ${place.name}, ${place.id}")
                        if (requestCode == SENDER_ADDRESS_REQUEST) {
                            tv_sender_name.text = place.name
                            tv_sender_address.text = place.address
                            viewModel.senderAddress = place
                        }
                        if (requestCode == RECEIVER_ADDRESS_REQUEST) {
                            tv_receiver_name.text = place.name
                            tv_receiver_address.text = place.address
                            viewModel.receiverAddress = place
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