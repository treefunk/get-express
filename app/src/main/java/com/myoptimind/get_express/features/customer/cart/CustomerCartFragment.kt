package com.myoptimind.get_express.features.customer.cart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.data.*
import com.myoptimind.get_express.features.customer.pabili.PabiliFormAdapter
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.CartType
import com.myoptimind.get_express.features.shared.data.idToCartType
import com.myoptimind.get_express.features.shared.data.toCartStatus
import com.myoptimind.get_express.features.shared.initMultilineEditText
import com.myoptimind.get_express.features.shared.toCartLocation
import com.myoptimind.get_express.features.shared.toMoneyFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_customer_cart.*
import timber.log.Timber


@AndroidEntryPoint
class CustomerCartFragment: TitleOnlyFragment() {

//    private val viewModel by activityViewModels<StoresViewModel>()
    private val cartViewModel by activityViewModels<CartViewModel>()
    private val AUTOCOMPLETE_REQUEST_CODE = 150
    private var adapter: CartItemsAdapter? = null
    private var mapFragment: SupportMapFragment? = null
    private val args by navArgs<CustomerCartFragmentArgs>()

    //for pabili
    private lateinit var pabiliAdapter: PabiliFormAdapter
    private lateinit var itemList: ArrayList<ItemInPabili>


    override fun getTitle() = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_customer_cart,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        et_notes_to_driver.initMultilineEditText()


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



        cartViewModel.pabiliResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> { }
                is Result.Success -> {
                    if(result.data != null){
                        Timber.d("result -> %s", result)
                        if(result.data.meta.code.equals("ok")){
                            val cart = result.data.data
                            cartViewModel.finalizeCart(
                                cart.id,
                                cart.notes,
                                cartViewModel.fromLocation.value!!.toCartLocation(),
                                cartViewModel.toLocation.value!!.toCartLocation(),
                                "COD"
                            )
/*                            cartViewModel.finalizeCartForDelivery(
                                cart.id,
                                cart.notes,
                                CartLocation.fromPlace(cartViewModel.fromLocation.value!!),
                                CartLocation.fromPlace(cartViewModel.toLocation.value!!),
                                "COD"
                            )*/
                        }
                    }
                }
                is Result.Error -> {
                    Timber.d("result -> %s", result.metaResponse.message)

                }
                is Result.HttpError -> {
                    Timber.d("result -> %s", result.error.message)

                }
            }
        }


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

                                    tv_sub_total.text = basketForFoodGrocery.subTotal.toMoneyFormat()
                                    tv_delivery_fee.text = basketForFoodGrocery.deliveryFee.toMoneyFormat()
                                    tv_total.text = basketForFoodGrocery.grandTotal.toMoneyFormat()
                                    label_delivery_to.text = "Deliver to (${basketForFoodGrocery.distanceInKm} km)"
                                }
                                btn_get.setOnClickListener {
                                    cartViewModel.finalizeCart(
                                            cartViewModel.cartId!!,
                                            "notes",
                                            pickupLocation = null,
                                            deliveryLocation = CartLocation.fromPlace(cartViewModel.toLocation.value!!),
                                            paymentType = "COD"
                                    )
                                }
                            }
                            CartType.FOOD -> {
                                btn_get.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_get_food_now))
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

                                    tv_sub_total.text = basketForFoodGrocery.subTotal.toMoneyFormat()
                                    tv_delivery_fee.text = basketForFoodGrocery.deliveryFee.toMoneyFormat()
                                    tv_total.text = basketForFoodGrocery.grandTotal.toMoneyFormat()
                                    label_delivery_to.text = "Deliver to (${basketForFoodGrocery.distanceInKm} km)"


                                    btn_get.setOnClickListener {
                                        cartViewModel.finalizeCart(
                                                cartViewModel.cartId!!,
                                                "notes",
                                                pickupLocation = null,
                                                deliveryLocation = CartLocation.fromPlace(cartViewModel.toLocation.value!!),
                                                paymentType = "COD"
                                        )
                                    }
                                }
                            }
                            CartType.PABILI -> {
                                btn_get.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_get_pabili_now))
                                val pabiliBasket = cart.initBasketForPabili()
                                Timber.d(pabiliBasket.toString())

