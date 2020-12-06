package com.myoptimind.getexpress.features.edit_profile.api

import com.myoptimind.getexpress.features.login.data.Customer
import com.myoptimind.getexpress.features.login.data.Rider
import com.myoptimind.getexpress.features.login.data.User
import com.myoptimind.getexpress.features.shared.api.MetaResponse
import okhttp3.RequestBody
import retrofit2.http.*

interface ProfileService {

    @GET("profile/customers/{id}")
    suspend fun getCustomerProfile(
        @Path("id") id: String
    ): CustomerProfileResponse

    data class CustomerProfileResponse(
        val data: Customer,
        val meta: MetaResponse
    )

    @GET("profile/riders/{id}")
    suspend fun getRiderProfile(
        @Path("id") id: String
    ): RiderProfileResponse

    data class RiderProfileResponse(
        val data: Rider,
        val meta: MetaResponse
    )

    @POST("riders/{rider_id}/vehicles")
    @FormUrlEncoded
    suspend fun addVehicle(
            @Path("rider_id") riderId: String,
            @Field("vehicle_id") vehicleTypeId: String,
            @Field("vehicle_model") vehicleModel: String,
            @Field("plate_number") plateNumber: String
    ): AddVehicleResponse

    data class AddVehicleResponse(
            val data: Rider,
            val meta: MetaResponse
    )

    @POST("riders/{rider_id}/vehicles/{vehicle_id}/active")
    suspend fun changeActiveVehicle(
            @Path("rider_id") riderId: String,
            @Path("vehicle_id") vehicleId: String,
    ): ChangeActiveVehicleResponse

    data class ChangeActiveVehicleResponse(
            val data: Rider,
            val meta: MetaResponse
    )

    @Multipart
   @POST("profile/{user_type}/{user_id}")
   suspend fun updateProfile(
           @Path("user_type") userType: String,
           @Path("user_id") userId: String,
           @Part("full_name") fullname: RequestBody? = null,
           @Part("email") email: RequestBody? = null,
           @Part("mobile_num") mobileNum: RequestBody? = null,
           @Part("birthdate") birthdate: RequestBody? = null,
           @Part("location") location: RequestBody? = null,
           @Part("password") password: RequestBody? = null,
           @Part("profile_picture") profilePicture: RequestBody? = null,
   ): UserResponse

   data class UserResponse(
           val data: User,
           val meta: MetaResponse
   )


}