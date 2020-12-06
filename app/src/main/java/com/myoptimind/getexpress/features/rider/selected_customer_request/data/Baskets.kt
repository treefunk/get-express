package com.myoptimind.getexpress.features.rider.selected_customer_request.data

import com.google.gson.annotations.SerializedName


// FOR FOOD / GROCERY SERVICE
data class BasketForFoodGrocery(
    @SerializedName("sub_total")
    var subTotal: String = "",
    @SerializedName("grand_total")
    var grandTotal: String = "",
    @SerializedName("total_items")
    var totalItems: String = "",
    @SerializedName("delivery_fee")
    var deliveryFee: String = "",
    var items: List<ItemInFoodGrocery> = ArrayList()
)


data class ItemInFoodGrocery(

    @SerializedName("cart_item_id")
    var cartItemId: String = "",

    @SerializedName("cart_id")
    var cartId: String = "",

    @SerializedName("product_id")
    var productId: String = "",

    @SerializedName("product_name")
    var productName: String = "",

    var image: String = "",

    var category: String = "",

    var description: String = "",

    @SerializedName("base_price")
    var basePrice: String = "",

    var quantity: String = "",

    @SerializedName("computed_price")
    var computedPrice: String = "",

    var notes: String = ""

): CartItem

// DELIVERY
data class BasketForDelivery(
        var category: String = "",
        var notes: String = "",
        var price: String = "",
        @SerializedName("grand_total")
        var grandTotal: String = ""
): CartItem

data class BasketForPabili(
        var estimateTotalAmount: String = "",
        var items: List<ItemInPabili> = ArrayList()
)

data class ItemInPabili(
        @SerializedName("item_name")
        var itemName: String = "",
        var quantity: String = ""
): CartItem