package com.myoptimind.get_express.features.edit_profile

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.libraries.places.api.model.Place
import com.myoptimind.get_express.features.edit_profile.api.ProfileService
import com.myoptimind.get_express.features.login.LoginRepository
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.api.Result
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File


class ProfileViewModel @ViewModelInject constructor(
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

    val selectedPlace: LiveData<Place> get() = _selectedPlace
    private val _selectedPlace = MutableLiveData<Place>()

    fun updateSelectedPlace(place: Place?){
        _selectedPlace.value = place
    }

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

            var profilePicture: MultipartBody.Part? = null

            if(_uploadedPicture.value != null){
                profilePicture = MultipartBody.Part.createFormData(
                    "profile_picture",
                    _uploadedPicture.value?.name,
                    _uploadedPicture.value!!.asRequestBody("image/*".toMediaType())
                )
            }

            profileRepository.updateProfileInformation(
                fullname?.toRequestBody(),
                email?.toRequestBody(),
                mobileNum?.toRequestBody(),
                birthDate?.toRequestBody(),
                location?.toRequestBody(),
                password?.toRequestBody(),
                profilePicture
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

    val addAddressResult: LiveData<Result<ProfileService.AddressResponse>> get() = _addAddressResult
    private val _addAddressResult = MutableLiveData<Result<ProfileService.AddressResponse>>()

    fun addAddress(
            label: String,
            addressId: String,
    ){
        viewModelScope.launch(IO){
            val fullAddress = _selectedPlace.value!!.address!!
            val latitude    = _selectedPlace.value!!.latLng!!.latitude.toString()
            val longitude   = _selectedPlace.value!!.latLng!!.longitude.toString()
            val newLabel = if(label.isNotBlank()) label else _selectedPlace.value!!.name!!
            profileRepository.addAddress(
                    newLabel,fullAddress,latitude,longitude,addressId
            ).collect {
                _addAddressResult.postValue(it)
            }
        }
    }

    val updateAddressResult: LiveData<Result<ProfileService.AddressResponse>> get() = _updateAddressResult
    private val _updateAddressResult = MutableLiveData<Result<ProfileService.AddressResponse>>()

    fun updateAddress(
            addressId: String,
            label: String,
            fullAddress: String,
            latitude: String,
            longitude: String
    ){
        viewModelScope.launch(IO){
            profileRepository.updateAddress(
                    addressId,label,fullAddress,latitude,longitude
            ).collect {
                _updateAddressResult.postValue(it)
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