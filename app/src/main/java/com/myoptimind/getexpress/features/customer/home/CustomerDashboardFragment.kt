package com.myoptimind.getexpress.features.customer.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.model.Place
import com.myoptimind.getexpress.MainActivity
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.cart.CartViewModel
import com.myoptimind.getexpress.features.customer.cart.data.CartLocation
import com.myoptimind.getexpress.features.customer.home.SelectAddressBottomDialog.Companion.EXTRA_CART_TYPE
import com.myoptimind.getexpress.features.customer.home.SelectAddressBottomDialog.Companion.EXTRA_ENTER_ADDRESS
import com.myoptimind.getexpress.features.customer.home.vehicle_options.VehicleOptionsDialog
import com.myoptimind.getexpress.features.edit_profile.EditProfileViewModel
import com.myoptimind.getexpress.features.login.data.Address
import com.myoptimind.getexpress.features.login.data.idToVehicleType
import com.myoptimind.getexpress.features.shared.LogoOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.data.CartType
import com.myoptimind.getexpress.features.shared.data.idToCartType
import com.myoptimind.getexpress.features.shared.toCartLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_customer_dashboard.*
import timber.log.Timber

@AndroidEntryPoint
class CustomerDashboardFragment: LogoOnlyFragment() {

    private val cartViewModel by activityViewModels<CartViewModel>()
    private val editProfileViewModel by activityViewModels<EditProfileViewModel>()

    companion object {
        private const val REQUEST_SELECT_ADDRESS = 222
        private const val TAG_SELECT_ADDRESS = "SELECT_ADDRESS_DIALOG"

        private const val REQUEST_VEHICLE_TYPE = 333
        private const val TAG_VEHICLE_OPTIONS = "VEHICLE_OPTIONS_DIALOG"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_dashboard,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListener()
        initObservers()

        cartViewModel.getActiveBooking()
        editProfileViewModel.getCustomerProfile()
    }

