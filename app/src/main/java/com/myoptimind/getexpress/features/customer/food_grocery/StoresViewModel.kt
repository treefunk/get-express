package com.myoptimind.getexpress.features.customer.food_grocery

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoptimind.getexpress.features.customer.home.HomeRepository
import com.myoptimind.getexpress.features.customer.food_grocery.api.StoresService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.coroutines.Dispatchers

class StoresViewModel @ViewModelInject constructor(
        private val repository: StoresRepository,
        private val homeRepository: HomeRepository,
        private val appSharedPref: AppSharedPref
): ViewModel() {

//    var cartId: String? = null
//    var storeLocation: CartLocation? = null
//
//    val place: LiveData<Place> get() = _place
//    private val _place = MutableLiveData<Place>()
//
//    fun updateLocation(place: Place){
//        _place.value = place
//    }

    val storesResult: LiveData<Result<StoresService.StoreByServiceResponse>> get() = _storesResult
    private val _storesResult = MutableLiveData<Result<StoresService.StoreByServiceResponse>>()

    fun updateStoresResult(storesResult: Result<StoresService.StoreByServiceResponse>){
        _storesResult.value = storesResult
    }

    fun getStoresByServiceId(serviceId: String,cartId: String?){
        viewModelScope.launch(IO){
            repository.getStoresByService(serviceId,cartId).collect {
                _storesResult.postValue(it)
            }
        }
    }

    fun searchStores(search: String, serviceId: String,cartId: String?){
        viewModelScope.launch(IO){
            repository.getStoresByKeyword(search,serviceId,cartId).collect {
                _storesResult.postValue(it)
            }
        }
    }



    val productCategoryResult: LiveData<Result<StoresService.ProductsByStoreResponse>> get() = _productCategoryResult
    private val _productCategoryResult = MutableLiveData<Result<StoresService.ProductsByStoreResponse>>()

    fun getProductsByStore(
            storeId: String,
            category: String?,
            search: String?
    ){
        viewModelScope.launch(Dispatchers.IO){
            repository.getProductsByStore(
                    storeId,category,search
            ).collect {
                _productCategoryResult.postValue(it)
            }
        }
    }

//    val activeStore: LiveData<Store>
//        get() = _activeStore
//    private val _activeStore = MutableLiveData<Store>()
//
//    fun setActiveStore(store: Store){
//        _activeStore.value = store
//        storeLocation = CartLocation(
//                store.name,
//                store.locationText,
//                store.coordinates.latitude,
//                store.coordinates.longitude,
//                ""
//        )
//    }

/*    val initCartResult: LiveData<Result<CustomerRequestService.GetCartInfoResponse>> get() = _initCartResult
    private val _initCartResult = MutableLiveData<Result<CustomerRequestService.GetCartInfoResponse>>()

    fun initCart(
            cartTypeId: String,
            vehicleId: String
    ){
        viewModelScope.launch(IO){
            homeRepository.initCart(
                    cartTypeId,vehicleId
            ).collect {
                _initCartResult.postValue(it)
            }
        }
    }

    fun setCartInfo(cartInfoResponse: Result<CustomerRequestService.GetCartInfoResponse>){
        _cart.value = cartInfoResponse
    }

*//*
    val addItemResult: LiveData<Result<CustomerRequestService.GetCartInfoResponse>> get() = _addItemResult
    private val _addItemResult = MutableLiveData<Result<CustomerRequestService.GetCartInfoResponse>>()
*//*

    val cart: LiveData<Result<CustomerRequestService.GetCartInfoResponse>> get() = _cart
    private val _cart = MutableLiveData<Result<CustomerRequestService.GetCartInfoResponse>>()

    fun addItemToCart(
        cartId: String,
        productId: String,
        quantity: String,
        notes: String,
        addOnIds: List<String>? = null,
        cartItemId: String? = null
    ){
        Timber.d("adding item from viewmodel..")
        viewModelScope.launch(IO){
            repository.addItemToCart(
                    cartId,
                    productId,
                    quantity,
                    notes,
                    addOnIds,
                    cartItemId
            ).collect {
                _cart.postValue(it)
            }
        }
    }

    var cartItemList: List<CartItem>? = null

    fun updateCartItemList(cartList: List<CartItem>){
        cartItemList = cartList
    }

    fun clearCartItemList(){
        cartItemList = null
    }

    val finalizeCartResult: LiveData<Result<CustomerRequestService.GetCartInfoResponse>> get() = _finalizeCartResult
    private val _finalizeCartResult = MutableLiveData<Result<CustomerRequestService.GetCartInfoResponse>>()

    fun finalizeCart(
            cartId: String,
            notes: String,
            pickupLocation: CartLocation? = null,
            deliveryLocation: CartLocation? = null,
            paymentType: String
    ){
        Timber.d("finalizing cart..")
        viewModelScope.launch(IO){

            val pickupLoc = if(pickupLocation == null && storeLocation != null) storeLocation else null


            repository.finalizeCart(
                cartId,
                    notes,
                    pickupLoc,
                    deliveryLocation,
                    paymentType
            ).collect {
                _cart.postValue(it)
            }
        }
    }

    val fetchCartInfoResult: LiveData<Result<CustomerRequestService.GetCartInfoResponse>> get() = _fetchCartInfoResult
    private val _fetchCartInfoResult = MutableLiveData<Result<CustomerRequestService.GetCartInfoResponse>>()

    fun getCartInformation(
            cartId: String
    ){
        Timber.d("fetching cart information")
        viewModelScope.launch(IO) {
            repository.getCartInformation(
                    cartId
            ).collect {
                _fetchCartInfoResult.postValue(it)
            }
        }
    }

    val activeBookingResult: LiveData<Result<CustomerRequestService.GetCartInfoResponse>> get() = _activeBookingResult
    private val _activeBookingResult = MutableLiveData<Result<CustomerRequestService.GetCartInfoResponse>>()

    fun getActiveBooking() {
        viewModelScope.launch(IO) {
            repository.getCustomerActiveBooking(appSharedPref.getUserId()).collect {
                _activeBookingResult.postValue(it)
            }
        }
    }

//    val cancelBookingResult: LiveData<Result<CustomerRequestService.GetCartInfoResponse>> get() = _cancelBookingResult
//    private val _cancelBookingResult = MutableLiveData<Result<CustomerRequestService.GetCartInfoResponse>>()


    fun cancelBooking(
            cartId: String
    ){
        viewModelScope.launch(IO) {
            repository.cancelBooking(cartId).collect {
                withContext(Main){
                    setCartInfo(it)
                }
            }
        }
    }

    fun emptyCart(
            cartId: String
    ){
        viewModelScope.launch(IO) {
            repository.cancelBooking(cartId).collect {
                withContext(Main){
                    setCartInfo(it)
                }
            }
        }
    }*/



}