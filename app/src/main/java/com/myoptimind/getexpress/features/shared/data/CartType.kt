package com.myoptimind.getexpress.features.shared.data

import android.graphics.drawable.Drawable
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.rider.selected_customer_request.data.RiderCartStatus

enum class CartType(val id: String,val title: String,val label: String,val drawableId: Int) {
    CAR("1","car","Get Car", R.drawable.ic_get_car),
    GROCERY("2","grocery","Get Grocery",R.drawable.ic_get_grocery),
    PABILI("3","pabili","Get Pabili",R.drawable.ic_get_pabili),
    DELIVERY("4","delivery","Get Delivery",R.drawable.ic_get_delivery),
    FOOD("5","food","Get Food",R.drawable.ic_get_food)
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

fun String.toRiderCartStatus(): RiderCartStatus {
    return when(this){
        "pending" -> RiderCartStatus.PENDING
        "accepted" -> RiderCartStatus.ACCEPTED
        "got_items" -> RiderCartStatus.GOT_ITEMS
        "otw" -> RiderCartStatus.OTW
        "arrived_at_destination" -> RiderCartStatus.ARRIVED
        "completed" -> RiderCartStatus.DELIVERED
        else -> throw Exception("Invalid Cart Status!")
    }
}