package com.myoptimind.getexpress.features.customer.cart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.cart.data.*
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.data.CartType
import com.myoptimind.getexpress.features.shared.data.idToCartType
import com.myoptimind.getexpress.features.shared.data.toCartStatus
import com.myoptimind.getexpress.features.shared.toCartLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_customer_cart.*
import kotlinx.android.synthetic.main.fragment_customer_cart.group_sub_total_and_delivery_fee
import kotlinx.android.synthetic.main.fragment_customer_cart.label_summary
import kotlinx.android.synthetic.main.fragment_customer_cart.tv_delivery_fee
import kotlinx.android.synthetic.main.fragment_customer_cart.tv_sub_total
import kotlinx.android.synthetic.main.fragment_customer_cart.tv_total
import timber.log.Timber

@AndroidEntryPoint
class CustomerCartFragment: TitleOnlyFragment() {

//    private val viewModel by activityViewModels<StoresViewModel>()
    private val cartViewModel by activityViewModels<CartViewModel>()
    private val AUTOCOMPLETE_REQUEST_CODE = 150
    private var adapter: CartItemsAdapter? = null
    private var mapFragment: SupportMapFragment? = null
    private val args by navArgs<CustomerCartFragmentArgs>()

    override fun getTitle() = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_customer_cart,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment




