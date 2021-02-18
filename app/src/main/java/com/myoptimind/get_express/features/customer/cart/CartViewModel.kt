package com.myoptimind.get_express.features.customer.cart

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.myoptimind.get_express.features.customer.delivery.DeliveryRepository
import com.myoptimind.get_express.features.customer.food_grocery.StoresRepository
import com.myoptimind.get_express.features.customer.food_grocery.data.Store
import com.myoptimind.get_express.features.customer.food_grocery.selected_store.data.ItemPayload
import com.myoptimind.get_express.features.customer.home.HomeRepository
import com.myoptimind.get_express.features.customer.cart.data.CartLocation
import com.myoptimind.get_express.features.customer.cart.data.GetCartInfoResponse
import com.myoptimind.get_express.features.customer.cart.data.ItemInPabili
import com.myoptimind.get_express.features.customer.pabili.PabiliRepository
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.api.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class CartViewModel @ViewModelInject constructor(
        private val storesRepository: StoresRepository,
        private val deliveryRepository: DeliveryRepository,
        private val homeRepository: HomeRepository,
        private val pabiliRepository: PabiliRepository,
        private val appSharedPref: AppSharedPref
): ViewModel() {

    var cartId: String? = null
    var storeLocation: CartLocation? = null

    val place: LiveData<Place> get() = _place
    private val _place = MutableLiveData<Place>()


    val currentLocation: LiveData<LatLng?> get() = _currentLocation
    private val _currentLocation = MutableLiveData<LatLng?>()

    fun updateCurrentLocation(latLng: LatLng){
        _currentLocation.value = latLng
    }

    fun clearFromToLocations(){
        _fromLocation.value = null
        _toLocation.value = null
    }

    val fromLocation: LiveData<Place> get() = _fromLocation
    private val _fromLocation = MutableLiveData<Place>()

    fun updateFromLocation(fromLocation: Place?){
        _fromLocation.value = fromLocation
    }

    val toLocation: LiveData<Place> get() = _toLocation
    private val _toLocation = MutableLiveData<Place>()

    fun updateToLocation(toLocation: Place?){
        _toLocation.value = toLocation
    }

    val initCartResult: LiveData<Result<GetCartInfoResponse>> get() = _initCartResult
    private val _initCartResult = MutableLiveData<Result<GetCartInfoResponse>>()


    val activeStore: LiveData<Store>
        get() = _activeStore
    private val _activeStore = MutableLiveData<Store>()

    fun setActiveStore(store: Store){
        _activeStore.value = store
        storeLocation = CartLocation(
                store.name,
                store.locationText,
                "",
                store.coordinates.latitude,
                store.coordinates.longitude,
                ""
        )
    }



    fun initCart(
            cartTypeId: String,
            vehicleId: String,
            cartLocation: CartLocation?
    ){
        viewModelScope.launch(Dispatchers.IO){
            homeRepository.initCart(
                    cartTypeId,vehicleId,cartLocation
            ).collect {
                _initCartResult.postValue(it)
            }
        }
    }

    fun setCartInfo(cartInfoResponse: Result<GetCartInfoResponse>?){
        _cart.value = cartInfoResponse
    }

/*
    val addItemResult: LiveData<Result<GetCartInfoResponse>> get() = _addItemResult
    private val _addItemResult = MutableLiveData<Result<GetCartInfoResponse>>()
*/

    val cart: LiveData<Result<GetCartInfoResponse>> get() = _cart
    private val _cart = MutableLiveData<Result<GetCartInfoResponse>>()

    var shouldEmptyCartFirst = false

    fun emptyThenAddItemToCart(
            cartId: String,
            productId: String,
            quantity: String,
            notes: String,
            addOnIds: List<String>? = null,
            cartItemId: String? = null
    ){
        Timber.d("adding item from viewmodel..")
        viewModelScope.launch(Dispatchers.IO){
            storesRepository.emptyThenAddItemToCart(
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

    fun addItemToCart(
            cartId: String,
            productId: String,
            quantity: String,
            notes: String,
            addOnIds: List<String>? = null,
            cartItemId: String? = null
    ){
        Timber.d("adding item from viewmodel..")
        viewModelScope.launch(Dispatchers.IO){
            storesRepository.addItemToCart(
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

    var cartItemList: List<ItemPayload>? = null

    fun updateCartItemList(cartList: List<ItemPayload>){
        cartItemList = cartList
    }

    fun clearCartItemList(){
        cartItemList = null
    }

    val finalizeCartResult: LiveData<Result<GetCartInfoResponse>> get() = _finalizeCartResult
    private val _finalizeCartResult = MutableLiveData<Result<GetCartInfoResponse>>()

    fun finalizeCartForDelivery(
            cartId: String,
            notes: String,
            pickupLocation: CartLocation? = null,
            deliveryLocation: CartLocation? = null,
            paymentType: String = "COD",
            changeStatus: Boolean = true
    ){
        Timber.d("finalizing cart..")
        viewModelScope.launch(Dispatchers.IO){

            storesRepository.finalizeCart(
                    cartId,
                    notes,
                    pickupLocation,
                    deliveryLocation,
                    paymentType,
                    changeStatus
            ).collect {
                _cart.postValue(it)
            }
        }
    }


    fun finalizeCart(
            cartId: String,
            notes: String,
            pickupLocation: CartLocation? = null,
            deliveryLocation: CartLocation? = null,
            paymentType: String,
            changeStatus: Boolean = true
    ){
        Timber.d("finalizing cart..")
        viewModelScope.launch(Dispatchers.IO){
            val pickupLoc = if(pickupLocation == null && storeLocation != null){
                storeLocation
            } else pickupLocation

            storesRepository.finalizeCart(
                    cartId,
                    notes,
                    pickupLoc,
                    deliveryLocation,
                    paymentType,
                    changeStatus
            ).collect {
                _cart.postValue(it)
            }
        }
    }

    val fetchCartInfoResult: LiveData<Result<GetCartInfoResponse>> get() = _fetchCartInfoResult
    private val _fetchCartInfoResult = MutableLiveData<Result<GetCartInfoResponse>>()

    fun getCartInformation(
            cartId: String
    ){
        Timber.d("fetching cart information")
        viewModelScope.launch(Dispatchers.IO) {
            storesRepository.getCartInformation(
                    cartId
            ).collect {
                _fetchCartInfoResult.postValue(it)
            }
        }
    }

    val activeBookingResult: LiveData<Result<GetCartInfoResponse>> get() = _activeBookingResult
    private val _activeBookingResult = MutableLiveData<Result<GetCartInfoResponse>>()

    fun getActiveBooking() {
        viewModelScope.launch(Dispatchers.IO) {
            storesRepository.getCustomerActiveBooking(appSharedPref.getUserId()).collect {
                _activeBookingResult.postValue(it)
            }
        }
    }

//    val cancelBookingResult: LiveData<Result<GetCartInfoResponse>> get() = _cancelBookingResult
//    private val _cancelBookingResult = MutableLiveData<Result<GetCartInfoResponse>>()


    fun cancelBooking(
            cartId: String
    ){
        viewModelScope.launch(Dispatchers.IO) {
            storesRepository.cancelBooking(cartId).collect {
                withContext(Dispatchers.Main){
                    setCartInfo(it)
                }
            }
        }
    }

    fun emptyCart(
            cartId: String
    ){
        viewModelScope.launch(Dispatchers.IO) {
            storesRepository.cancelBooking(cartId).collect {
                withContext(Dispatchers.Main){
                    setCartInfo(it)
                }
            }
        }
    }


    val deliveryResult: LiveData<Result<GetCartInfoResponse>> get() = _deliveryResult
    private val _deliveryResult = MutableLiveData<Result<GetCartInfoResponse>>()


    fun createDelivery(
            cartId: String,
            notes: String,
            category: String,
            distance: String,
    ){
        viewModelScope.launch(IO){
            deliveryRepository.createDelivery(
                    cartId, notes, category, distance
            ).collect {
                _deliveryResult.postValue(it)
            }
        }
    }


    val pabiliItemList: LiveData<List<ItemInPabili>> get() = _pabiliItemList
    private val _pabiliItemList = MutableLiveData<List<ItemInPabili>>()

    fun updatePabiliItemList(list: List<ItemInPabili>){
        _pabiliItemList.value = list
    }

    val pabiliResult: LiveData<Result<GetCartInfoResponse>> get() = _pabiliResult
    private val _pabiliResult = MutableLiveData<Result<GetCartInfoResponse>>()

    fun createPabili(
            cartId: String,
            pabiliItems: List<ItemInPabili>,
            totalEstimateAmount: Double
    ){
        viewModelScope.launch(IO){
            pabiliRepository.createPabili(
                    cartId,
                    totalEstimateAmount,
                    pabiliItems
            ).collect {
                _pabiliResult.postValue(it)
            }
        }
    }
}