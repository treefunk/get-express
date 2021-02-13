package com.myoptimind.get_express.features.login.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.shared.api.BaseRemoteEntity
import kotlinx.android.parcel.Parcelize

open class User(
    var id: String = "",
    @SerializedName("full_name")
    var fullName: String = "",
    var email: String = "",
    @SerializedName("mobile_num")
    var mobileNum: String= "",
    var birthdate: String = "",
    var location: String = "",
    var password: String = "",
    @SerializedName("profile_picture")
    var profilePicture: String = "",
    @SerializedName("social_token")
    var socialToken: String = "",
    @SerializedName("is_email_verified")
    var isEmailVerified: String = "",
    @SerializedName("mobile_otp")
    var mobileOtp: String = "",
    @SerializedName("verification_token")
    var verification_token: String= "",
    @SerializedName("banned_at")
    var bannedAt: String = "",

): BaseRemoteEntity()

@Parcelize
data class FacebookUserPayload(
    val fbToken: String,
    val email: String,
    val firstName: String,
    val lastName: String
): Parcelable

@Parcelize
data class GoogleUserPayload(
        val googleToken: String,
        val email: String,
        val name: String
        ): Parcelable

