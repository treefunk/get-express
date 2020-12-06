package com.myoptimind.getexpress.features.edit_profile

import com.myoptimind.getexpress.features.edit_profile.api.ProfileService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.switchMap
import okhttp3.RequestBody
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
            profilePicture: RequestBody?,
    ) = flow {
        val response = profileService.updateProfile(
                appSharedPref.getUserType().label + "s", // api accepts plural form of type
                appSharedPref.getUserId(),
                fullname, email, mobileNum, birthdate, location, password, profilePicture
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false,nullOnComplete = true)

}