        initObservers()
        initClickListeners()
//        cartViewModel.updateToLocation(args.senderLocation)


    }

    private fun showPlacesAutocomplete(){
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext())
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun initClickListeners() {
        tv_add_items.setOnClickListener {
            if(findNavController().currentDestination?.id == R.id.customerCartFragment){
                findNavController().popBackStack()
            }
        }

/*        btn_get.setOnClickListener {
            Snackbar.make(requireView(),"Please select an address.",Snackbar.LENGTH_SHORT).show()
        }*/
    }

    private fun initObservers() {




        cartViewModel.cart.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        val cart = result.data.data
                        cartViewModel.cartId = cart.id
                        val cartType = cart.cartTypeId.idToCartType()
                        val cartStatus = cart.status.toCartStatus()

                        setUpObservables(cart)

                        when(cartType){
                            CartType.FOOD,CartType.GROCERY -> {
                                tv_edit_location.visibility = View.GONE
                                tv_delivery_to_location.text = cart.deliveryLocation.label + "\n" + cart.deliveryLocation.addressText
                                tv_change_address.visibility = View.GONE
                            }
                            else -> {
                                tv_edit_location.setOnClickListener {
                                    showPlacesAutocomplete()
                                }
                            }
                        }


                        when(cartType){
                            CartType.CAR -> TODO()
                            CartType.GROCERY -> {
                                val basketForFoodGrocery = cart.initBasketForGrocery()
                                if(basketForFoodGrocery.items.isEmpty()){
                                    findNavController().popBackStack()
                                }else{
                                    rv_orders.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                                    adapter = CartItemsAdapter(basketForFoodGrocery.items,cartType, object: CartItemsAdapter.CartItemListener{
                                        override fun onPressed(cartItem: CartItem, isIncrement: Boolean) {
                                            cartItem as ItemInFoodGrocery
                                            cartViewModel.addItemToCart(
                                                    cartItem.cartId,
                                                    cartItem.productId,
                                                    (if(isIncrement) cartItem.quantity.toInt() + 1 else cartItem.quantity.toInt() - 1).toString(),
                                                    cartItem.notes,
                                                    null,
                                                    cartItem.cartItemId
                                            )
                                        }
                                    })
                                    rv_orders.adapter = adapter
                                    adapter?.notifyDataSetChanged()

                                    tv_sub_total.text = basketForFoodGrocery.subTotal
                                    tv_delivery_fee.text = basketForFoodGrocery.deliveryFee
                                    tv_total.text = basketForFoodGrocery.grandTotal
                                }
                            }
                            CartType.FOOD -> {
                                val basketForFoodGrocery = cart.initBasketForFood()
                                if(basketForFoodGrocery.items.isEmpty()){
                                    findNavController().popBackStack()
                                }else{
                                    rv_orders.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                                    adapter = CartItemsAdapter(basketForFoodGrocery.items,cartType, object: CartItemsAdapter.CartItemListener{
                                        override fun onPressed(cartItem: CartItem, isIncrement: Boolean) {
                                            cartItem as ItemInFoodGrocery
                                            cartViewModel.addItemToCart(
                                                    cartItem.cartId,
                                                    cartItem.productId,
                                                    (if(isIncrement) cartItem.quantity.toInt() + 1 else cartItem.quantity.toInt() - 1).toString(),
                                                    cartItem.notes,
                                                    cartItem.addons.map{ it.id },
                                                    cartItem.cartItemId
                                            )
                                        }
                                    })
                                    rv_orders.adapter = adapter
                                    adapter?.notifyDataSetChanged()

                                    tv_sub_total.text = basketForFoodGrocery.subTotal
                                    tv_delivery_fee.text = basketForFoodGrocery.deliveryFee
                                    tv_total.text = basketForFoodGrocery.grandTotal
                                }
                            }
                            CartType.PABILI -> TODO()
                            CartType.DELIVERY -> {
                                label_summary.text = "Delivery Summary"
                                group_sub_total_and_delivery_fee.visibility = View.GONE
                                val deliveryBasket = cart.initBasketForDelivery()
                                Timber.d(deliveryBasket.toString())

                                val items = ArrayList<CartItem>().apply {
                                    add(deliveryBasket)
                                }
                                tv_total.text = deliveryBasket.grandTotal

                                rv_orders.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                                val basketAdapter = BasketAdapter(
                                        items,CartType.DELIVERY
                                )
                                rv_orders.adapter = basketAdapter
                                basketAdapter.itemList = items
                                basketAdapter.notifyDataSetChanged()



                                tv_edit_location.setOnClickListener(null)
                                tv_change_address.visibility = View.GONE
                                tv_add_items.visibility = View.GONE

                                if(args.senderLocation != null){
//                                    cartViewModel.updateLocation(args.receiverLocation!!)
                                    tv_delivery_to_location.text = args.receiverLocation!!.name + "\n" + args.receiverLocation!!.address
                                    tv_edit_location.visibility = View.GONE
                                    initMap(args.receiverLocation!!.latLng!!,args.receiverLocation!!.name!!)
                                }
                                Timber.v("sender address : ${args.senderLocation!!.address!!}")
                                Timber.v("receiver address : ${args.receiverLocation!!.address!!}")

                            }

                        }
//                        val basket = cart.initBasketForFoodGrocery()
                        Timber.d("cart is ${cart.id}")
                        if(cartStatus == CartStatus.PENDING){
                            Timber.d("finalize_cart " + result.data.meta.message)
                            CustomerCartFragmentDirections.actionCustomerCartFragmentToCustomerRiderSearchFragment(cart.id).also {
                                findNavController().navigate(it)
                            }
                            return@observe
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

/*        viewModel.finalizeCartResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null) {
                        val cart = result.data.data
                        val cartStatus = cart.status.toCartStatus()
                        if(cartStatus == CartStatus.PENDING){
                            Timber.d("finalize_cart " + result.data.meta.message)
                                CustomerCartFragmentDirections.actionCustomerCartFragmentToCustomerRiderSearchFragment(cart.id).also {
                                    findNavController().navigate(it)
                                }
//                            findNavController().navigate(R.id.action_customerCartFragment_to_customerRiderSearchFragment)
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
        }*/
    }

    private fun setUpObservables(cart: Cart) {
        when(cart.cartTypeId.idToCartType()){
            CartType.CAR -> TODO()
            CartType.GROCERY,CartType.FOOD -> {

                cartViewModel.toLocation.observe(viewLifecycleOwner){ place ->
                    if(place != null){
                        val latlng = place.latLng!!

                        initMap(latlng, place.name!!)
                        tv_delivery_to_location.text = place.name + "\n" + place.address
                        Timber.v("place not null")
                            btn_get.setOnClickListener {
                                cartViewModel.finalizeCart(
                                        cartViewModel.cartId!!,
                                        "notes",
                                        pickupLocation = null,
                                        CartLocation.fromPlace(place),
                                        "COD"
                                )
                            }
                    }
                }
            }
            CartType.PABILI -> TODO()
            CartType.DELIVERY -> {
                    btn_get.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_get_delivery_now))
                    btn_get.setOnClickListener {
                        Timber.v("")
                        cartViewModel.finalizeCartForDelivery(
                                cart.id,et_notes_to_driver.text.toString(),CartLocation.fromPlace(args.senderLocation!!),CartLocation.fromPlace(args.receiverLocation!!),"COD"
                        )
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Timber.d("Place: ${place.name}, ${place.id}")
                        cartViewModel.updateFromLocation(place)
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

    private fun initMap(latLng: LatLng, label: String) {

        // INIT MAP
        mapFragment?.getMapAsync { googleMap ->


            googleMap.apply {
                mapType = GoogleMap.MAP_TYPE_NORMAL
                uiSettings.apply {
                    isZoomControlsEnabled = false
                    isZoomGesturesEnabled = false
                    isScrollGesturesEnabledDuringRotateOrZoom = false
                }

                addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(label)
                )
                moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
            }

        }

    }
}