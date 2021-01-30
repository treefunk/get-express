package com.myoptimind.getexpress.features.rider.selected_customer_request

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.getexpress.features.login.data.VehicleType
import com.myoptimind.getexpress.features.login.data.idToVehicleType
import com.myoptimind.getexpress.features.customer.cart.data.*
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.data.CartType
import com.myoptimind.getexpress.features.shared.data.idToCartType
import com.myoptimind.getexpress.features.shared.data.toCartStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_selected_customer_request.*
import kotlinx.coroutines.Job
import timber.log.Timber
import com.myoptimind.getexpress.R



private const val REQUEST_PERMISSION_COARSE_LOCATION = 898
@AndroidEntryPoint
class SelectedCustomerRequestFragment: TitleOnlyFragment() {

    private val viewModel by activityViewModels<SelectedCustomerRequestViewModel>()
    private var adapter: BasketAdapter? = null
    override fun getTitle() = ""
    private var mapFragment: SupportMapFragment? = null


    private var trackingJob: Job? = null
    private lateinit var locationCallback: LocationCallback
    private var riderMarker: Marker? = null

    private val args: SelectedCustomerRequestFragmentArgs by navArgs()

    private fun sendRiderLocationJob(cartId: String, vehicleType: VehicleType, latitude: Double, longitude: Double) = lifecycleScope.launchWhenCreated{
/*        for(i in 15 downTo 1){
            Timber.d("hashcode - ${this.hashCode()} refreshing in $i..")
            delay(1000)
        }*/
        Timber.d("hashcode - ${this.hashCode()} sending location updates..")
//        viewModel.sendRiderLocationUpdates(cartId, latitude, longitude)
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
            if(riderMarker == null){
                riderMarker = googleMap.addMarker(
                        markerOptions
                )
            }else{
                riderMarker?.position = latLong
            }

        }
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_selected_customer_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTransparentView()
        Timber.v("card id - ${args.cartId}")

        checkIfGpsEnabled()
        checkLocationPermissions()




        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment




        if(args.isAccepted.not()){
            if(args.historyOnly){
                group_history_views.visibility = View.GONE
                group_status_change.visibility = View.GONE
            }
            viewModel.initCartInfo(args.cartId)
        }else{
            viewModel.acceptCustomerRequest(args.cartId)
        }

        rv_orders.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        adapter = BasketAdapter(ArrayList())
        rv_orders.adapter = adapter