//                                group_sub_total_and_delivery_fee.visibility = View.GONE
                                label_sub_total.visibility = View.GONE
                                tv_sub_total.visibility = View.GONE
                                label_delivery_fee.text = "Estimated Total"
                                tv_delivery_fee.text = pabiliBasket.estimateTotalAmount.toMoneyFormat()
                                label_total.text = "Pabili Fee"
                                tv_total.text = pabiliBasket.deliveryFee
                                label_delivery_to.text = "Deliver to (${pabiliBasket.distanceInKm} km)"




                                itemList = ArrayList()
                                pabiliAdapter = PabiliFormAdapter(itemList, isPostForm = true, listener = object: PabiliFormAdapter.PabiliFormListener{
                                    override fun onRemove(pabiliItem: ItemInPabili, index: Int) {
                                        MaterialAlertDialogBuilder(requireContext())
                                                .setTitle("")
                                                .setMessage("Remove this item from the cart?")
                                                .setNeutralButton("NO") { dialog, which ->
                                                    // Respond to neutral button press
                                                }
                                                .setPositiveButton("YES") { dialog, which ->
                                                    itemList.removeAt(index)
                                                    pabiliAdapter.pabiliItemList = itemList
                                                    pabiliAdapter.notifyItemRemoved(index)
                                                    pabiliAdapter.notifyItemRangeChanged(index,itemList.size)
                                                    cartViewModel.updatePabiliItemList(itemList)
                                                    if(itemList.isEmpty()){
                                                        findNavController().popBackStack()
                                                    }
                                                }
                                                .show()

                                    }
                                })

                                itemList.clear()
                                itemList.addAll(pabiliBasket.items)


                                pabiliAdapter.pabiliItemList = itemList
                                rv_orders.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
                                rv_orders.adapter = pabiliAdapter
                                pabiliAdapter.notifyDataSetChanged()


                                cartViewModel.updatePabiliItemList(itemList)
                                tv_add_items.setOnClickListener {
                                    cartViewModel.updatePabiliItemList(itemList)
                                    CustomerCartFragmentDirections.actionCustomerCartFragmentToPabiliFormFragment().also {
                                        findNavController().navigate(it)
                                    }
                                }

/*                                (activity as MainActivity).ib_nav_back.setOnClickListener {
                                    it.setOnClickListener { findNavController().popBackStack() }
                                    findNavController().popBackStack()
                                }*/

                                btn_get.setOnClickListener {

                                    val list = if(this@CustomerCartFragment.itemList.isNotEmpty())
                                        this@CustomerCartFragment.itemList.map{ ItemInPabili(it.itemName,if(it.quantity.isNotEmpty()) it.quantity.toInt().toString() else "") }.filter {
                                            if(it.itemName.trim().isNotEmpty() &&
                                                    (it.quantity.trim().isEmpty())
                                            ){
                                                Snackbar.make(requireView(),"Please input a quantity for ${it.itemName}", Snackbar.LENGTH_SHORT).show()
                                                return@setOnClickListener
                                            }
                                            if(it.quantity.trim().isNotEmpty() && it.quantity.trim().toInt() <= 0){
                                                Snackbar.make(requireView(),"Please input a valid quantity for ${it.itemName}", Snackbar.LENGTH_SHORT).show()
                                                return@setOnClickListener
                                            }
                                            it.itemName.trim().isNotEmpty() && it.quantity.trim().isNotEmpty()
                                        }
                                    else
                                        ArrayList()

                                    if(list.isEmpty()){
                                        Snackbar.make(requireView(),"Please add an item to the cart", Snackbar.LENGTH_SHORT).show()
                                        return@setOnClickListener
                                    }


                                    cartViewModel.createPabili(
                                            cart.id,
                                            list,
                                            pabiliBasket.estimateTotalWithoutDeliveryFee.toDouble()
                                    )




                                }


