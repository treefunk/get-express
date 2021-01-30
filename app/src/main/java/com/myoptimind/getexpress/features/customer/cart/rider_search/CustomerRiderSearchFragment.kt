package com.myoptimind.getexpress.features.customer.cart.rider_search

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.cart.CartViewModel
import com.myoptimind.getexpress.features.customer.cart.rider_search.data.TrackingPayload
import com.myoptimind.getexpress.features.login.data.VehicleType
import com.myoptimind.getexpress.features.login.data.idToVehicleType
import com.myoptimind.getexpress.features.customer.cart.data.BasketAdapter
import com.myoptimind.getexpress.features.customer.cart.data.CartItem
import com.myoptimind.getexpress.features.customer.cart.data.CartLocation
import com.myoptimind.getexpress.features.customer.cart.data.CartStatus
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.data.CartType
import com.myoptimind.getexpress.features.shared.data.idToCartType
import com.myoptimind.getexpress.features.shared.data.toCartStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_customer_rider_search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber
import java.lang.StringBuilder

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


    private var getCartInformationJob: Job? = null
    private var loadingTextJob: Job? = null

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
        cartViewModel.getCartInformation(cartId)
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
                }
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

    private fun initLocationObserver(cartId: String) {
        cartViewModel.cart.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {
                }
                is Result.Success -> {
                    if (result.data != null) {
                        val cart = result.data.data

                        cartViewModel.cartId = cart.id

                        tv_to_location.text = cart.deliveryLocation.label + "\n" + cart.deliveryLocation.addressText

                        val cartType = cart.cartTypeId.idToCartType()
                        val cartStatus = cart.status.toCartStatus()
                        tv_customer_details.text = "${cart.rider.vehicleModel} | ${cart.rider.plateNumber}"

                        /** B A S K E T **/
                        val basketMap = cart.basket as Map<*, *>
                        adapter?.listType = cartType


                        Glide.with(requireContext())
                                .load(cartType.drawableId)
                                .into(iv_icon)

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
                            CustomerRiderSearchFragmentDirections.actionCustomerRiderSearchFragmentToCustomerCartFragment(
                                    cart.pickUpLocation.toPlace(),cart.deliveryLocation.toPlace()
                            ).also {
                                findNavController().navigate(it)
                            }
                        }else{
                            loadingTextJob?.cancel()
                            btn_cancel.visibility = View.GONE
                        }

                        when (cartType) {
                            CartType.CAR -> {
                                //
                            }
                            CartType.GROCERY, CartType.FOOD -> {
                                group_sub_total_and_delivery_fee.visibility = View.VISIBLE
                                label_summary.text = "Order Summary"
                                val itemsBasket = cart.initBasketForGrocery()

                                Timber.d(itemsBasket.toString())
                                tv_sub_total.text = itemsBasket.subTotal
                                tv_delivery_fee.text = itemsBasket.deliveryFee
                                tv_total.text = itemsBasket.grandTotal
                                adapter?.itemList = itemsBasket.items
                                adapter?.notifyDataSetChanged()
                            }
                            CartType.PABILI -> {
                                val pabiliBasket = cart.initBasketForPabili()
                                Timber.d(pabiliBasket.toString())
                                group_sub_total_and_delivery_fee.visibility = View.GONE
/*                                label_sub_total.visibility = View.GONE
                                label_delivery_fee.text = "Pabili Fee"
                                tv_delivery_fee.text = pabiliBasket.estimateTotalAmount */
                                label_total.text = "Estimated Total"
                                tv_total.text = pabiliBasket.estimateTotalAmount
                                adapter?.itemList = pabiliBasket.items
                                adapter?.notifyDataSetChanged()
                            }
                            CartType.DELIVERY -> {
                                label_summary.text = "Delivery Summary"
                                group_sub_total_and_delivery_fee.visibility = View.GONE
                                val deliveryBasket = cart.initBasketForDelivery()
                                Timber.d(deliveryBasket.toString())

                                val items = ArrayList<CartItem>().apply {
                                    add(deliveryBasket)
                                }
                                tv_total.text = deliveryBasket.grandTotal
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
                }
                is Result.HttpError -> {
                    Timber.d(result.error.message)
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