    private fun initObservers() {

        editProfileViewModel.getCustomerProfile.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {}
                is Result.Success -> {
                    if(result.data != null){
                        iv_get_food.setOnClickListener {
                            showAddressSelection(CartType.FOOD,result.data.data.addresses)
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

        cartViewModel.activeBookingResult.observe(viewLifecycleOwner){ result ->
            when(result) {
                is Result.Progress -> {
                    if (result.isLoading) {
                        Timber.d("Checking active booking..")
                    }
                }
                is Result.Success -> {
                    if (result.data != null) {

                        Timber.d("Active booking detected..")
                        val cart = result.data.data
                        if(cart.createdAt == null){
                            return@observe
                        }
                        val cartType = cart.cartTypeId.idToCartType()

                        cartViewModel.setCartInfo(result)

                        when(cartType){
                            CartType.CAR -> TODO()
                            CartType.GROCERY,CartType.FOOD -> {
                                CustomerDashboardFragmentDirections.actionHomeFragmentToStoresFragment(cart.cartTypeId, cart.id,cart.deliveryLocation.toPlace()).also {
                                    CustomerDashboardFragmentDirections.actionHomeFragmentToCustomerRiderSearchFragment(cart.id).also {
                                        findNavController().navigate(it)
                                    }
                                }
                            }
                            CartType.PABILI -> {
                                CustomerDashboardFragmentDirections.actionHomeFragmentToCustomerRiderSearchFragment(cart.id).also {
                                    findNavController().navigate(it)
                                }
                            }
                            CartType.DELIVERY -> {
                                CustomerDashboardFragmentDirections.actionHomeFragmentToDeliveryFormFragment(cart.id,cart.vehicleId).also {
                                    findNavController().navigate(it)
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

        cartViewModel.initCartResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    if(result.isLoading){
                       Timber.d("Initializing Cart..")
                    }
                }
                is Result.Success -> {
                    if(result.data != null){
                        Timber.d("Cart Initialized! Redirecting..")
                        val cart = result.data.data
                        val cartType = cart.cartTypeId.idToCartType()

                        cartViewModel.setCartInfo(result)
                        when(cartType){
                            CartType.CAR -> TODO()
                            CartType.GROCERY,CartType.FOOD -> {
                                CustomerDashboardFragmentDirections.actionHomeFragmentToStoresFragment(cart.cartTypeId,cart.id,cart.deliveryLocation.toPlace()).also {
                                    findNavController().navigate(it)
                                }
                            }
                            CartType.PABILI -> {
                                CustomerDashboardFragmentDirections.actionHomeFragmentToPabiliFormFragment(cart.id,cart.vehicleId).also {
                                    findNavController().navigate(it)
                                }
                            }
                            CartType.DELIVERY -> {
                                    CustomerDashboardFragmentDirections.actionHomeFragmentToDeliveryFormFragment(cart.id,cart.vehicleId).also {
                                        findNavController().navigate(it)
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
    }

    private fun initClickListener() {
//        iv_get_grocery.setOnClickListener {
//            showVehicleTypeChooser(CartType.GROCERY)
//        }
//        iv_get_car.setOnClickListener {
//            showVehicleTypeChooser(CartType.CAR)
//        }
        iv_get_pabili.setOnClickListener {
            showVehicleTypeChooser(CartType.PABILI)
        }
        iv_get_delivery.setOnClickListener {
            showVehicleTypeChooser(CartType.DELIVERY)
        }


        (activity as MainActivity).findViewById<TextView>(R.id.tv_history_side).setOnClickListener {
            Navigation.findNavController(requireActivity(),R.id.nav_host_container).navigate(R.id.action_global_customerProfileFragment, Bundle().also { it.putBoolean("is_history",true) })
            if ((activity as MainActivity).drawer_layout.isDrawerOpen(GravityCompat.END)) {
                (activity as MainActivity).drawer_layout.closeDrawers()
            } else {
                (activity as MainActivity).drawer_layout.openDrawer(GravityCompat.END)
            }
        }
    }

    private fun showAddressSelection(cartType: CartType, addressList: List<Address>){
        val selectAddressBottomSheet = SelectAddressBottomDialog.newInstance(cartType,
                addressList
/*                listOf(Address("","","testlabel","testaddress","",""),
                        Address("","","testlabel2","testaddress","","")
                )*/
        )
        selectAddressBottomSheet.setTargetFragment(this@CustomerDashboardFragment, REQUEST_SELECT_ADDRESS)
        selectAddressBottomSheet.show(parentFragmentManager, TAG_SELECT_ADDRESS)
    }

    private fun showVehicleTypeChooser(cartType: CartType){
        val dialogVehicleOptions = VehicleOptionsDialog.newInstance(cartType)
        dialogVehicleOptions.setTargetFragment(this@CustomerDashboardFragment, REQUEST_VEHICLE_TYPE)
        dialogVehicleOptions.show(parentFragmentManager,TAG_VEHICLE_OPTIONS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_VEHICLE_TYPE && resultCode == Activity.RESULT_OK){
            if(data != null){
                val ext = data.extras
                val vehicleType = ext?.getString(VehicleOptionsDialog.EXTRA_VEHICLE_ID)?.idToVehicleType()
                val cartType = ext?.getString(VehicleOptionsDialog.EXTRA_SERVICE_ID)?.idToCartType()

                if (vehicleType != null && cartType != null) {
                    Timber.d(cartType.label + " - " + vehicleType.label)
                            cartViewModel.initCart(cartType.id,vehicleType.id,cartViewModel.toLocation.value!!.toCartLocation())
                }
            }
        }

        if(requestCode == REQUEST_SELECT_ADDRESS && resultCode == Activity.RESULT_OK){
            if(data != null){
                val ext = data.extras
                val cartType = ext?.getParcelable<CartType>(EXTRA_CART_TYPE)!!
                val selectedPlace = ext.getParcelable<Place>(EXTRA_ENTER_ADDRESS)
                showVehicleTypeChooser(cartType)
                cartViewModel.updateToLocation(selectedPlace!!)
            }
        }
    }
}