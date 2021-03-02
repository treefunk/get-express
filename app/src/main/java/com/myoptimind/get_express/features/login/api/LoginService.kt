package com.myoptimind.get_express.features.login.api

import com.myoptimind.get_express.features.login.data.Customer
import com.myoptimind.get_express.features.login.data.Rider
import com.myoptimind.get_express.features.login.data.User
import com.myoptimind.get_express.features.shared.api.MetaResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


interface LoginService {



    /**
     * CUSTOMERS
     */



    @POST("sign-up{otp}/customer")
    @FormUrlEncoded
    suspend fun signUpCustomer(
        @Field("full_name") fullname: String,
        @Field("email") email: String,
        @Field("mobile_num") mobileNum: String,
        @Field("birthdate") birthdate: String,
        @Field("location") location: String,
        @Field("password") password: String? = null,
        @Field("social_token") socialToken: String? = null,
        @Field("is_email_verified") isEmailVerified: String? = null,
        @Path("otp") otpEnabled: String,
    ): CustomerSignUpResponse

    data class CustomerSignUpResponse (
        val data: Customer,
        val meta: MetaResponse
    )

    @POST("sign-in{otp}/customer")
    @FormUrlEncoded
    suspend fun signInCustomer(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("device_id") deviceId: String,
        @Field("firebase_id") firebaseId: String,
        @Path("otp") otpEnabled: String
    ): CustomerSignInResponse

    data class CustomerSignInResponse(
        val data: Customer,
        val meta: MetaResponse
    )

    @POST("social-sign-in/customers")
    @FormUrlEncoded
    suspend fun signInCustomerSocial(
        @Field("email") email: String,
        @Field("social_token") socialToken: String,
        @Field("device_id") deviceId: String,
        @Field("firebase_id") firebaseId: String
    ): CustomerSignInResponse



    @POST("sign-out/{user_type}")
    @FormUrlEncoded
    suspend fun signOut(
        @Field("device_id") deviceId: String,
        @Path("user_type") userType: String
    ): SignOutResponse

    data class SignOutResponse(
        val meta: MetaResponse
    )





    /**
     *  Riders
     */

    @Multipart
    @POST("sign-up{otp}/rider")
    suspend fun signUpRider(
        @Part("full_name") fullname: RequestBody,
        @Part("email") email: RequestBody,
        @Part("mobile_num") mobileNum: RequestBody,
        @Part("birthdate") birthdate: RequestBody,
        @Part("location") location: RequestBody,
        @Part("password") password: RequestBody? = null,
        @Part("social_token") socialToken: RequestBody? = null,
        @Part("is_email_verified") isEmailVerified: RequestBody? = null,
        @Part identificationDocument: MultipartBody.Part,
        @Part("vehicle_id") vehicleId: RequestBody,
        @Part("vehicle_model") vehicleModel: RequestBody,
        @Part("plate_number") plateNumber: RequestBody,
        @Path("otp") otpEnabled: String
    ): RiderSignUpResponse

    data class RiderSignUpResponse(
        val data: Rider,
        val meta: MetaResponse
    )

    @POST("sign-in{otp}/rider")
    @FormUrlEncoded
    suspend fun signInRider(
            @Field("email") email: String,
            @Field("password") password: String,
            @Field("device_id") deviceId: String,
            @Field("firebase_id") fireBaseId: String,
            @Path("otp") otpEnabled: String
    ): RiderSignInResponse

    data class RiderSignInResponse(
            val data: Rider,
            val meta: MetaResponse
    )

    @POST("social-sign-in/riders")
    @FormUrlEncoded
    suspend fun signInRiderSocial(
        @Field("email") email: String,
        @Field("social_token") socialToken: String,
        @Field("device_id") deviceId: String,
        @Field("firebase_id") firebaseId: String
    ): RiderSignInResponse


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

    data class UserSignInResponse(
        val data: User,
        val meta: MetaResponse

    )

    @POST("sign-in/{userType}/otp")
    @FormUrlEncoded
    suspend fun solveOtpChallenge(
        @Field("email") email: String,
        @Field("mobile_otp") mobileNum: String,
        @Path("userType") userType: String
    ): UserSignInResponse

/*    @POST("sign-in/riders/otp")
    @FormUrlEncoded
    suspend fun solveOtpChallengeRider(
        @Field("email") email: String,
        @Field("mobile_otp") mobileNum: String
    ): RiderSignInResponse*/

    @POST("sign-in/{userType}/otp/resend")
    @FormUrlEncoded
    suspend fun resendOtpChallenge(
        @Field("email") email: String,
        @Path("userType") userType: String
    ): ResendOtpResponse

    data class ResendOtpResponse(
        val meta: MetaResponse
    )






}