package com.myoptimind.get_express.features.customer.cart.rider_search

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.CartViewModel
import com.myoptimind.get_express.features.customer.cart.rider_search.data.TrackingPayload
import com.myoptimind.get_express.features.login.data.VehicleType
import com.myoptimind.get_express.features.login.data.idToVehicleType
import com.myoptimind.get_express.features.customer.cart.data.BasketAdapter
import com.myoptimind.get_express.features.customer.cart.data.CartItem
import com.myoptimind.get_express.features.customer.cart.data.CartLocation
import com.myoptimind.get_express.features.customer.cart.data.CartStatus
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.CartType
import com.myoptimind.get_express.features.shared.data.idToCartType
import com.myoptimind.get_express.features.shared.data.toCartStatus
import com.myoptimind.get_express.features.shared.toMoneyFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_customer_rider_search.*
import kotlinx.android.synthetic.main.fragment_customer_rider_search.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

private const val REFRESH_INTERVAL_SECOND = 10

@AndroidEntryPoint
class CustomerRiderSearchFragment : TitleOnlyFragment() {

    companion object {

        private const val ACTION_STATUS_CHANGE = "action_status_change"
        private const val EXTRA_TRACKING_PAYLOAD = "extra_tracking_payload"

        fun createBroadcastIntent(trackingPayload: TrackingPayload): Intent {
            return Intent().also { intent ->
                intent.action = ACTION_STATUS_CHANGE
                intent.putExtra(EXTRA_TRACKING_PAYLOAD, trackingPayload)
            }
        }
    }

    private val cartViewModel by activityViewModels<CartViewModel>()
    private var mapFragment: SupportMapFragment? = null
    private var riderMarker: Marker? = null
    private val args by navArgs<CustomerRiderSearchFragmentArgs>()

    private var adapter: BasketAdapter? = null

    private lateinit var onCancelBackCallback: OnBackPressedCallback
    private lateinit var onBackWhenAcceptedBookingCallback: OnBackPressedCallback

    @Inject
    lateinit var appSharedPref: AppSharedPref


    private var getCartInformationJob: Job? = null
    private var loadingTextJob: Job? = null
    private var expiryTimerJob: Job? = null

