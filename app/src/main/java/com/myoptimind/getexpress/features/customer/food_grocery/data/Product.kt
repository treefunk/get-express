package com.myoptimind.getexpress.features.customer.food_grocery.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
        val id: String,
        @SerializedName("partner_id")
        val partnerId: String,
        @SerializedName("in_stock")
        val inStock: String,
        @SerializedName("product_name")
        val productName: String,
        val description: String,
        var quantity: String = "0",
        @SerializedName("base_price")
        val basePrice: String,
        val image: String,
        @SerializedName("addon_children_ids")
        val addonIds: ArrayList<String>,
        val category: String,
        val addons: HashMap<String,List<Product>>,
//        val addons: List<Product>,
        // if existing product exists
        var cartItemId: String? = null,
        var cartQuantity: String? = null,
): Parcelable