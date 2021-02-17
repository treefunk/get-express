package com.myoptimind.get_express.features.customer.delivery

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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.CartViewModel
import com.myoptimind.get_express.features.customer.cart.data.ItemInPabili
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog
import com.myoptimind.get_express.features.customer.pabili.PabiliFormFragmentDirections
import com.myoptimind.get_express.features.edit_profile.ProfileViewModel
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.idToCartType
import com.myoptimind.get_express.features.shared.data.toCartStatus
import com.myoptimind.get_express.features.shared.initMultilineEditText
import com.myoptimind.get_express.features.shared.toCartLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_delivery_form.*
import kotlinx.android.synthetic.main.fragment_delivery_form.et_delivery_instructions
import kotlinx.android.synthetic.main.fragment_delivery_form.iv_receiver_icon
import kotlinx.android.synthetic.main.fragment_delivery_form.iv_sender_icon
import kotlinx.android.synthetic.main.fragment_delivery_form.label_receiver_place_holder
import kotlinx.android.synthetic.main.fragment_delivery_form.label_sender_place_holder
import kotlinx.android.synthetic.main.fragment_delivery_form.tv_receiver_address
import kotlinx.android.synthetic.main.fragment_delivery_form.tv_receiver_name
import kotlinx.android.synthetic.main.fragment_delivery_form.tv_sender_address
import kotlinx.android.synthetic.main.fragment_delivery_form.tv_sender_name
import kotlinx.android.synthetic.main.fragment_pabili_form.*
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
    private val profileViewModel by activityViewModels<ProfileViewModel>()
    private val args by navArgs<DeliveryFormFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_delivery_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        et_delivery_instructions.initMultilineEditText()
        viewModel.getDeliveryFormDetails(args.vehicleTypeId)
    }

    private fun initObservers() {

        cartViewModel.fromLocation.observe(viewLifecycleOwner){ fromLocation ->
            if(fromLocation != null){
                label_sender_place_holder.visibility = View.INVISIBLE
                tv_sender_name.text = fromLocation.name
                tv_sender_address.text = fromLocation.address
            }
        }

        cartViewModel.toLocation.observe(viewLifecycleOwner){ toLocation ->
            if(toLocation != null){
                label_receiver_place_holder.visibility = View.INVISIBLE
                tv_receiver_name.text = toLocation.name
                tv_receiver_address.text = toLocation.address
            }
        }

        profileViewModel.getCustomerProfile.observe(viewLifecycleOwner){ result ->
            if(result is Result.Success && result.data != null){
                tv_sender_address.setOnClickListener {
/*                    showAddressSelection(result.data.data.addresses,
                        SENDER_ADDRESS_REQUEST)*/
                    showAddressSelectionWithMap(PERSON_TYPE.SENDER,
                        SENDER_ADDRESS_REQUEST)
                }
                tv_sender_name.setOnClickListener {
/*                    showAddressSelection(result.data.data.addresses,
                        SENDER_ADDRESS_REQUEST)*/
                    showAddressSelectionWithMap(PERSON_TYPE.SENDER,
                        SENDER_ADDRESS_REQUEST)
                }
                iv_sender_icon.setOnClickListener {
/*                    showAddressSelection(result.data.data.addresses,
                        SENDER_ADDRESS_REQUEST)*/
                    showAddressSelectionWithMap(PERSON_TYPE.SENDER,
                        SENDER_ADDRESS_REQUEST)
                }

                tv_receiver_address.setOnClickListener {
/*                    showAddressSelection(result.data.data.addresses,
                        RECEIVER_ADDRESS_REQUEST)*/
                    showAddressSelectionWithMap(PERSON_TYPE.RECIPIENT,
                        RECEIVER_ADDRESS_REQUEST)
                }

                tv_receiver_name.setOnClickListener {
/*                    showAddressSelection(result.data.data.addresses,
                        RECEIVER_ADDRESS_REQUEST)*/
                    showAddressSelectionWithMap(PERSON_TYPE.RECIPIENT,
                        RECEIVER_ADDRESS_REQUEST)
                }

                iv_receiver_icon.setOnClickListener {
/*                    showAddressSelection(result.data.data.addresses,
                        RECEIVER_ADDRESS_REQUEST)*/
                    showAddressSelectionWithMap(PERSON_TYPE.RECIPIENT,
                        RECEIVER_ADDRESS_REQUEST)
                }

            }
        }

        cartViewModel.cart.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
                        val cart = result.data.data




                        if (result.data.meta.code.equals("ok2")) {
                            if (result.data != null && result.data.meta.status == 200) {
                                cartViewModel.setCartInfo(result)

                                Timber.v("pre finalizing...")
                                DeliveryFormFragmentDirections.actionDeliveryFormFragmentToCustomerCartFragment().also {
                                    findNavController().navigate(it)
                                }
                                result.data.meta.code = "ok"
                            }
                            return@observe
                        }

                        cartViewModel.cartId = cart.id
                        val cartType = cart.cartTypeId.idToCartType()
                        val cartStatus = cart.status.toCartStatus()
                        val deliveryBasket = cart.initBasketForDelivery()

/*                        if (cart.pickUpLocation.addressText.isBlank().not()) {
                            cartViewModel.updateFromLocation(cart.pickUpLocation.toPlace())
                        }
                        if (cart.deliveryLocation.addressText.isBlank().not()) {
                            cartViewModel.updateToLocation(cart.deliveryLocation.toPlace())
                        }*/
                        et_delivery_category.setText(deliveryBasket.category)
                        et_delivery_instructions.setText(deliveryBasket.notes)


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

        cartViewModel.deliveryResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        val cart = result.data.data
                        if(result.data.meta.status == 200){

                            cartViewModel.setCartInfo(result)

                            cartViewModel.finalizeCartForDelivery(
                                cart.id,
                                cart.notes,
                                cartViewModel.fromLocation.value!!.toCartLocation(),
                                cartViewModel.toLocation.value!!.toCartLocation(),
                                "COD",
                                false
                            )


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

                        btn_getdelivery.setOnClickListener {

                            if(cartViewModel.fromLocation.value == null || cartViewModel.toLocation.value == null){
                                Toast.makeText(requireContext(),"Please select pickup and destination locations",Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            if(et_delivery_category.text.toString().isBlank()){
                                Toast.makeText(requireContext(),"Please select a category",Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            val results = FloatArray(1)
                            val startLatLng = cartViewModel.toLocation.value!!.latLng
                            val endLatLng = cartViewModel.fromLocation.value!!.latLng


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
        if (requestCode == RECEIVER_ADDRESS_REQUEST || requestCode == SENDER_ADDRESS_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
//                        val place = Autocomplete.getPlaceFromIntent(data)
                        val place = it.getParcelableExtra<Place>(SelectAddressBottomDialog.EXTRA_ENTER_ADDRESS)!!
                        Timber.d("Place: ${place.name}, ${place.id}")
                        if (requestCode == SENDER_ADDRESS_REQUEST) {
                            tv_sender_name.text = place.name
                            tv_sender_address.text = place.address
                            cartViewModel.updateFromLocation(place)
                        }
                        if (requestCode == RECEIVER_ADDRESS_REQUEST) {
                            tv_receiver_name.text = place.name
                            tv_receiver_address.text = place.address
//                            viewModel.receiverAddress = place
                            cartViewModel.updateToLocation(place)
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