package com.myoptimind.getexpress.features.login

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.myoptimind.getexpress.features.CUSTOMER.home.HomeRepository
import com.myoptimind.getexpress.features.CUSTOMER.home.api.HomeService
import com.myoptimind.getexpress.features.login.api.LoginService
import com.myoptimind.getexpress.features.login.data.UserType
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class LoginViewModel @ViewModelInject constructor(
    private val loginRepository: LoginRepository,
    private val homeRepository: HomeRepository,
    private val appSharedPref: AppSharedPref
) : ViewModel() {


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
        password: String,
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
        viewModelScope.launch(IO) {
            loginRepository.signInCustomer(email, password).collect {
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
        password: String,
        socialToken: String? = null,
        isEmailVerified: String? = null,
        vehicleId: String,
        vehicleModel: String,
        plateNumber: String
    ) {
        viewModelScope.launch(IO) {
            loginRepository.signUpRider(
                fullname.toRequestBody(),
                email.toRequestBody(),
                mobileNum.toRequestBody(),
                birthdate.toRequestBody(),
                location.toRequestBody(),
                password.toRequestBody(),
                socialToken?.toRequestBody(),
                isEmailVerified?.toRequestBody(),
                uploadedLicense.value!!.asRequestBody(),
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
        viewModelScope.launch(IO) {
            loginRepository.signInRider(email, password).collect {
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

}