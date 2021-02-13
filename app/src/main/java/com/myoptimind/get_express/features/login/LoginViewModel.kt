package com.myoptimind.get_express.features.login

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.myoptimind.get_express.features.customer.home.HomeRepository
import com.myoptimind.get_express.features.customer.home.api.HomeService
import com.myoptimind.get_express.features.login.api.LoginService
import com.myoptimind.get_express.features.login.data.FacebookUserPayload
import com.myoptimind.get_express.features.login.data.GoogleUserPayload
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.api.Result
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File

class LoginViewModel @ViewModelInject constructor(
    private val loginRepository: LoginRepository,
    private val homeRepository: HomeRepository,
    private val appSharedPref: AppSharedPref
) : ViewModel() {


    val facebookUserPayload: LiveData<FacebookUserPayload?> get() = _facebookUserPayload
    private val _facebookUserPayload = MutableLiveData<FacebookUserPayload?>()

    val googleUserPayload: LiveData<GoogleUserPayload?> get() = _googleUserPayload
    private val _googleUserPayload = MutableLiveData<GoogleUserPayload?>()

    fun clearLoginPayloads(){
        _facebookUserPayload.value = null
        _googleUserPayload.value = null
    }

    fun updateGoogleUserPayload(googleUserPayload: GoogleUserPayload){
        _facebookUserPayload.value = null
        _googleUserPayload.value = googleUserPayload
    }

    fun updateFacebookPayload(facebookUserPayload: FacebookUserPayload){
        _googleUserPayload.value = null
        _facebookUserPayload.value = facebookUserPayload
    }

    val vehicleList: LiveData<Result<HomeService.GetVehiclesResponse>>
        get() = loginRepository.getVehicles().asLiveData()

    val servicesResult: LiveData<Result<HomeService.ServicesResponse>>
        get() = homeRepository.getServices().asLiveData()

    /**
     *  Customers
     */
    val signUpCustomerResult: LiveData<Result<LoginService.CustomerSignUpResponse>> get() = _signUpCustomerResult
    private val _signUpCustomerResult = MutableLiveData<Result<LoginService.CustomerSignUpResponse>>()

    val signInCustomerResult: LiveData<Result<LoginService.CustomerSignInResponse>> get() = _signInCustomerResult
    private val _signInCustomerResult = MutableLiveData<Result<LoginService.CustomerSignInResponse>>()


    /**
     *  Rider
     */
    val uploadedLicense: LiveData<File> get() = _uploadedLicense
    private val _uploadedLicense = MutableLiveData<File>()

    val signUpRiderResult: LiveData<Result<LoginService.RiderSignUpResponse>> get() = _signUpRiderResult
    private val _signUpRiderResult = MutableLiveData<Result<LoginService.RiderSignUpResponse>>()

    val signInRiderResult: LiveData<Result<LoginService.RiderSignInResponse>> get() = _signInRiderResult
    private val _signInRiderResult = MutableLiveData<Result<LoginService.RiderSignInResponse>>()

    /**
     *  forgot password
     */

    val forgotPasswordResult: LiveData<Result<LoginService.ForgotPasswordResponse>> get() = _forgotPasswordResult
    private val _forgotPasswordResult = MutableLiveData<Result<LoginService.ForgotPasswordResponse>>()

    init {
        _uploadedLicense.value = null
    }

    /**
     *  CUSTOMER
     */
    fun signUpCustomer(
        fullname: String,
        email: String,
        mobileNum: String,
        birthdate: String,
        location: String,
        password: String? = null,
        socialToken: String? = null,
        isEmailVerified: String? = null
    ) {
        viewModelScope.launch(IO) {
            loginRepository.signUpCustomer(
                fullname,
                email,
                mobileNum,
                birthdate,
                location,
                password,
                socialToken,
                isEmailVerified
            ).collect {
                _signUpCustomerResult.postValue(it)
            }
        }

    }

    fun signInCustomer(
        email: String,
        password: String
    ) {
        retrieveFcmToken { firebaseToken ->
            viewModelScope.launch(IO) {

                loginRepository.signInCustomer(
                        email,
                        password,
                        firebaseToken
                ).collect {
                    _signInCustomerResult.postValue(it)
                    if(it is Result.Success && it.data != null){
                        val customer = it.data.data
                        appSharedPref.storeLoginCredentials(
                                customer.id,
                                "customer",
                                customer.profilePicture,
                                customer.fullName
                        )
                    }
                }
            }
        }
    }

    fun signInCustomerSocial(
        email: String,
        token: String,
    ) {
        retrieveFcmToken { firebaseToken ->
            viewModelScope.launch(IO) {

                loginRepository.signInCustomerSocial(
                    email,
                    token,
                    firebaseToken
                ).collect {
                    _signInCustomerResult.postValue(it)
                    if(it is Result.Success && it.data != null){
                        val customer = it.data.data
                        appSharedPref.storeLoginCredentials(
                                customer.id,
                                "customer",
                                customer.profilePicture,
                                customer.fullName
                        )
                    }
                }
            }
        }
    }

    /**
     *  RIDER
     */
    fun setUploadedLicense(uploadedLicense: File) {
        _uploadedLicense.postValue(uploadedLicense)
    }

    fun signUpRider(
        fullname: String,
        email: String,
        mobileNum: String,
        birthdate: String,
        location: String,
        password: String? = null,
        socialToken: String? = null,
        isEmailVerified: String? = null,
        vehicleId: String,
        vehicleModel: String,
        plateNumber: String
    ) {
        val identificationDocument = MultipartBody.Part.createFormData(
                "identification_document",
                _uploadedLicense.value?.name,
                _uploadedLicense.value!!.asRequestBody("image/*".toMediaType())
        )
        viewModelScope.launch(IO) {
            loginRepository.signUpRider(
                fullname.toRequestBody(),
                email.toRequestBody(),
                mobileNum.toRequestBody(),
                birthdate.toRequestBody(),
                location.toRequestBody(),
                password?.toRequestBody(),
                socialToken?.toRequestBody(),
                isEmailVerified?.toRequestBody(),
                identificationDocument,
                vehicleId.toRequestBody(),
                vehicleModel.toRequestBody(),
                plateNumber.toRequestBody()
            ).collect {
                _signUpRiderResult.postValue(it)
            }
        }

    }

    fun signInRider(
            email: String,
            password: String
    ) {
        retrieveFcmToken { firebaseToken ->
            viewModelScope.launch(IO) {
                loginRepository.signInRider(email, password,firebaseToken).collect {
                    _signInRiderResult.postValue(it)
                    if(it is Result.Success && it.data != null){
                        val customer = it.data.data
                        appSharedPref.storeRiderLoginCredentials(
                                customer.id,
                                "rider",
                                customer.profilePicture,
                                customer.fullName,
                                customer.activeVehicle,
                        )
                    }
                }
            }
        }
    }

    fun signInRiderSocial(
        email: String,
        token: String,
    ) {
        retrieveFcmToken { firebaseToken ->
            viewModelScope.launch(IO) {

                loginRepository.signInRiderSocial(
                    email,
                    token,
                    firebaseToken
                ).collect {
                    _signInRiderResult.postValue(it)
                    if(it is Result.Success && it.data != null){
                        val customer = it.data.data
                        appSharedPref.storeRiderLoginCredentials(
                                customer.id,
                                "rider",
                                customer.profilePicture,
                                customer.fullName,
                                customer.activeVehicle,
                        )
                    }
                }
            }
        }
    }

    fun resetPassword(
            email: String,
            userType: UserType
    ){
        viewModelScope.launch(IO){
            loginRepository.resetPassword(
                    userType,
                    email
            ).collect {
                _forgotPasswordResult.postValue(it)
            }
        }
    }

    private fun retrieveFcmToken(onRetrieveToken: (token: String) -> Unit){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e("Fetching FCM registration token failed")
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val fcmToken = task.result
            onRetrieveToken(fcmToken)
        })
    }

    val logoutResult: LiveData<Result<LoginService.SignOutResponse>> get() = _logoutResult
    private val _logoutResult = MutableLiveData<Result<LoginService.SignOutResponse>>()

    fun logout(){
        viewModelScope.launch(IO){
            loginRepository.signOut(appSharedPref.getUserType()).collect {
                _logoutResult.postValue(it)
            }
        }
    }

}