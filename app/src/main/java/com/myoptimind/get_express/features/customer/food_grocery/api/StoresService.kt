package com.myoptimind.get_express.features.customer.food_grocery.api

import com.myoptimind.get_express.features.customer.cart.data.GetCartInfoResponse
import com.myoptimind.get_express.features.customer.food_grocery.data.Product
import com.myoptimind.get_express.features.customer.food_grocery.data.Store
import com.myoptimind.get_express.features.shared.api.MetaResponse
import retrofit2.http.*

interface StoresService {

    @GET("partners/service/{service_id}")
    suspend fun getStoresByService(
        @Path("service_id") serviceId: String,
        @Query("cart_id") cartId: String? = null
    ): StoreByServiceResponse

    data class StoreByServiceResponse(
            val data: List<Store>,
            val meta: MetaResponse
    )

    @GET("partners/search/{search}")
    suspend fun getStoresByKeyword(
            @Path("search") keyword: String,
            @Query("service_id") serviceId: String? = null,
            @Query("cart_id") cartId: String? = null
    ): StoreByServiceResponse

    @GET("inventory/partner/{partner_id}")
    suspend fun getProductsByStore(
            @Path("partner_id") storeId: String,
            @Query("category") category: String? = null,
            @Query("search") search: String? = null
    ): ProductsByStoreResponse

    data class ProductsByStoreResponse(
            val data: HashMap<String,List<Product>>,
            val meta: MetaResponse
    )

    @GET("inventory/categories/partner/{partner_id}")
    suspend fun getCategoriesByStore(
        @Path("partner_id") storeId: String
    ): CategoriesByStoreResponse

    data class CategoriesByStoreResponse(
        val data: List<String>,
        val meta: MetaResponse
    )

    @POST("cart/{cart_id}")
    @FormUrlEncoded
    suspend fun addOrUpdateItem(
            @Path("cart_id") cartId: String,
            @Field("product_id") productId: String,
            @Field("quantity") quantity: String,
            @Field("notes") notes: String,
            @Field("addon_ids[]") addOnIds: List<String>? = null,
            @Field("cart_item_id") cartItemId: String? = null
    ): GetCartInfoResponse

    @POST("cart/{cart_id}/finalize")
    @FormUrlEncoded
    suspend fun finalizeCart(
            @Path("cart_id") cartId: String,
            @Field("notes") notes: String,
            @Field("pickup_location") pickupLocation: String? = null,
            @Field("delivery_location") deliveryLocation: String? = null,
            @Field("payment_method") paymentMethod: String = "COD",
            @Query("status_change") statusChange: String? = null
    ): GetCartInfoResponse


    @GET("cart/{cart_id}/info")
    suspend fun getCartInformation(
            @Path("cart_id") cartId: String
    ): GetCartInfoResponse


   @GET("cart/customers/{customer_id}/active")
   suspend fun getActiveBooking(
           @Path("customer_id") customerId: String
   ): GetCartInfoResponse

   @POST("cart/{cart_id}/cancel")
   suspend fun cancelCart(
           @Path("cart_id") cartId: String
   ): GetCartInfoResponse

   @POST("cart/{cart_id}/empty")
   suspend fun emptyCart(
           @Path("cart_id") cartId: String
   ): GetCartInfoResponse














}