package com.myoptimind.get_express.features.login.data

import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.shared.api.BaseRemoteEntity

data class VehicleListItem(
    val id: String,
    val name: String,
    val image: String
): BaseRemoteEntity()

enum class VehicleType(val id: String,val title: String,val label: String,val drawableId: Int, val highResDrawable: Int) {
    WALK("1","walk","Walk", R.drawable.ic_vehicle_walk,R.drawable.ic_walk_big),
    BICYCLE("2","bicycle","Bicycle", R.drawable.ic_vehicle_bicycle,R.drawable.ic_bicycle_big),
    MOTORCYCLE("3","motorcycle","Motorcycle", R.drawable.ic_vehicle_motorcycle,R.drawable.ic_motorcycle_big),
    CAR_2_SEATER("4","car_2_seater","Car (2-seater)", R.drawable.ic_vehicle_2seater,R.drawable.ic_car2_big),
    MPV("5","mpv","MPV", R.drawable.ic_vehicle_mpv,R.drawable.ic_mpv_big),
    TRUCK("6","truck","Truck", R.drawable.ic_vehicle_truck,R.drawable.ic_truck_big),
    CAR_4_SEATER("7","car_4_seater","Car (4-seater)", R.drawable.ic_vehicle_4seater,R.drawable.ic_car4_big),
    SUV("8","suv","SUV", R.drawable.ic_vehicle_truck,R.drawable.ic_truck_big)
}

fun String.idToVehicleType(): VehicleType {
    return when(this){
        "1" -> VehicleType.WALK
        "2" -> VehicleType.BICYCLE
        "3" -> VehicleType.MOTORCYCLE
        "4" -> VehicleType.CAR_2_SEATER
        "5" -> VehicleType.MPV
        "6" -> VehicleType.TRUCK
        "7" -> VehicleType.CAR_4_SEATER
        "8" -> VehicleType.SUV
        else -> throw Exception("Invalid Vehicle Type ID!")
    }
}
