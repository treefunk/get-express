package com.myoptimind.get_express.features.rider.rider_dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.myoptimind.get_express.features.rider.topup.RiderTopupRepository
import com.myoptimind.get_express.features.rider.topup.api.RiderTopupService
import com.myoptimind.get_express.features.shared.api.Result
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RiderDashboardViewModel @ViewModelInject constructor(
        val riderTopupRepository: RiderTopupRepository
): ViewModel() {

    val walletOffersResult: LiveData<Result<RiderTopupService.WalletOffersResponse>> get() = _walletOffersResult
    private val _walletOffersResult = MutableLiveData<Result<RiderTopupService.WalletOffersResponse>>()


    fun getWalletOffers(
        vehicleTypeId: String
    ){
        viewModelScope.launch(IO){
            riderTopupRepository.getWalletOffers(vehicleTypeId).collect {
                _walletOffersResult.postValue(it)
            }
        }
    }

    val topUpWalletResult: LiveData<Result<RiderTopupService.TopUpResponse>> get() = _topUpWalletResult
    private val _topUpWalletResult = MutableLiveData<Result<RiderTopupService.TopUpResponse>>()

    fun topUpWallet(
        riderId: String,
        promoName: String,
        amount: String,
        durationInHours: String
    ){
        viewModelScope.launch(IO){
            riderTopupRepository.topUpWallet(
                riderId,promoName,amount,durationInHours
            ).collect {
                _topUpWalletResult.postValue(it)
            }
        }
    }

    val topUpWalletByBalanceResult: LiveData<Result<RiderTopupService.TopUpResponse>> get() = _topUpWalletByBalanceResult
    private val _topUpWalletByBalanceResult = MutableLiveData<Result<RiderTopupService.TopUpResponse>>()

    fun topUpWalletByBalance(
        riderId: String,
        promoName: String,
        amount: String,
        durationInHours: String
    ){
        viewModelScope.launch(IO){
            riderTopupRepository.topUpWalletByBalance(
                riderId,promoName,amount,durationInHours
            ).collect {
                _topUpWalletByBalanceResult.postValue(it)
            }
        }
    }

    val addWalletBalanceResult: LiveData<Result<RiderTopupService.AddWalletResponse>> get() = _addWalletBalanceResult
    private val _addWalletBalanceResult = MutableLiveData<Result<RiderTopupService.AddWalletResponse>>()

    fun addWalletBalance(
        riderId: String,
        amount: String,
    ){
        viewModelScope.launch(IO){
            riderTopupRepository.addWalletBalance(
                riderId,amount
            ).collect {
                _addWalletBalanceResult.postValue(it)
            }
        }
    }

    val updateCashOnHandResult: LiveData<Result<RiderTopupService.UpdateCashOnHandResponse>> get() = _updateCashOnHandResult
    private val _updateCashOnHandResult = MutableLiveData<Result<RiderTopupService.UpdateCashOnHandResponse>>()

    fun updateCashOnHand(
        riderId: String,
        amount: String,
    ){
        viewModelScope.launch(IO){
            riderTopupRepository.updateCashOnHand(
                riderId,amount
            ).collect {
                _updateCashOnHandResult.postValue(it)
            }
        }
    }


}