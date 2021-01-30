package com.myoptimind.getexpress.features.shared.data

import android.os.Parcelable
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.cart.data.CartStatus
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class CartType(val id: String,val title: String,val label: String,val drawableId: Int): Parcelable {
    CAR("1","car","Get Car", R.drawable.ic_get_car_ns),
    GROCERY("2","grocery","Get Grocery",R.drawable.ic_get_grocery_ns),
    PABILI("3","pabili","Get Pabili",R.drawable.ic_get_pabili_ns),
    DELIVERY("4","delivery","Get Delivery",R.drawable.ic_get_delivery_ns),
    FOOD("5","food","Get Food",R.drawable.ic_get_food_ns)
}

fun String.idToCartType(): CartType {
    return when(this){
        "1" -> CartType.CAR
        "2" -> CartType.GROCERY
        "3" -> CartType.PABILI
        "4" -> CartType.DELIVERY
        "5" -> CartType.FOOD
        else -> throw Exception("Invalid Cart Type ID!")
    }
}

fun String.toCartStatus(): CartStatus {
    return when(this){
        "pending" -> CartStatus.PENDING
        "accepted" -> CartStatus.ACCEPTED
        "got_items" -> CartStatus.GOT_ITEMS
        "otw" -> CartStatus.OTW
        "arrived_at_destination" -> CartStatus.ARRIVED
        "completed" -> CartStatus.DELIVERED
        "cancelled" -> CartStatus.CANCELLED
        "init" -> CartStatus.INIT
        else -> throw Exception("Invalid Cart Status!")
    }
}