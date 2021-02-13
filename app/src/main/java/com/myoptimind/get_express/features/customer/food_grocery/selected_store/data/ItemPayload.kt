package com.myoptimind.get_express.features.customer.food_grocery.selected_store.data

import android.os.Parcelable
import com.myoptimind.get_express.features.customer.cart.data.AddOn
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemPayload(
        val productName: String,
        val productId: String,
        val quantity: String,
        val notes: String,
        var addons: List<AddOn>? = null,
        var cartItemId: String? = null
): Parcelable