        initObservers()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTransparentView() {
        transparent_map_view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                else -> v.parent.requestDisallowInterceptTouchEvent(true)
            }
            false
        }
    }

    private fun checkLocationPermissions () {

        if (
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {

                requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ), REQUEST_PERMISSION_COARSE_LOCATION)
            return
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            if(!checkPermissionsAndroidP()){
                return
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    fun checkPermissionsAndroidP(): Boolean {
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                    arrayOf(Manifest.permission.FOREGROUND_SERVICE), REQUEST_PERMISSION_COARSE_LOCATION
            )
            return false
        }
        return true
    }

    private fun sendCommandToService(action: String,cartId: String) = RiderTrackingService.createIntent(requireContext(),action,cartId)


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_COARSE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(requireContext(), "Location Permission is granted.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Location Permission is not enabled.", Toast.LENGTH_LONG).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun initMap(pickupLocation: CartLocation, deliveryLocation: CartLocation) {

        // INIT MAP
        mapFragment?.getMapAsync { googleMap ->

            val pickupLatLong = LatLng(pickupLocation.latitude.toDouble(), pickupLocation.longitude.toDouble())
            val destinationLatLong = LatLng(deliveryLocation.latitude.toDouble(), deliveryLocation.longitude.toDouble())

            googleMap.apply {
                mapType = GoogleMap.MAP_TYPE_NORMAL
                uiSettings.apply {
                    isZoomControlsEnabled = true
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabledDuringRotateOrZoom = true
                }

                addMarker(
                        MarkerOptions()
                                .position(pickupLatLong)
                                .title("Pickup Location: ${pickupLocation.label}")
                )
//                cameraPosition = CameraUpdateFactory.newLatLngZoom(pickupLatLong, 15F)
                if(riderMarker == null){
                    moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLong, 15F))
                }
                addMarker(
                        MarkerOptions()
                                .position(destinationLatLong)
                                .title("Delivery Location: ${deliveryLocation.label}")
                )
            }

        }

    }

    private fun initObservers() {
        viewModel.cartInfoResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {
                    Timber.v("loading - ${result.isLoading}")
                }
                is Result.Success -> {
                    if (result.data != null) {
                        val cart = result.data.data
                        val cartType = cart.cartTypeId.idToCartType()
                        val cartStatus = cart.status.toCartStatus()



                        initMap(cart.pickUpLocation, cart.deliveryLocation)


                        updateCommonUILabels(cart, cartType)

                        if (cartStatus == CartStatus.PENDING) {
                            initPending(cart.id)
                        } else if (args.historyOnly.not() && (cartStatus != CartStatus.CANCELLED && cartStatus != CartStatus.INIT)) {
                            enableChangeableStatus(cart, cartStatus)
                            initLocationObserver(cart.id, cart.vehicleId)
                            sendCommandToService(RiderTrackingService.ACTION_START_OR_RESUME_SERVICE,cart.id)
                        } else { // HISTORY
                            Glide.with(requireContext())
                                    .load(cart.customer.customer.profilePicture)
                                    .into(iv_big_customer_image)
                        }

                        /** B A S K E T **/
                        val basketMap = cart.basket as Map<*, *>
                        adapter?.listType = cartType




                        Glide.with(requireContext())
                                .load(cartType.drawableId)
                                .into(iv_icon)

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



                        when (cartStatus) {
                            CartStatus.PENDING -> Unit
                            CartStatus.ACCEPTED -> Unit
                            CartStatus.GOT_ITEMS -> {

                            }
                            CartStatus.OTW -> Unit
                            CartStatus.ARRIVED -> Unit
                            CartStatus.DELIVERED -> {
                                sendCommandToService(RiderTrackingService.ACTION_STOP_SERVICE,cart.id)
                            }
                            CartStatus.CANCELLED, CartStatus.INIT -> {
                                Snackbar.make(
                                        requireView(), "This order has been cancelled. Please try another request.", Snackbar.LENGTH_LONG
                                ).setTextColor(ContextCompat.getColor(requireContext(), R.color.main_text_color)).show()
                                findNavController().navigate(R.id.action_selectedCustomerRequestFragment_to_riderDashboardFragment)
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

    private fun initLocationObserver(cartId: String, vehicleId: String) {
        RiderTrackingService.latLong.observe(viewLifecycleOwner){ latLong ->
            Timber.d("latlong ${latLong}")
            if (
                trackingJob == null || // if initial request
                trackingJob!!.isCompleted // if request is finished
            ) {
                trackingJob.run {
                    this?.cancel()
                    trackingJob = sendRiderLocationJob(
                            cartId,
                            vehicleId.idToVehicleType(),
                            latLong.latitude,
                            latLong.longitude
                    )
                    trackingJob?.start()
                }
            }

        }
    }



    private fun updateCommonUILabels(cart: Cart, cartType: CartType) {

        // customer details
        tv_customer_name.text = cart.customer.customer.fullName
        tv_customer_details.text = cart.customer.orderInfo.pickupLocationLabel
        Glide.with(requireContext())
                .load(cart.customer.orderInfo.serviceIcon)
                .into(iv_icon)
        Glide.with(requireContext())
                .load(cart.customer.customer.profilePicture)
                .into(iv_customer_image)

        label_get.text = cartType.label
        tv_from_name.text = cart.pickUpLocation.label
        if(args.historyOnly){
            setNewTitle(cart.pickUpLocation.label)
        }else{
            setNewTitle(cartType.label)
        }
        tv_from_location.text = cart.pickUpLocation.addressText
        tv_to_name.text = cart.deliveryLocation.label
        tv_to_location.text = cart.deliveryLocation.addressText
        if(cart.notes.isBlank()){
            label_additional_notes_to_rider.visibility = View.GONE
            tv_additional_notes_to_rider.text = cart.notes
        }
    }




    private fun initPending(cartId: String){
        if(group_status_change.visibility == View.VISIBLE){
            group_status_change.visibility = View.GONE
        }
        box_accept.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_200))
        box_accept.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_accept))
        box_accept.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Accept this request?")
                    .setNeutralButton("CANCEL") { _, _ ->
                        // Respond to neutral button press
                    }
                    .setPositiveButton("ACCEPT") { _, _ ->
                        viewModel.acceptCustomerRequest(cartId)
                    }
                    .show()
        }
    }

    private fun enableChangeableStatus(cart: Cart, cartStatus: CartStatus){
        if(group_status_change.visibility == View.GONE){
            group_status_change.visibility = View.VISIBLE
        }
        box_accept.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow_500))
        box_accept.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.call_icon))

        box_accept.setOnClickListener {
            if(cart.customer.customer.mobileNum.isNotBlank()){
                val phone = cart.customer.customer.mobileNum
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone))
                startActivity(intent)
            }
        }

        box_reject.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow_500))
        box_reject.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.message_icon))

        btn_got_items.initStatusButton(cart, cartStatus, CartStatus.GOT_ITEMS)
        btn_on_the_way.initStatusButton(cart, cartStatus, CartStatus.OTW)
        btn_arrived.initStatusButton(cart, cartStatus, CartStatus.ARRIVED)
        btn_delivered.initStatusButton(cart, cartStatus, CartStatus.DELIVERED)

    }

    private fun changeStatus(cart: Cart, currentStatus: CartStatus, newStatus: CartStatus){
        MaterialAlertDialogBuilder(requireContext())
                .setMessage("Change status from [${currentStatus.label}] to [${newStatus.label}]?")
                .setNeutralButton("NO") { dialog, which ->
                    // Respond to neutral button press
                }
                .setPositiveButton("YES") { dialog, which ->
                    if(newStatus != CartStatus.DELIVERED){
                        viewModel.updateStatusCustomerRequest(cart.id, newStatus)
                    }else{
                        val grandTotal = cart.customer.orderInfo.grandTotal.toString()
                        Timber.d("Completing booking for \ncart id:$cart.id\ntotal price:$grandTotal\npayment status:verified")
                        viewModel.completeBooking(cart.id,grandTotal,"verified")
                    }
                }
                .show()
    }

    private fun Button.initStatusButton(cart: Cart, currentStatus: CartStatus, buttonStatus: CartStatus) {
        if(currentStatus != buttonStatus){
            this.isEnabled = true
            this.setOnClickListener {
                changeStatus(cart, currentStatus, buttonStatus)
            }
            if(currentStatus.order < buttonStatus.order){
                this.background.setTint(ContextCompat.getColor(requireContext(), R.color.status_button_grey))
            }else if (currentStatus.order >= buttonStatus.order){
                this.background.setTint(ContextCompat.getColor(requireContext(), R.color.yellow_700))
            }
        }else{
            this.isEnabled = false
            this.background.setTint(ContextCompat.getColor(requireContext(), R.color.yellow_500))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().stopService(Intent(requireContext(), RiderTrackingService::class.java).also {
            requireContext().startService(it)
        })

    }

    private fun checkIfGpsEnabled(){
        if(isLocationEnabled(requireContext()).not()){
            Snackbar.make(requireView(), "Please enable your GPS.", Snackbar.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_selectedCustomerRequestFragment_to_riderDashboardFragment)
            return
        }
    }
}

