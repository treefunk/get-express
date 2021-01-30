package com.myoptimind.getexpress.features.edit_profile

import android.location.Location
import com.google.android.libraries.places.api.model.Place
import com.myoptimind.getexpress.features.edit_profile.api.ProfileService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.Part
import javax.inject.Inject


class ProfileRepository @Inject constructor(
    val profileService: ProfileService,
    val appSharedPref: AppSharedPref
) {

    /**
     * RIDER
     */
    fun getRiderProfile() = flow {
        val response = profileService.getRiderProfile(appSharedPref.getUserId())
        emit(Result.Success(response))
    }.applyDefaultEffects()

    fun addVehicle(
        vehicleTypeId: String,
        vehicleModel: String,
        plateNumber: String
    ) = flow {
        val response = profileService.addVehicle(
            appSharedPref.getUserId(),
            vehicleTypeId,
            vehicleModel,
            plateNumber
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false, true)


    fun changeActiveVehicle(
        vehicleId: String
    ) = flow {
        val response = profileService.changeActiveVehicle(
            appSharedPref.getUserId(),
            vehicleId
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false, true)

    /**
     * CUSTOMER
     */
    fun getCustomerProfile() = flow {
        val response = profileService.getCustomerProfile(appSharedPref.getUserId())
        emit(Result.Success(response))
    }.applyDefaultEffects()

    fun updateProfileInformation(
        fullname: RequestBody?,
        email: RequestBody?,
        mobileNum: RequestBody?,
        birthdate: RequestBody?,
        location: RequestBody?,
        password: RequestBody?,
        profilePicture: MultipartBody.Part?,
    ) = flow {

        val response = profileService.updateProfile(
            appSharedPref.getUserType().label + "s", // api accepts plural form of type
            appSharedPref.getUserId(),
            fullname, email, mobileNum, birthdate, location, password, profilePicture
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)

    fun checkLocation(
            selectedPlace: Place,
    ) = flow<Result<Place>> {
        val maxDistanceLimit = profileService.getStoresRadius().data.storeRadius.toDouble() * 1000
        val results = FloatArray(1)

        Location.distanceBetween(
                14.651763,121.049318,
                selectedPlace.latLng!!.latitude,selectedPlace.latLng!!.longitude,results)
        val distance = results[0]
        if(distance > maxDistanceLimit){
            emit(Result.HttpError(AreaLaunchingSoonException()))
        }else{
            emit(Result.Success(selectedPlace))
        }
    }.applyDefaultEffects(false,true)

    fun addAddress(
            label: String,
            fullAddress: String,
            latitude: String,
            longitude: String,
            saveAddress: Boolean = false
    ) = flow {
        val maxDistanceLimit = profileService.getStoresRadius().data.storeRadius.toDouble() * 1000
        val results = FloatArray(1)

        Location.distanceBetween(
                14.651763,121.049318,
                latitude.toDouble(),longitude.toDouble(),results)
        val distance = results[0]
        if(distance > maxDistanceLimit){
            emit(Result.HttpError(AreaLaunchingSoonException()))
        }else{
            val response = profileService.addAddress(
                    appSharedPref.getUserId(),
                    label,
                    fullAddress,
                    latitude,
                    longitude
            )
            emit(Result.Success(response))
        }
    }.applyDefaultEffects(false, true)

    class AreaLaunchingSoonException(val title:String = "Launching Soon"): Exception(){
        override val message: String
            get() = "Our services are currently not available in this area."
    }

    fun updateAddress(
            addressId: String,
            label: String,
            fullAddress: String,
            latitude: String,
            longitude: String
    ) = flow {
        val response = profileService.updateAddress(
                addressId,
                label,
                fullAddress,
                latitude,
                longitude
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false, true)




}