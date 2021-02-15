package com.myoptimind.get_express.features.customer.food_grocery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.CartViewModel
import com.myoptimind.get_express.features.customer.food_grocery.data.Store
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.CartType
import com.myoptimind.get_express.features.shared.data.idToCartType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_stores.*
import timber.log.Timber
import java.lang.Exception

@AndroidEntryPoint
class StoresFragment: TitleOnlyFragment() {

    private val viewModel by activityViewModels<StoresViewModel>()
    private val cartViewModel by activityViewModels<CartViewModel>()
    private val args: StoresFragmentArgs by navArgs()
    override fun getTitle() = ""

    var adapter: StoresAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_stores,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cartType = args.serviceId.idToCartType()
//        val vehicleType = args.vehicleTypeId.idToVehicleType()
        setNewTitle(cartType.label)

        rv_stores.layoutManager = GridLayoutManager(requireContext(),3)


        when(cartType){
            CartType.GROCERY -> et_search_store.hint = "Search for Items/Grocery"
            CartType.FOOD -> et_search_store.hint = "Search for Restaurant/Fast food"
            else -> throw Exception("Cart type must be grocery or food")
        }




        cartViewModel.cart.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        val adapter = StoresAdapter(ArrayList(), object: StoresAdapter.StoreListener {
                            override fun onPressed(store: Store, index: Int) {
//                Toast.makeText(requireContext(),store.name + " pressed.",Toast.LENGTH_LONG).show()
                                val cart = result.data.data
                                val cartType = cart.cartTypeId.idToCartType()
                                when(cartType){
                                    CartType.GROCERY,CartType.FOOD -> {
                                        val basket = cart.initBasketForGrocery()

                                        StoresFragmentDirections.actionStoresFragmentToSelectedStoreFragment(args.serviceId,args.cartId,store.id).also {
                                            cartViewModel.setActiveStore(store)
                                            cartViewModel.updateFromLocation(
                                                    Place.builder()
                                                            .setName(store.name)
                                                            .setAddress(store.locationText)
                                                            .setLatLng(LatLng(store.coordinates.latitude.toDouble(),store.coordinates.longitude.toDouble()))
                                                    .build()
                                            )
                                            cartViewModel.updateToLocation(cart.deliveryLocation.toPlace())
                                            findNavController().navigate(it)
                                        }
                                    }
                                }

                            }

                        })
                        rv_stores.adapter = adapter

                        viewModel.storesResult.observe(viewLifecycleOwner){ result ->
                            when(result){
                                is Result.Progress -> {

                                }
                                is Result.Success -> {
                                    if(result.data != null){
                                        val response = result.data
                                        adapter?.stores = response.data
                                        adapter?.notifyDataSetChanged()
                                    }
                                }
                                is Result.Error -> {
                                    Timber.e(result.metaResponse.message)
                                    if(result.metaResponse.status == 404){
                                            adapter.stores = listOf()
                                            adapter.notifyDataSetChanged()
                                    }
                                }
                                is Result.HttpError -> {
                                    Timber.e(result.error.message)
                                }
                            }
                        }

                    }
                }
                is Result.Error -> {
                    Timber.e(result.metaResponse.message)
                }
                is Result.HttpError -> {
                    Timber.e(result.error.message)
                }
            }
        }



        viewModel.getStoresByServiceId(args.serviceId,args.cartId)

        iv_search_store.setOnClickListener {
            viewModel.searchStores(et_search_store.text.toString(),args.serviceId,args.cartId)
        }
    }

}