/*
                                adapter?.items = pabiliBasket.items
                                adapter?.notifyDataSetChanged()
*/
/*                                btn_get.setOnClickListener {
                                    Timber.v("")
                                    cartViewModel.finalizeCartForDelivery(
                                            cart.id,et_notes_to_driver.text.toString(),CartLocation.fromPlace(cartViewModel.fromLocation.value!!),CartLocation.fromPlace(cartViewModel.toLocation.value!!),"COD"
                                    )
                                }*/
                            }
                            CartType.DELIVERY -> {
                                label_summary.text = "Delivery Summary"
                                group_sub_total_and_delivery_fee.visibility = View.GONE
                                val deliveryBasket = cart.initBasketForDelivery()
                                Timber.d(deliveryBasket.toString())

                                val items = ArrayList<CartItem>().apply {
                                    add(deliveryBasket)
                                }
                                tv_total.text = deliveryBasket.grandTotal.toMoneyFormat()

                                rv_orders.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                                val basketAdapter = BasketAdapter(
                                        items,CartType.DELIVERY
                                )
                                tv_edit_location.visibility = View.GONE
                                rv_orders.adapter = basketAdapter
                                basketAdapter.itemList = items
                                basketAdapter.notifyDataSetChanged()

                                label_delivery_to.text = "Deliver to (${deliveryBasket.distanceInKm} km)"




                                tv_edit_location.setOnClickListener(null)
                                tv_change_address.visibility = View.GONE
                                tv_add_items.visibility = View.GONE

/*                                if(cartViewModel.toLocation.value != null){
//                                    cartViewModel.updateLocation(args.receiverLocation!!)
                                    val toLocation = cartViewModel.toLocation.value!!
                                    tv_delivery_to_location.text = toLocation.name + "\n" + toLocation.address
                                    tv_edit_location.visibility = View.GONE
                                    initMap(toLocation.latLng!!,toLocation.name!!)
                                }*/

                                btn_get.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_get_delivery_now))
                                btn_get.setOnClickListener {
                                    Timber.v("")
                                    cartViewModel.finalizeCartForDelivery(
                                            cart.id,et_notes_to_driver.text.toString(),
                                        CartLocation.fromPlace(cartViewModel.fromLocation.value!!),
                                        CartLocation.fromPlace(cartViewModel.toLocation.value!!),"COD"
                                    )
                                }
                            }

                        }
//                        val basket = cart.initBasketForFoodGrocery()
                        Timber.d("cart is ${cart.id}")
                        if(cartStatus == CartStatus.PENDING){
                            Timber.d("finalize_cart " + result.data.meta.message)
                            if(findNavController().currentDestination?.id == R.id.customerCartFragment){
                                CustomerCartFragmentDirections.actionCustomerCartFragmentToCustomerRiderSearchFragment(cart.id).also {
                                    findNavController().navigate(it)
                                }
                                return@observe
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


/*        cartViewModel.fromLocation.observe(viewLifecycleOwner){ place ->
            if(place != null){
                val latlng = place.latLng!!

                initMap(latlng, place.name!!)
                tv_from_location.text = place.name + "\n" + place.address
            }
        }*/

        cartViewModel.toLocation.observe(viewLifecycleOwner){ place ->
            if(place != null){
                val latlng = place.latLng!!

                initMap(latlng, place.name!!)
                tv_delivery_to_location.text = place.name + "\n" + place.address
                tv_edit_location.visibility = View.GONE
                Timber.v("place not null")
            }
        }
//        when(cart.cartTypeId.idToCartType()){
//            CartType.CAR -> TODO()
//            CartType.GROCERY,CartType.FOOD -> {
//
//
//            }
//            CartType.PABILI -> {
//            }
//        }
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