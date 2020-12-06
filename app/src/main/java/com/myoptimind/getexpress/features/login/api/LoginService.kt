package com.myoptimind.getexpress.features.login.api

import com.myoptimind.getexpress.features.login.data.Customer
import com.myoptimind.getexpress.features.login.data.Rider
import com.myoptimind.getexpress.features.login.data.VehicleListItem
import com.myoptimind.getexpress.features.shared.api.MetaResponse
import okhttp3.RequestBody
import retrofit2.http.*


interface LoginService {

    /**
     * CUSTOMERS
     */

    @POST("sign-up/customer")
    @FormUrlEncoded
    suspend fun signUpCustomer(
        @Field("full_name") fullname: String,
        @Field("email") email: String,
        @Field("mobile_num") mobileNum: String,
        @Field("birthdate") birthdate: String,
        @Field("location") location: String,
        @Field("password") password: String? = null,
        @Field("social_token") socialToken: String? = null,
        @Field("is_email_verified") isEmailVerified: String? = null
    ): CustomerSignUpResponse

    data class CustomerSignUpResponse (
        val data: Customer,
        val meta: MetaResponse
    )

    @POST("sign-in/customer")
    @FormUrlEncoded
    suspend fun signInCustomer(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("device_id") deviceId: String,
        @Field("firebase_id") firebaseId: String
    ): CustomerSignInResponse

    data class CustomerSignInResponse(
        val data: Customer,
        val meta: MetaResponse
    )

    /**
     *  Riders
     */

    @Multipart
    @POST("sign-up/rider")
    suspend fun signUpRider(
        @Part("full_name") fullname: RequestBody,
        @Part("email") email: RequestBody,
        @Part("mobile_num") mobileNum: RequestBody,
        @Part("birthdate") birthdate: RequestBody,
        @Part("location") location: RequestBody,
        @Part("password") password: RequestBody? = null,
        @Part("social_token") socialToken: RequestBody? = null,
        @Part("is_email_verified") isEmailVerified: RequestBody? = null,
        @Part("identification_document") identificationDocument: RequestBody,
        @Part("vehicle_id") vehicleId: RequestBody,
        @Part("vehicle_model") vehicleModel: RequestBody,
        @Part("plate_number") plateNumber: RequestBody
    ): RiderSignUpResponse

    data class RiderSignUpResponse(
        val data: Rider,
        val meta: MetaResponse
    )

    @POST("sign-in/rider")
    @FormUrlEncoded
    suspend fun signInRider(
            @Field("email") email: String,
            @Field("password") password: String,
            @Field("device_id") deviceId: String,
            @Field("firebase_id") fireBaseId: String
    ): RiderSignInResponse

    data class RiderSignInResponse(
            val data: Rider,
            val meta: MetaResponse
    )


    /**
     * FORGOT PASSWORD
     */
    @POST("forgot-password/{type}")
    @FormUrlEncoded
    suspend fun resetPassword(
            @Path("type") userType: String,
            @Field("email") email: String
    ): ForgotPasswordResponse

    data class ForgotPasswordResponse(
            val meta: MetaResponse
    )




}