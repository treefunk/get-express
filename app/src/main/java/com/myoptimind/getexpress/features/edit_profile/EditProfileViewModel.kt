package com.myoptimind.getexpress.features.edit_profile

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.myoptimind.getexpress.features.edit_profile.api.ProfileService
import com.myoptimind.getexpress.features.login.LoginRepository
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File

class EditProfileViewModel @ViewModelInject constructor(
    private val profileRepository: ProfileRepository,
    private val loginRepository: LoginRepository,
    private val appSharedPref: AppSharedPref
): ViewModel() {
    val getRiderProfile: LiveData<Result<ProfileService.RiderProfileResponse>> get() = _getRiderProfile
    private val _getRiderProfile = MutableLiveData<Result<ProfileService.RiderProfileResponse>>()

    val getCustomerProfile: LiveData<Result<ProfileService.CustomerProfileResponse>> get() = _getCustomerProfile
    private val _getCustomerProfile = MutableLiveData<Result<ProfileService.CustomerProfileResponse>>()

    val vehicleTypeList = loginRepository.getVehicles().asLiveData()

    val addVehicleResult: LiveData<Result<ProfileService.AddVehicleResponse>> get() = _addVehicleResult
    private val _addVehicleResult = MutableLiveData<Result<ProfileService.AddVehicleResponse>>()

    val changeActiveVehicleResponse: LiveData<Result<ProfileService.ChangeActiveVehicleResponse>> get() = _changeActiveVehicleResult
    private val _changeActiveVehicleResult = MutableLiveData<Result<ProfileService.ChangeActiveVehicleResponse>>()

    val updateProfileResult: LiveData<Result<ProfileService.UserResponse>> get() = _updateProfileResult
    private val _updateProfileResult = MutableLiveData<Result<ProfileService.UserResponse>>()

    val uploadedPicture: LiveData<File> get() = _uploadedPicture
    private val _uploadedPicture = MutableLiveData<File>()

    init {
        _uploadedPicture.value = null
    }

    fun getRiderProfile(){
        viewModelScope.launch(IO){
            profileRepository.getRiderProfile().collect {
                _getRiderProfile.postValue(it)
            }
        }
    }

    fun setUploadedPicture(uploadedPicture: File) {
        _uploadedPicture.postValue(uploadedPicture)
    }

    fun updateProfile(
            fullname: String?,
            email: String?,
            mobileNum: String?,
            birthDate: String?,
            location: String?,
            password: String?
    ){
        viewModelScope.launch(IO){
            profileRepository.updateProfileInformation(
                fullname?.toRequestBody(),
                    email?.toRequestBody(),
                    mobileNum?.toRequestBody(),
                    birthDate?.toRequestBody(),
                    location?.toRequestBody(),
                    password?.toRequestBody(),
                    _uploadedPicture.value?.asRequestBody()
            ).collect {
                _updateProfileResult.postValue(it)
            }
        }
    }

    fun getCustomerProfile(){
        viewModelScope.launch(IO){
            profileRepository.getCustomerProfile().collect {
                _getCustomerProfile.postValue(it)
            }
        }
    }

    fun addVehicle(
            vehicleTypeId: String,
            vehicleModel: String,
            plateNumber: String
    ){
        viewModelScope.launch(IO){
            profileRepository.addVehicle(
                    vehicleTypeId, vehicleModel, plateNumber
            ).collect {
                Timber.v("updating add vehicle")
                _addVehicleResult.postValue(it)
            }
        }
    }

    fun changeActiveVehicle(
            vehicleId: String
    ){
        viewModelScope.launch(IO){
            profileRepository.changeActiveVehicle(
                    vehicleId
            ).collect {
                _changeActiveVehicleResult.postValue(it)
            }
        }
    }

}