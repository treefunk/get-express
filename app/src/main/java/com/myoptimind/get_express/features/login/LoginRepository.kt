package com.myoptimind.get_express.features.login

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.myoptimind.get_express.features.customer.home.api.HomeService
import com.myoptimind.get_express.features.login.api.LoginService
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import javax.inject.Inject

class LoginRepository @Inject constructor(
        private val loginService: LoginService,
        private val homeService: HomeService,
        private val context: Context
) {

    init {

    }

/*
    private fun getFirebaseId(): String {
        val idTask = FirebaseInstallations.getInstance().id
        return Tasks.await(idTask)
    }
*/

    @SuppressLint("HardwareIds")
    private fun getDeviceId() = Settings.Secure.getString(context.getContentResolver(),
            Settings.Secure.ANDROID_ID);

    fun signUpCustomer(
            fullname: String,
            email: String,
            mobileNum: String,
            birthdate: String,
            location: String,
            password: String?,
            socialToken: String?,
            isEmailVerified: String?,
            isOtpEnabled: Boolean = false
    ) = flow {
        val response = loginService.signUpCustomer(
                fullname, email, mobileNum, birthdate, location, password,socialToken,isEmailVerified, if(isOtpEnabled) "-2" else ""
        )
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)

    fun signInCustomer(
            email: String,
            password: String,
            firebaseToken: String,
            isOtpEnabled: Boolean = false
    ) = flow {
        val deviceId = getDeviceId()

        val response = loginService.signInCustomer(
                email, password, deviceId, firebaseToken, if(isOtpEnabled) "-2" else ""
        )
        Timber.v("Customer Sign in Details: \nEmail - $email\nDevice Id - $deviceId\nFirebase Id - $firebaseToken\n")
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)

    fun signInCustomerSocial(
        email: String,
        token: String,
        firebaseToken: String
    ) = flow {
        val deviceId = getDeviceId()

        val response = loginService.signInCustomerSocial(
            email, token, deviceId, firebaseToken
        )
        Timber.v("Customer Sign in Details: \nEmail - $email\nDevice Id - $deviceId\nFirebase Id - $firebaseToken\n")
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(false, true)

    fun signOut(userType: UserType) = flow {
        val deviceId = getDeviceId()
        val response = loginService.signOut(deviceId,userType.label)
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)


    fun getVehicles() = flow {
        val response = homeService.getVehicles()
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects()

    fun signUpRider(
            fullname: RequestBody,
            email: RequestBody,
            mobileNum: RequestBody,
            birthdate: RequestBody,
            location: RequestBody,
            password: RequestBody?,
            socialToken: RequestBody?,
            isEmailVerified: RequestBody?,
            identificationDocument: MultipartBody.Part,
            vehicleId: RequestBody,
            vehicleModel: RequestBody,
            plateNumber: RequestBody,
            isOtpEnabled: Boolean = false
    ) = flow {
        val response = loginService.signUpRider(
                fullname,
                email,
                mobileNum,
                birthdate,
                location,
                password,
                socialToken,
                isEmailVerified,
                identificationDocument,
                vehicleId,
                vehicleModel,
                plateNumber,
                if(isOtpEnabled) "-2" else ""
        )
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)

    fun signInRider(
            email: String,
            password: String,
            firebaseToken: String,
            isOtpEnabled: Boolean = false
    ) = flow {
        val deviceId = getDeviceId()
//        val firebaseId = getFirebaseId()

        val response = loginService.signInRider(
                email, password, deviceId, firebaseToken,if(isOtpEnabled) "-2" else ""
        )
        Timber.v("Rider Sign in Details: \nEmail - $email\nDevice Id - $deviceId\nFirebase Id - $firebaseToken\n")
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)

    fun signInRiderSocial(
        email: String,
        token: String,
        firebaseToken: String
    ) = flow {
        val deviceId = getDeviceId()

        val response = loginService.signInRiderSocial(
            email, token, deviceId, firebaseToken
        )
        Timber.v("Customer Sign in Details: \nEmail - $email\nDevice Id - $deviceId\nFirebase Id - $firebaseToken\n")
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)

    fun resetPassword(
            userType: UserType,
            email: String
    ) = flow {
        val response = loginService.resetPassword(
                "${userType.label}s",
                email
        )
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)

    fun solveOtpChallenge(
        email: String,
        mobile: String,
        userType: UserType
    ) = flow {
        val label = when(userType){
            UserType.CUSTOMER -> "customers"
            UserType.RIDER -> "riders"
        }
        val response = loginService.solveOtpChallenge(email,mobile,label)
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)

/*
    fun solveOtpChallengeRider(
        email: String,
        mobile: String
    ) = flow {
        val response = loginService.solveOtpChallengeRider(email,mobile)
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(enableRetry = false, nullOnComplete = true)
*/

    fun resendOtpChallenge(
        email: String,
        userType: UserType
    ) = flow {
        val label = when(userType){
            UserType.CUSTOMER -> "customers"
            UserType.RIDER -> "riders"
        }
        val response = loginService.resendOtpChallenge(
            email,label
        )
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }
}