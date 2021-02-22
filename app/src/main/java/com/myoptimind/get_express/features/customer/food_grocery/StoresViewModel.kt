package com.myoptimind.get_express.features.customer.food_grocery

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoptimind.get_express.features.customer.home.HomeRepository
import com.myoptimind.get_express.features.customer.food_grocery.api.StoresService
import com.myoptimind.get_express.features.shared.AppSharedPref
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.myoptimind.get_express.features.shared.api.Result
import kotlinx.coroutines.Dispatchers

class StoresViewModel @ViewModelInject constructor(
        private val repository: StoresRepository,
        private val homeRepository: HomeRepository,
        private val appSharedPref: AppSharedPref
): ViewModel() {



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

    val productCategory: LiveData<String?> get() = _productCategory
    private val _productCategory = MutableLiveData<String?>()

    val productSearch: LiveData<String?> get() = _productSearch
    private val _productSearch   = MutableLiveData<String?>()

    val productMinPrice: LiveData<String?> get() = _productMinPrice
    private val _productMinPrice   = MutableLiveData<String?>()

    val productMaxPrice: LiveData<String?> get() = _productMaxPrice
    private val _productMaxPrice   = MutableLiveData<String?>()

    fun updateCategory(category: String?){
        _productCategory.value = category
    }

    fun updateSearch(keyword: String?){
        _productSearch.value = keyword
    }

    fun updateMinPrice(minPrice: String?){
        _productMinPrice.value = minPrice
    }

    fun updateMaxPrice(maxPrice: String?){
        _productMaxPrice.value = maxPrice
    }

    fun clearFilters(){
        _productSearch.value = null
        _productCategory.value = null
        _productMinPrice.value = null
        _productMaxPrice.value = null
    }

    fun getProductsByStore(
            storeId: String
    ){
        viewModelScope.launch(Dispatchers.IO){
            val productCategory = if(_productCategory.value == "All") null else _productCategory.value
            repository.getProductsByStore(
                    storeId,productCategory,_productSearch.value,_productMinPrice.value,_productMaxPrice.value
            ).collect {
                _productCategoryResult.postValue(it)
            }
        }
    }

    val storeResult:LiveData<Result<StoresService.SingleStoreResponse>> get() = _storeResult
    private val _storeResult = MutableLiveData<Result<StoresService.SingleStoreResponse>>()


    val storeCategories: LiveData<Result<StoresService.CategoriesByStoreResponse>> get() = _storeCategories
    private val _storeCategories = MutableLiveData<Result<StoresService.CategoriesByStoreResponse>>()


    fun getCategoriesByStore(
        storeId: String
    ){
        viewModelScope.launch(IO){
            repository.getCategoriesByStore(
                storeId
            ).collect {
                _storeCategories.postValue(it)
            }
        }
    }



}