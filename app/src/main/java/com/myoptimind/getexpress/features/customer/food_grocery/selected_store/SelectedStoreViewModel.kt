package com.myoptimind.getexpress.features.customer.food_grocery.selected_store

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.myoptimind.getexpress.features.customer.food_grocery.StoresRepository

class SelectedStoreViewModel @ViewModelInject constructor(
        val storesRepository: StoresRepository
): ViewModel() {

/*    val productCategoryResult: LiveData<Result<StoresService.ProductsByStoreResponse>> get() = _productCategoryResult
    private val _productCategoryResult = MutableLiveData<Result<StoresService.ProductsByStoreResponse>>()

    fun getProductsByStore(
            storeId: String,
            category: String?,
            search: String?
    ){
        viewModelScope.launch(Dispatchers.IO){
            storesRepository.getProductsByStore(
                    storeId,category,search
            ).collect {
                _productCategoryResult.postValue(it)
            }
        }
    }*/
}