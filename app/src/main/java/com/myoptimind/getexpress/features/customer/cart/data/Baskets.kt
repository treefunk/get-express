package com.myoptimind.getexpress.features.customer.cart.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.myoptimind.getexpress.features.customer.food_grocery.selected_store.data.ItemPayload
import kotlinx.android.parcel.Parcelize


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

    var notes: String = "",


    var addons: List<AddOn> = listOf()

): CartItem {
    fun toItemPayload(): ItemPayload {
        return ItemPayload(
                productName,
                productId,
                quantity,
                notes,
                addons,
                cartItemId
        )
    }
}

@Parcelize
data class AddOn(
        var id: String = "",
        @SerializedName("product_name")
        var productName: String = "",
        @SerializedName("base_price")
        var basePrice: String = ""
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if(other is AddOn){
            return this.id == other.id
        }
        return super.equals(other)
    }
}

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
        var deliveryFee: String = "",
        var estimateTotalWithoutDeliveryFee: String = "",
        var items: List<ItemInPabili> = ArrayList()
)

data class ItemInPabili(
        @SerializedName("item_name")
        var itemName: String = "",
        var quantity: String = ""
): CartItem