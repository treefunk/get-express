package com.myoptimind.get_express.features.rider.topup.api

import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.login.data.Rider
import com.myoptimind.get_express.features.rider.topup.data.WalletOffer
import com.myoptimind.get_express.features.rider.topup.data.WalletTransactionHistory
import com.myoptimind.get_express.features.shared.api.BaseRemoteEntity
import com.myoptimind.get_express.features.shared.api.MetaResponse
import com.myoptimind.get_express.features.shared.api.TopUpMetaResponse
import com.myoptimind.get_express.features.shared.toReadableDateTime
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.PeriodFormat
import org.joda.time.format.PeriodFormatterBuilder
import retrofit2.http.*

interface RiderTopupService {

    @GET("riders/wallet/offers")
    suspend fun getWalletOffers(
        @Query("vehicle_id") vehicleId: String
    ): WalletOffersResponse

    data class WalletOffersResponse(
        val data: List<WalletOffer>,
        val meta: TopUpMetaResponse
    )

    @POST("riders/{rider_id}/wallet")
    @FormUrlEncoded
    suspend fun topUpWallet(
        @Path("rider_id") riderId: String,
        @Field("promo_name") promoName: String,
        @Field("amount") amount: String,
        @Field("duration_in_hours") durationInHours: String
    ): TopUpResponse

    data class TopUpResponse(
        val data: TopUpData,
        val meta: MetaResponse
    )

    @POST("riders/{rider_id}/avail-offer-by-balance")
    @FormUrlEncoded
    suspend fun topUpWalletByBalance(
        @Path("rider_id") riderId: String,
        @Field("promo_name") promoName: String,
        @Field("amount") amount: String,
        @Field("duration_in_hours") durationInHours: String
    ): TopUpResponse


    data class TopUpData(
        val id: String,
        val amount: String,
        @SerializedName("rider_id")
        val riderId: String,
        @SerializedName("transaction_id")
        val transactionId: String,
        @SerializedName("checkout_url")
        val checkoutUrl: String,
        val status: String,
        @SerializedName("duration_in_hours")
        val durationInHours: String,
    ): BaseRemoteEntity()

    @GET("riders/{rider_id}/wallet")
    suspend fun getWalletTransactionHistory(
            @Path("rider_id") riderId: String
    ): WalletTransactionResponse

    data class WalletTransactionResponse(
            val data: List<WalletTransactionHistory>,
            @SerializedName("booking_available_until")
            val bookingAvailableUntil: String,
            @SerializedName("booking_available_until_f")
            val bookingAvailableUntilFormatted: String,
            val meta: MetaResponse
    ){

        fun getExpiresIn(): String? {
            if(bookingAvailableUntil.isBlank()){
                return null
            }
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

            val periodFormatter = PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours()
                .appendSuffix(":")
                .appendMinutes()
                .appendSuffix(":")
                .appendSeconds()
                .toFormatter()

            val bookingUntil = DateTime.parse(bookingAvailableUntil,formatter)
            return if((bookingUntil.dayOfYear().get() - DateTime().dayOfYear().get()) <= 1 ){
                val per = Period(DateTime(), bookingUntil)
                per.toString(periodFormatter)
            }else{
                null
            }
        }

    }

    @POST("riders/{rider_id}/balance")
    @FormUrlEncoded
    suspend fun addWalletBalance(
        @Path("rider_id") riderId: String,
        @Field("amount") amount: String
    ): AddWalletResponse

    data class AddWalletResponse(
        val data: TopUpData,
        val meta: MetaResponse
    )

    @GET("riders/{rider_id}/balance")
    suspend fun getRemainingBalance(
        @Path("rider_id") riderId: String
    ): RemainingBalanceResponse


    data class RemainingBalanceResponse(
        val data: String,
        val meta: MetaResponse
    )

    @POST("riders/{rider_id}/cash-on-hand")
    @FormUrlEncoded
    suspend fun updateCashOnHand(
        @Path("rider_id") riderId: String,
        @Field("amount") amount: String
    ): UpdateCashOnHandResponse

    data class UpdateCashOnHandResponse(
        val data: Rider,
        val meta: MetaResponse
    )








}