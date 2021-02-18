package com.myoptimind.get_express.features.customer.cart.data

import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.login.data.RiderInCart
import com.myoptimind.get_express.features.rider.customer_requests_list.data.CustomerRequest
import com.myoptimind.get_express.features.shared.api.BaseRemoteEntity
import com.myoptimind.get_express.features.shared.api.MetaResponse

open class Cart(
        val id: String = "",

        @SerializedName("cart_type_id")
        val cartTypeId: String = "",

        @SerializedName("partner_id")
        val partnerId: String = "",

        val customer: CustomerRequest,

        val rider: RiderInCart,

        @SerializedName("vehicle_id")
        val vehicleId: String = "",

        val status: String = "",

        val payload: String = "",

        @SerializedName("rider_id")
        val riderId: String = "",

        val notes: String = "",

        @SerializedName("pickup_location")
        val pickUpLocation: CartLocation = CartLocation(),

        @SerializedName("delivery_location")
        val deliveryLocation: CartLocation = CartLocation(),

        @SerializedName("payment_method")
        val paymentMethod: String = "",

        @SerializedName("transaction_id")
        val transactionId: String = "",

        @SerializedName("payment_status")
        val paymentStatus: String = "",

        @SerializedName("total_price")
        val totalPrice: String = "",

        @SerializedName("service_type")
        val serviceType: String = "",

        @SerializedName("rider_current_location_latitude")
        val riderLat: String = "",
        @SerializedName("rider_current_location_longitude")
        val riderLong: String = "",

        val basket: Any = Any()

) : BaseRemoteEntity() {
        // manually parsing data
        fun initBasketForGrocery(): BasketForFoodGrocery {
                val basketMap = this.basket as Map<*,*>
                return BasketForFoodGrocery().apply {
                        for (entry in basketMap) {
                                entry.getDataFor<Double>("sub_total"){ subTotal = it.toString() }
                                entry.getDataFor<Double>("grand_total"){ grandTotal = it.toString() }
                                entry.getDataFor<Int>("total_items"){ totalItems = it.toString() }
                                entry.getDataFor<Double>("delivery_fee"){ deliveryFee = it.toString() }
                                entry.getDataFor<Double>("distance_in_km"){ distanceInKm = "%.2f".format(it) }
                                entry.getDataFor<ArrayList<*>>("items") { itemList ->
                                        items = initItemInGroceryBasket(itemList)
                                }
                                if(basketMap.containsKey("eta_text")){
                                        entry.getDataFor<String>("eta_text"){ etaText = it }
                                }
                        }
                }
        }

        fun initBasketForFood(): BasketForFoodGrocery {
                val basketMap = this.basket as Map<*,*>
                return BasketForFoodGrocery().apply {
                        for (entry in basketMap) {
                                entry.getDataFor<Double>("sub_total"){ subTotal = it.toString() }
                                entry.getDataFor<Double>("grand_total"){ grandTotal = it.toString() }
                                entry.getDataFor<Int>("total_items"){ totalItems = it.toString() }
                                entry.getDataFor<Double>("delivery_fee"){ deliveryFee = it.toString() }
                                entry.getDataFor<Double>("distance_in_km"){ distanceInKm = "%.2f".format(it) }
                                entry.getDataFor<ArrayList<*>>("items") { itemList ->
                                        items = initItemInFoodBasket(itemList)
                                }
                                if(basketMap.containsKey("eta_text")){
                                        entry.getDataFor<String>("eta_text"){ etaText = it }
                                }
                        }
                }
        }


        fun initBasketForPabili(): BasketForPabili {
                val basketMap = this.basket as Map<*,*>
                return BasketForPabili().apply {
                        for (entry in basketMap) {
                                entry.getDataFor<Double>("estimate_total_amount"){ estimateTotalAmount = it.toString() }
                                entry.getDataFor<Double>("delivery_fee"){ deliveryFee = it.toString() }
                                entry.getDataFor<Double>("_estimate_total_amount_without_delivery_fees"){ estimateTotalWithoutDeliveryFee = it.toString() }
                                entry.getDataFor<ArrayList<*>>("items") { itemList ->
                                        items = initItemInPabili(itemList)
                                }
                                entry.getDataFor<Double>("distance_in_km"){ distanceInKm = "%.2f".format(it) }

                                if(basketMap.containsKey("eta_text")){
                                        entry.getDataFor<String>("eta_text"){ etaText = it }
                                }
                        }
                }
        }

        private fun initItemInPabili(itemList: ArrayList<*>): List<ItemInPabili> {
                return itemList.map { item ->
                        item.let { v ->
                                ItemInPabili().apply {
                                        for(entry in v as Map<*, *>){
                                                entry.getDataFor<String>("item_name"){ itemName = it }
                                                entry.getDataFor<String>("quantity"){ quantity = it }
                                        }
                                }
                        }
                }
        }

        private fun initItemInGroceryBasket(items: ArrayList<*>): List<ItemInFoodGrocery> {
                return items.map { item ->
                        item.let { v ->
                                ItemInFoodGrocery().apply {
                                        for(entry in v as Map<*, *>){
                                                entry.getDataFor<String>("cart_item_id"){ cartItemId = it }
                                                entry.getDataFor<String>("cart_id"){ cartId = it }
                                                entry.getDataFor<String>("product_id"){ productId = it }
                                                entry.getDataFor<String>("product_name"){ productName = it }
                                                entry.getDataFor<String>("image"){ image = it }
                                                entry.getDataFor<String>("category"){ category = it }
                                                entry.getDataFor<String>("in_stock"){ inStock = it == "1" }
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

        private fun initItemInFoodBasket(items: ArrayList<*>): List<ItemInFoodGrocery> {
                return items.map { item ->
                        item.let { v ->
                                ItemInFoodGrocery().apply {
                                        for(entry in v as Map<*, *>){
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

                                                entry.getDataFor<ArrayList<*>>("addons"){ list ->
                                                        addons = list.map { any ->
                                                                any.let { ao ->
                                                                        AddOn().apply {
                                                                                for (entry in ao as Map<*, *>) {
                                                                                        entry.getDataFor<String>("id") { id = it }
                                                                                        entry.getDataFor<String>("product_name") { productName = it }
                                                                                        entry.getDataFor<Double>("base_price") { basePrice = it.toString() }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }

        fun initBasketForDelivery(): BasketForDelivery {
                val basketMap = this.basket as Map<*,*>

                return BasketForDelivery().apply {
                        for(entry in basketMap){
                                entry.getDataFor<String>("category"){ category = it }
                                entry.getDataFor<String>("notes"){ notes = it }
                                entry.getDataFor<Int>("price"){ price = it.toString() }
                                entry.getDataFor<Int>("grand_total"){ grandTotal = it.toString() }
                                entry.getDataFor<Double>("distance_in_km"){ distanceInKm = "%.2f".format(it) }
                                if(basketMap.containsKey("eta_text")){
                                        entry.getDataFor<String>("eta_text"){ etaText = it }
                                }
                        }
                }
        }

        /**
         *
         * FOR EXTRACTING DYNAMIC BASKET KEY
         *  tried using gson for parsing but it didn't work :( -jhondee
         */
        private fun <T : Any> Map.Entry<Any?, Any?>.getDataFor(keyName: String, value: (type: T) -> Unit) {
                if(this.key is String && this.key == keyName){ // cast the key to string and check if keyname is equal to the Map.key
                        @Suppress("UNCHECKED_CAST")
                        value(this.value as T) // if equal, cast Map.value with T
                }
        }
}

interface CartItem


data class GetCartInfoResponse(
        val data: Cart,
        val meta: MetaResponse
)