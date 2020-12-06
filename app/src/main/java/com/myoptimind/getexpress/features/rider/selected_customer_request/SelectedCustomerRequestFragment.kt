package com.myoptimind.getexpress.features.rider.selected_customer_request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.rider.selected_customer_request.data.*
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.data.CartType
import com.myoptimind.getexpress.features.shared.data.idToCartType
import com.myoptimind.getexpress.features.shared.data.toRiderCartStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_selected_customer_request.*
import timber.log.Timber


@AndroidEntryPoint
class SelectedCustomerRequestFragment: TitleOnlyFragment() {

    private val viewModel by activityViewModels<SelectedCustomerRequestViewModel>()
    private var adapter: BasketAdapter? = null
    override fun getTitle() = ""

    private val args: SelectedCustomerRequestFragmentArgs by navArgs()

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
        Timber.v("card id - ${args.cartId}")
        if(args.isAccepted.not()){
            viewModel.initCartInfo(args.cartId)
        }else{
            viewModel.acceptCustomerRequest(args.cartId)
        }

        rv_orders.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        adapter = BasketAdapter(ArrayList())
        rv_orders.adapter = adapter

        initObservers()
        initMap()

    }

    private fun initMap() {

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
                        val cartStatus = cart.status.toRiderCartStatus()


                        // INIT MAP
                        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                        mapFragment.getMapAsync { googleMap ->
                            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

                            val pickupLatLong = LatLng(cart.pickUpLocation.latitude.toDouble(), cart.pickUpLocation.longitude.toDouble())
                            val destinationLatLong = LatLng(cart.deliveryLocation.latitude.toDouble(),cart.deliveryLocation.longitude.toDouble())
                            googleMap.addMarker(
                                    MarkerOptions()
                                            .position(pickupLatLong)
                                            .title("Pickup Location: ${cart.pickUpLocation.label}")
                            )
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLong,17F))
                            googleMap.addMarker(
                                    MarkerOptions()
                                            .position(destinationLatLong)
                                            .title("Delivery Location: ${cart.deliveryLocation.label}")
                            )
                        }


                        updateCommonUILabels(cart,cartType)

                        if(cartStatus == RiderCartStatus.PENDING){
                            initPending(cart.id)
                        }else{
                            enableChangeableStatus(cart.id,cartStatus)
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
                                val itemsBasket = initBasketForFoodGrocery(basketMap)

                                Timber.d(itemsBasket.toString())

                                tv_sub_total.text = itemsBasket.subTotal
                                tv_delivery_fee.text = itemsBasket.deliveryFee
                                tv_total.text = itemsBasket.grandTotal
                                adapter?.itemList = itemsBasket.items
                                adapter?.notifyDataSetChanged()
                            }
                            CartType.PABILI -> {
                                val pabiliBasket = initBasketForPabili(basketMap)
                                Timber.d(pabiliBasket.toString())
                                group_sub_total_and_delivery_fee.visibility = View.GONE
                                tv_total.text = pabiliBasket.estimateTotalAmount
                                adapter?.itemList = pabiliBasket.items
                                adapter?.notifyDataSetChanged()
                            }
                            CartType.DELIVERY -> {
                                label_summary.text = "Delivery Summary"
                                group_sub_total_and_delivery_fee.visibility = View.GONE
                                val deliveryBasket = initBasketForDelivery(basketMap)
                                Timber.d(deliveryBasket.toString())

                                val items = ArrayList<CartItem>().apply {
                                    add(deliveryBasket)
                                }
                                tv_total.text = deliveryBasket.grandTotal
                                adapter?.itemList = items
                                adapter?.notifyDataSetChanged()

                            }
                        }



                        when(cartStatus){
                            RiderCartStatus.PENDING -> Unit
                            RiderCartStatus.ACCEPTED -> Unit
                            RiderCartStatus.GOT_ITEMS -> {

                            }
                            RiderCartStatus.OTW -> Unit
                            RiderCartStatus.ARRIVED -> Unit
                            RiderCartStatus.DELIVERED -> Unit
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

    private fun initBasketForPabili(basketMap: Map<*, *>): BasketForPabili {
        return BasketForPabili().apply {
            for (entry in basketMap) {
                entry.getDataFor<String>("estimate_total_amount"){ estimateTotalAmount = it.toString() }
                entry.getDataFor<ArrayList<*>>("items") { itemList ->
                    items = initItemInPabili(itemList)
                }
            }
        }
    }

    private fun initItemInPabili(itemList: ArrayList<*>): List<ItemInPabili> {
        return itemList.map { item ->
            item.let { v ->
                ItemInPabili().apply {
                    for(entry in v as Map<*,*>){
                        entry.getDataFor<String>("item_name"){ itemName = it }
                        entry.getDataFor<String>("quantity"){ quantity = it }
                    }
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
        tv_from_location.text = cart.pickUpLocation.addressText
        tv_to_name.text = cart.deliveryLocation.label
        tv_to_location.text = cart.deliveryLocation.addressText
        tv_additional_notes_to_rider.text = cart.notes
    }

    // manually parsing data
    private fun initBasketForFoodGrocery(basketMap: Map<*, *>): BasketForFoodGrocery {
        return BasketForFoodGrocery().apply {
            for (entry in basketMap) {
                entry.getDataFor<Double>("sub_total"){ subTotal = it.toString() }
                entry.getDataFor<Double>("grand_total"){ grandTotal = it.toString() }
                entry.getDataFor<Double>("total_items"){ totalItems = it.toString() }
                entry.getDataFor<Double>("delivery_fee"){ deliveryFee = it.toString() }
                entry.getDataFor<ArrayList<*>>("items") { itemList ->
                    items = initItemInFoodGroceryBasket(itemList)
                }
            }
        }
    }

    private fun initItemInFoodGroceryBasket(items: ArrayList<*>): List<ItemInFoodGrocery> {
        return items.map { item ->
            item.let { v ->
                ItemInFoodGrocery().apply {
                    for(entry in v as Map<*,*>){
                        entry.getDataFor<String>("cart_item_id"){ cartItemId = it }
                        entry.getDataFor<String>("cart_id"){ cartId = it }
                        entry.getDataFor<String>("product_id"){ productId = it }
                        entry.getDataFor<String>("product_name"){ productName = it }
                        entry.getDataFor<String>("image"){ image = it }
                        entry.getDataFor<String>("category"){ category = it }
                        entry.getDataFor<String>("description"){ description = it }
                        entry.getDataFor<Int>("base_price"){ basePrice = it.toString() }
                        entry.getDataFor<Int>("quantity"){ quantity = it.toString() }
                        entry.getDataFor<Int>("computed_price"){ computedPrice = it.toString() }
                        entry.getDataFor<String>("notes"){ notes = it }
                    }
                }
            }
        }
    }

    private fun initBasketForDelivery(basketMap: Map<*, *>): BasketForDelivery {
        return BasketForDelivery().apply {
            for(entry in basketMap){
                entry.getDataFor<String>("category"){ category = it }
                entry.getDataFor<String>("notes"){ notes = it }
                entry.getDataFor<Int>("price"){ price = it.toString() }
                entry.getDataFor<Int>("grand_total"){ grandTotal = it.toString() }
            }
        }
    }

    /**
     *
     * FOR EXTRACTING DYNAMIC BASKET KEY
     *  tried using gson for parsing but it didn't work :( -jhondee
     */
    private fun <T: Any> Map.Entry<Any?,Any?>.getDataFor(keyName: String, value: (type:T) -> Unit) {
        if(this.key is String && this.key == keyName){ // cast the key to string and check if keyname is equal to the Map.key
            @Suppress("UNCHECKED_CAST")
            value(this.value as T) // if equal, cast Map.value with T
        }
    }

    private fun initPending(cartId: String){
        if(group_status_change.visibility == View.VISIBLE){
            group_status_change.visibility = View.GONE
        }
        box_accept.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_200))
        box_accept.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_accept))
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

    private fun enableChangeableStatus(cartId: String, cartStatus: RiderCartStatus){
        if(group_status_change.visibility == View.GONE){
            group_status_change.visibility = View.VISIBLE
        }
        box_accept.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        box_accept.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.call_icon))

        box_reject.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        box_reject.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.message_icon))

        btn_got_items.initStatusButton(cartId,cartStatus,RiderCartStatus.GOT_ITEMS)
        btn_on_the_way.initStatusButton(cartId,cartStatus,RiderCartStatus.OTW)
        btn_arrived.initStatusButton(cartId,cartStatus,RiderCartStatus.ARRIVED)
        btn_delivered.initStatusButton(cartId,cartStatus,RiderCartStatus.DELIVERED)

    }

    private fun changeStatus(cartId: String, currentStatus: RiderCartStatus, newStatus: RiderCartStatus){
        MaterialAlertDialogBuilder(requireContext())
                .setMessage("Change status from [${currentStatus.label}] to [${newStatus.label}]?")
                .setNeutralButton("NO") { dialog, which ->
                    // Respond to neutral button press
                }
                .setPositiveButton("YES") { dialog, which ->
                    viewModel.updateStatusCustomerRequest(cartId,newStatus)
                }
                .show()
    }

    private fun Button.initStatusButton(cartId: String, currentStatus: RiderCartStatus, buttonStatus: RiderCartStatus) {
        if(currentStatus != buttonStatus){
            this.isEnabled = true
            this.setOnClickListener {
                changeStatus(cartId,currentStatus,buttonStatus)
            }
            if(currentStatus.order < buttonStatus.order){
                this.background.setTint(ContextCompat.getColor(requireContext(), R.color.status_button_grey))
            }else if (currentStatus.order >= buttonStatus.order){
                this.background.setTint(ContextCompat.getColor(requireContext(), R.color.color_yellow_300))
            }
        }else{
            this.isEnabled = false
            this.background.setTint(ContextCompat.getColor(requireContext(), R.color.purple_500))
        }
    }
}

