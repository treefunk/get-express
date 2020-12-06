package com.myoptimind.getexpress.features.login.data

import com.google.gson.annotations.SerializedName
import com.myoptimind.getexpress.features.shared.api.BaseRemoteEntity

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