    private val trackingBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Now you can call all your fragments method here
            Timber.d("broadcast received!")
            if(intent.action == ACTION_STATUS_CHANGE){
                if(cartViewModel.cartId != null){
                    val cartId = cartViewModel.cartId!!
                    cartViewModel.getCartInformation(cartId)
                    getCartInformationJob.run {
                        this?.cancel()
                        getCartInformationJob = refresh(cartId)
                        getCartInformationJob?.start()
                    }
                }
            }


        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onCancelBackCallback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                cancelBooking()
            }
        }

        onBackWhenAcceptedBookingCallback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Toast.makeText(requireContext(),"Please finish ongoing booking first",Toast.LENGTH_SHORT).show()
            }
        }



    }




    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter().also { it.addAction(ACTION_STATUS_CHANGE) }
        requireContext().registerReceiver(trackingBroadcastReceiver,intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(trackingBroadcastReceiver)
    }

    private fun refresh(cartId: String) = lifecycleScope.launchWhenResumed {
        for (i in REFRESH_INTERVAL_SECOND downTo 1) {
            Timber.d("hashcode - ${this.hashCode()} refreshing in $i..")
            delay(1000)
        }
        cartViewModel.getCartInformation(cartId,true)
    }

    private fun timerTask() = lifecycleScope.launchWhenCreated {
            delay(TimeUnit.MINUTES.toMillis(5))
            cartViewModel.cancelBooking(args.cartId)
//            Toast.makeText(requireContext(),"Looks like our riders are full at the moment, please try again.",Toast.LENGTH_LONG).show()
            Snackbar.make(requireView(),"Looks like our riders are full at the moment, please try again.",Snackbar.LENGTH_LONG).show()
    }

    private fun loadingTask() = lifecycleScope.launchWhenResumed {
        lifecycleScope.launch(IO){
            for (i in 1..5){
                delay(2000)
                withContext(Main){
                    val sb = StringBuilder()
                    sb.append("Looking for your rider")
                    var y = i
                    while(y != 0){
                        sb.append(".")
                        y--
                    }
                    if(tv_looking_for != null){
                        tv_looking_for.text = sb.toString()
                    }
                }
            }
        }
    }


    private fun updateMapLocation(cartId: String, vehicleType: VehicleType, latitude: Double, longitude: Double) = lifecycleScope.launchWhenCreated {
        Timber.d("hashcode - ${this.hashCode()} Retrieving Cart information (job)..")
        val latLong = LatLng(latitude, longitude)
        updateMapByLatLong(vehicleType, latLong)
    }

    private fun updateMapByLatLong(vehicleType: VehicleType, latLong: LatLng) {


        mapFragment?.getMapAsync { googleMap ->

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 18F))
            val markerOptions = MarkerOptions()
                    .position(latLong)
                    .title("You")
                    .icon(BitmapDescriptorFactory.fromResource(vehicleType.drawableId))
            if (riderMarker == null) {
                riderMarker = googleMap.addMarker(
                        markerOptions
                )
            } else {
                riderMarker?.position = latLong
            }

        }
    }

    override fun getTitle() = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,onCancelBackCallback)
        return inflater.inflate(R.layout.fragment_customer_rider_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        val receiver = CustomerRiderSearchReceiver()




        initLocationObserver(args.cartId)
        cartViewModel.getCartInformation(args.cartId)


        rv_orders.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        adapter = BasketAdapter(ArrayList())
        rv_orders.adapter = adapter

        initClickListeners()
    }

    private fun initClickListeners() {
        btn_cancel.setOnClickListener {
            cancelBooking()
        }
    }

    private fun cancelBooking(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("")
            .setMessage("Are you sure you want to cancel this booking?")
            .setNeutralButton("NO") { dialog, which ->
                // Respond to neutral button press
            }
            .setPositiveButton("YES") { dialog, which ->
                cartViewModel.cancelBooking(args.cartId)
            }
            .show()
    }

    private fun initMap(pickupLocation: CartLocation, deliveryLocation: CartLocation) {

        // INIT MAP
        if(riderMarker != null){
            return
        }
        mapFragment?.getMapAsync { googleMap ->



            googleMap.apply {
                mapType = GoogleMap.MAP_TYPE_NORMAL
                uiSettings.apply {
                    isZoomControlsEnabled = true
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabledDuringRotateOrZoom = true
                }

                if(pickupLocation.latitude.isNotEmpty() && pickupLocation.longitude.isNotEmpty()){
                    val pickupLatLong = LatLng(pickupLocation.latitude.toDouble(), pickupLocation.longitude.toDouble())
                    addMarker(
                            MarkerOptions()
                                    .position(pickupLatLong)
                                    .title("Pickup Location: ${pickupLocation.label}")
                    )
                    val destinationLatLong = LatLng(deliveryLocation.latitude.toDouble(), deliveryLocation.longitude.toDouble())


//                cameraPosition = CameraUpdateFactory.newLatLngZoom(pickupLatLong, 15F)
                    if (riderMarker == null) {
                        moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLong, 15F))
                    }
                    addMarker(
                            MarkerOptions()
                                    .position(destinationLatLong)
                                    .title("Delivery Location: ${deliveryLocation.label}")
                    )
                }

            }

        }

    }

    private fun initLocationObserver(cartId: String) {
        cartViewModel.cart.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {
                    initCenterProgress(result.isLoading)
                    btn_cancel.isEnabled = result.isLoading.not()
                }
                is Result.Success -> {
                    if (result.data != null) {
                        val cart = result.data.data

                        val phone = cart.rider.mobileNum


                        card_customer_request.box_call.setOnClickListener {
                                if(phone.isNotBlank()){
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone))
                                    startActivity(intent)
                                }else{
                                    Toast.makeText(requireContext(),"Contact Number is not available",Toast.LENGTH_SHORT).show()
                                }
                        }

                        card_customer_request.box_message.setOnClickListener {

                            if(phone.isNotBlank()){
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null)))
                            }else{
                                Toast.makeText(requireContext(),"Contact Number is not available.",Toast.LENGTH_SHORT).show()
                            }
                        }


                        cartViewModel.cartId = cart.id

                        tv_to_location.text = cart.deliveryLocation.label + "\n" + cart.deliveryLocation.addressText

                        val cartType = cart.cartTypeId.idToCartType()
                        val cartStatus = cart.status.toCartStatus()
                        tv_customer_details.text = "${cart.rider.vehicleModel} | ${cart.rider.plateNumber}"
                        tv_additional_notes_to_rider.text = cart.notes

                        setNewTitle(cartType.label)
                        /** B A S K E T **/
                        val basketMap = cart.basket as Map<*, *>
                        adapter?.listType = cartType


                        Glide.with(requireContext())
                                .load(cartType.drawableId)
                                .into(iv_icon)

                        if(cartStatus != CartStatus.PENDING){
                            expiryTimerJob?.cancel()
                            expiryTimerJob = null
                            Timber.d("not pending")
                            onCancelBackCallback.remove()

                            if(cartStatus.order > 1){
                                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,onBackWhenAcceptedBookingCallback)
                            }

                        }else if(cartStatus == CartStatus.PENDING){
                            appSharedPref.storePendingBooking(cart.id)
                            expiryTimerJob?.cancel()
                            expiryTimerJob = timerTask()
                            expiryTimerJob?.start()
                            onBackWhenAcceptedBookingCallback.remove()
                            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,onCancelBackCallback)
                        }

                        if (cartStatus == CartStatus.PENDING) {



                            initMap(cart.pickUpLocation, cart.deliveryLocation)
//                            initMap(cart.deliveryLocation)
                            group_looking_for.visibility = View.VISIBLE
                            if(iv_vehicle_looking.drawable == null){
                                Glide.with(requireContext())
                                        .load(requireContext().getDrawable(cart.vehicleId.idToVehicleType().highResDrawable))
                                        .into(iv_vehicle_looking)
                            }

                            loadingTextJob?.cancel()
                            loadingTextJob = loadingTask()
                            loadingTextJob?.start()

                            btn_cancel.visibility = View.VISIBLE
                        }else if(cartStatus == CartStatus.CANCELLED){
                            loadingTextJob?.cancel()
                            if(findNavController().currentDestination?.id == R.id.customerRiderSearchFragment){
                                if(appSharedPref.getPendingBooking() != null){
                                    appSharedPref.clearPendingBooking()
                                    findNavController().popBackStack()
                                }else{
                                    CustomerRiderSearchFragmentDirections.actionCustomerRiderSearchFragmentToCustomerCartFragment().also {
                                        findNavController().navigate(it)
                                    }
                                }
                            }

                        }else if(cartStatus == CartStatus.DELIVERED){
                            Timber.d("Cart Delivered")
                            Snackbar.make(requireView(),"Booking Completed.",Snackbar.LENGTH_LONG).show()
                            cartViewModel.clearCartItemList()
                            cartViewModel.updateFromLocation(null)
                            cartViewModel.updateToLocation(null)
                            cartViewModel.setCartInfo(null)
                            if(findNavController().currentDestination?.id == R.id.customerRiderSearchFragment){
                                CustomerRiderSearchFragmentDirections.actionCustomerRiderSearchFragmentToHomeFragment().also {
                                    findNavController().navigate(it)
                                }
                            }

                        }
                        else{
                            loadingTextJob?.cancel()
                            btn_cancel.visibility = View.GONE
                        }

                        if(cartStatus != CartStatus.PENDING){
                            appSharedPref.clearPendingBooking()
                        }

                        when (cartType) {
                            CartType.CAR -> {
                                //
                            }
                            CartType.GROCERY, CartType.FOOD -> {
                                group_sub_total_and_delivery_fee.visibility = View.VISIBLE
                                label_summary.text = "Order Summary"
                                val itemsBasket = cart.initBasketForGrocery()

                                label_delivery_to.text = "Deliver to (${itemsBasket.distanceInKm} km)"

                                if(itemsBasket.etaText.isNullOrBlank().not() && itemsBasket.etaText != "-"){
                                    group_eta.visibility = View.VISIBLE
                                    tv_eta.text = itemsBasket.etaText
                                }else{
                                    group_eta.visibility = View.GONE
                                }

                                Timber.d(itemsBasket.toString())
                                tv_sub_total.text = itemsBasket.subTotal.toMoneyFormat()
                                tv_delivery_fee.text = itemsBasket.deliveryFee.toMoneyFormat()
                                tv_total.text = itemsBasket.grandTotal.toMoneyFormat()
                                adapter?.itemList = itemsBasket.items
                                adapter?.notifyDataSetChanged()
                            }
                            CartType.PABILI -> {
                                val pabiliBasket = cart.initBasketForPabili()
                                label_delivery_to.text = "Deliver to (${pabiliBasket.distanceInKm} km)"
                                Timber.d(pabiliBasket.toString())
                                if(pabiliBasket.etaText.isNullOrBlank().not() && pabiliBasket.etaText != "-"){
                                    group_eta.visibility = View.VISIBLE
                                    tv_eta.text = pabiliBasket.etaText
                                }else{
                                    group_eta.visibility = View.GONE
                                }
                                label_sub_total.visibility = View.GONE
                                tv_sub_total.visibility = View.GONE
                                label_delivery_fee.text = "Estimated Total"
                                tv_delivery_fee.text = pabiliBasket.estimateTotalAmount.toMoneyFormat()
                                label_total.text = "Pabili Fee"
                                tv_total.text = pabiliBasket.deliveryFee.toMoneyFormat()
                                adapter?.itemList = pabiliBasket.items
                                adapter?.notifyDataSetChanged()
                            }
                            CartType.DELIVERY -> {
                                label_summary.text = "Delivery Summary"
                                group_sub_total_and_delivery_fee.visibility = View.GONE
                                val deliveryBasket = cart.initBasketForDelivery()
                                label_delivery_to.text = "Deliver to (${deliveryBasket.distanceInKm} km)"
                                if(deliveryBasket.etaText.isNullOrBlank().not() && deliveryBasket.etaText != "-"){
                                    group_eta.visibility = View.VISIBLE
                                    tv_eta.text = deliveryBasket.etaText
                                }else{
                                    group_eta.visibility = View.GONE
                                }
                                Timber.d(deliveryBasket.toString())

                                val items = ArrayList<CartItem>().apply {
                                    add(deliveryBasket)
                                }
                                tv_total.text = deliveryBasket.grandTotal.toMoneyFormat()
                                adapter?.itemList = items
                                adapter?.notifyDataSetChanged()

                            }
                        }



                        if (cartStatus != CartStatus.INIT && cartStatus != CartStatus.PENDING) {

                            if (group_looking_for.visibility == View.VISIBLE) {
                                group_looking_for.visibility = View.GONE
                            }

                            if (cart.riderLat.isNotEmpty() && cart.riderLong.isNotEmpty()) {
                                updateMapLocation(cartId, cart.vehicleId.idToVehicleType(), cart.riderLat.toDouble(), cart.riderLong.toDouble())
                            }

//                            Snackbar.make(requireView(), "new status: ${cartStatus.label}", Snackbar.LENGTH_SHORT).show()
                            card_customer_request.visibility = View.VISIBLE
                            tv_status.text = cartStatus.label
                            tv_customer_name.text = cart.rider.fullName
                            Glide.with(requireContext())
                                    .load(cart.rider.profilePicture)
                                    .into(iv_icon)


/*                            when (cartStatus) {

                            }*/
                        }
                    }
                }

                is Result.Error -> {
                    Timber.e(result.metaResponse.message)
                    Toast.makeText(requireContext(),result.metaResponse.message,Toast.LENGTH_SHORT).show()
                }
                is Result.HttpError -> {
                    Timber.e(result.error.message)
                    Toast.makeText(requireContext(),result.error.message,Toast.LENGTH_SHORT).show()
                }
                }

            }
            cartViewModel.fetchCartInfoResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Result.Progress -> {
                    }
                    is Result.Success -> {
                        if (result.data != null) {
                            val cart = result.data.data
                            var updated = false
                            Timber.d(result.data.meta.message)

                            /**
                             *  cart status update priority
                             *  1. Firebase update
                             *  2. Timer-based update
                             */

                            // if firebase update
                            if (getCartInformationJob != null && getCartInformationJob!!.isActive) {
                                getCartInformationJob?.cancel() // cancel ongoing jobs
                                cartViewModel.setCartInfo(result)
                                updated = true
                            }

                            // start timer based job
                            getCartInformationJob.run {
                                this?.cancel()
                                getCartInformationJob = refresh(cart.id)
                                getCartInformationJob?.start()
                            }

                            // timer-based update || does not update cart if already updated by firebase
                            if (updated.not()) {
                                cartViewModel.setCartInfo(result)
                            }
                            if (result is Result.Success) {
                                val cartStatus = result.data.data.status.toCartStatus()
                                Timber.d("Update status to -> ${cartStatus} ${if(updated) "(Firebase update)" else "(Timer-based update)"}")
                            }
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.metaResponse.message)
                    }
                    is Result.HttpError -> {
                        Timber.d(result.error.message)
                    }
                }
            }
        }


    }
