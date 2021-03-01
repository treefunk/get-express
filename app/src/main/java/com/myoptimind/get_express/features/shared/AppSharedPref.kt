package com.myoptimind.get_express.features.shared

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.myoptimind.get_express.features.login.data.Rider
import com.myoptimind.get_express.features.login.data.RiderVehicle
import com.myoptimind.get_express.features.login.data.UserType
import timber.log.Timber

const val NAME = "GetExpressSharedPref"
const val FILE_NAME = "shared_pref"
private const val MODE = Context.MODE_PRIVATE

private const val PREF_USER_ID   = "pref_user_id"
private const val PREF_FULL_NAME = "pref_full_name"
private const val PREF_EMAIL_ADDRESS = "pref_email_address"
private const val PREF_PROFILE_PICTURE = "pref_profile_picture"
private const val PREF_USER_TYPE = "pref_user_type"

private const val PREF_RIDER_VEHICLE_MODEL = "rider_vehicle_model"
private const val PREF_RIDER_VEHICLE_PLATE_NO = "rider_vehicle_plate_no"
private const val PREF_RIDER_VEHICLE_TYPE_ID = "rider_vehicle_type"

private const val PREF_PENDING_BOOKING = "pref_pending_booking"
class AppSharedPref(val context: Context) {
    private var preferences: SharedPreferences? = null


    init {
        setupSharedPref()
    }

    private fun setupSharedPref() {
        Timber.v("initializing shared pref")
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
//        preferences = EncryptedSharedPreferences.create(context, FILE_NAME,masterKey,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM) as EncryptedSharedPreferences
        preferences = try {
            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (exception: Exception) {
            context.getSharedPreferences(NAME, MODE)
        }


    }

    fun getUserType(): UserType {
        return when(preferences?.getString(PREF_USER_TYPE,"")){
            "rider" -> UserType.RIDER
            "customer" -> UserType.CUSTOMER
            else -> UserType.CUSTOMER
        }
    }

    fun storeLoginCredentials(
        userId: String,
        userType: String,
        profilePicture: String,
        fullname: String
    ) {
        val editor = preferences?.edit()
        editor?.putString(PREF_USER_ID, userId)
        editor?.putString(PREF_USER_TYPE,userType)
        editor?.putString(PREF_FULL_NAME, fullname)
        editor?.putString(PREF_PROFILE_PICTURE, profilePicture)
        editor?.apply()
    }

    fun storeRiderLoginCredentials(
            userId: String,
            userType: String,
            profilePicture: String,
            fullname: String,
            activeVehicle: RiderVehicle
    ) {
        val editor = preferences?.edit()
        editor?.putString(PREF_USER_ID, userId)
        editor?.putString(PREF_USER_TYPE,userType)
        editor?.putString(PREF_FULL_NAME, fullname)
        editor?.putString(PREF_PROFILE_PICTURE, profilePicture)
        editor?.putString(PREF_RIDER_VEHICLE_TYPE_ID, activeVehicle.vehicleId)
        editor?.putString(PREF_RIDER_VEHICLE_MODEL, activeVehicle.vehicleModel)
        editor?.putString(PREF_RIDER_VEHICLE_PLATE_NO, activeVehicle.plateNumber)
        editor?.apply()
    }

    fun updateActiveVehicle(
        activeVehicle: RiderVehicle
    ){
        val editor = preferences?.edit()
        editor?.putString(PREF_RIDER_VEHICLE_TYPE_ID, activeVehicle.vehicleId)
        editor?.putString(PREF_RIDER_VEHICLE_MODEL, activeVehicle.vehicleModel)
        editor?.putString(PREF_RIDER_VEHICLE_PLATE_NO, activeVehicle.plateNumber)
        editor?.apply()

    }




    fun getRiderFromPrefs(): Rider {
        return Rider().apply {
            id = getUserId()
            fullName = preferences?.getString(PREF_FULL_NAME, "")!!
            email = preferences?.getString(PREF_EMAIL_ADDRESS, "")!!
            profilePicture = preferences?.getString(PREF_PROFILE_PICTURE,"")!!
            activeVehicle = RiderVehicle(
                    vehicleModel = preferences?.getString(PREF_RIDER_VEHICLE_MODEL,"")!!,
                    plateNumber = preferences?.getString(PREF_RIDER_VEHICLE_PLATE_NO, "")!!,
                    vehicleId = preferences?.getString(PREF_RIDER_VEHICLE_TYPE_ID,"")!!
            )
        }
    }

    fun getUserId(): String = preferences?.getString(PREF_USER_ID, "")!!
    fun getUserFullname(): String = preferences?.getString(PREF_FULL_NAME,"")!!

    fun removeUserId() {
        val editor = preferences?.edit()
        editor?.remove(PREF_USER_ID)
        editor?.remove(PREF_PROFILE_PICTURE)
        editor?.remove(PREF_EMAIL_ADDRESS)
        editor?.remove(PREF_FULL_NAME)
        editor?.apply()
    }

    fun storePendingBooking(cartId: String){
        preferences?.edit()?.apply {
            putString(PREF_PENDING_BOOKING,cartId)
        }?.apply()
    }

    fun getPendingBooking(): String? {
        return preferences?.getString(PREF_PENDING_BOOKING,null)
    }

    fun clearPendingBooking(){
        preferences?.edit()?.apply {
            remove(PREF_PENDING_BOOKING)
        }?.apply()
    }


}