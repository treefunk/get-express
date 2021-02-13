package com.myoptimind.get_express.features.customer.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.Place
import com.myoptimind.get_express.features.edit_profile.ProfileRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.myoptimind.get_express.features.shared.api.Result

class EnterAddressViewModel @ViewModelInject constructor(
    private val profileRepository: ProfileRepository
): ViewModel() {

    val selectedPlace: LiveData<Place> get() = _selectedPlace
    private val _selectedPlace = MutableLiveData<Place>()

    fun updateSelectedPlace(place: Place){
        _selectedPlace.value = place
    }

    val checkLocationResult: LiveData<Result<Place>> get() = _checkLocationResult
    private val _checkLocationResult = MutableLiveData<Result<Place>>()

    fun checkLocationIfValid(
            selectedPlace: Place
    ){
        viewModelScope.launch(IO){
            profileRepository.checkLocation(selectedPlace).collect {
                _checkLocationResult.postValue(it)
            }
        }
    }
}