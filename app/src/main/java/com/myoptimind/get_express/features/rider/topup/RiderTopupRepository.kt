package com.myoptimind.get_express.features.rider.topup

import com.myoptimind.get_express.features.rider.topup.api.RiderTopupService
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RiderTopupRepository @Inject constructor(
    private val riderTopupService: RiderTopupService
) {

    fun getWalletOffers(
        vehicleTypeId: String,
    ) = flow {
        val response = riderTopupService.getWalletOffers(vehicleTypeId)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun topUpWallet(
        riderId: String,
        promoName: String,
        amount: String,
        durationInHours: String
    ) = flow {
        val response = riderTopupService.topUpWallet(
            riderId,promoName, amount, durationInHours
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun topUpWalletByBalance(
        riderId: String,
        promoName: String,
        amount: String,
        durationInHours: String
    ) = flow {
        val response = riderTopupService.topUpWalletByBalance(
            riderId,promoName, amount, durationInHours
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun addWalletBalance(
        riderId: String,
        amount: String,
    ) = flow {
        val response = riderTopupService.addWalletBalance(
            riderId,amount
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun getRemainingBalance(
        riderId: String
    ) = flow {
        val response = riderTopupService.getRemainingBalance(riderId)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun updateCashOnHand(
        riderId: String,
        amount: String
    ) = flow {
        val response = riderTopupService.updateCashOnHand(riderId,amount)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)


}