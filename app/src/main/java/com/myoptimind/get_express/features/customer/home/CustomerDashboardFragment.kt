package com.myoptimind.get_express.features.customer.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.internal.Utility.arrayList
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.CartViewModel
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog.Companion.EXTRA_CART_TYPE
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog.Companion.EXTRA_ENTER_ADDRESS
import com.myoptimind.get_express.features.customer.home.vehicle_options.VehicleOptionsDialog
import com.myoptimind.get_express.features.customer.whats_new.WhatsNewAdapter
import com.myoptimind.get_express.features.customer.whats_new.WhatsNewViewModel
import com.myoptimind.get_express.features.customer.whats_new.data.WhatsNew
import com.myoptimind.get_express.features.edit_profile.ProfileViewModel
import com.myoptimind.get_express.features.login.data.idToVehicleType
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.LogoOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.CartType
import com.myoptimind.get_express.features.shared.data.idToCartType
import com.myoptimind.get_express.features.shared.toCartLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_customer_dashboard.*
import kotlinx.android.synthetic.main.fragment_customer_dashboard.iv_get_delivery
import kotlinx.android.synthetic.main.fragment_customer_dashboard.iv_get_food
import kotlinx.android.synthetic.main.fragment_customer_dashboard.iv_get_grocery
import kotlinx.android.synthetic.main.fragment_customer_dashboard.iv_get_pabili
import kotlinx.android.synthetic.main.fragment_customer_dashboard.rv_whats_new
import kotlinx.android.synthetic.main.fragment_customer_dashboard_original.*
import kotlinx.android.synthetic.main.partial_nav_top.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CustomerDashboardFragment : LogoOnlyFragment() {

    private val cartViewModel by activityViewModels<CartViewModel>()
    private val editProfileViewModel by activityViewModels<ProfileViewModel>()
    private val whatsNewViewModel by activityViewModels<WhatsNewViewModel>()

    private lateinit var adapter: WhatsNewAdapter

    @Inject
    lateinit var appSharedPref: AppSharedPref


    companion object {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListener()
        initObservers()

        cartViewModel.getActiveBooking()
        editProfileViewModel.getCustomerProfile()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).ib_whats_new.visibility = View.VISIBLE
        (activity as MainActivity).ib_whats_new.setOnClickListener {
            CustomerDashboardFragmentDirections.actionHomeFragmentToWhatsNewFragment().also {
                findNavController().navigate(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).ib_whats_new.visibility = View.GONE
    }

    private fun initObservers() {

        adapter = WhatsNewAdapter(arrayList(), object : WhatsNewAdapter.WhatsNewListener {
            override fun onSelectWhatsNew(whatsNew: WhatsNew) {
                CustomerDashboardFragmentDirections.actionHomeFragmentToSelectedWhatsNewFragment(
                    whatsNew
                ).also {
                    findNavController().navigate(it)
                }
            }
        })
        val manager = object : LinearLayoutManager(requireContext()) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                val margin = 8
                lp?.height = (height / 2) - ((margin * 2) + 4)
                lp?.bottomMargin = margin + 4
                lp?.topMargin = margin
                return true
            }
        }.also {
            it.orientation = RecyclerView.VERTICAL
        }
        rv_whats_new.layoutManager = manager
        rv_whats_new.adapter = adapter

        lifecycleScope.launchWhenCreated {
            whatsNewViewModel.whatsNewListResult.collect { result ->
                when (result) {
                    is Result.Progress -> {
                        initCenterProgress(result.isLoading)
                    }
                    is Result.Success -> {
                        if (result.data != null) {
                            adapter.whatsNewsList = result.data.data
                            adapter.notifyDataSetChanged()
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



        cartViewModel.activeBookingResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {
                    if (result.isLoading) {
                        Timber.d("Checking active booking..")
                    }
                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                }
                is Result.Success -> {
                    if (result.data != null) {

                        Timber.d("Active booking detected..")
                        val cart = result.data.data
                        if (cart.createdAt == null) {
                            return@observe
                        }
                        val cartType = cart.cartTypeId.idToCartType()

                        cartViewModel.setCartInfo(result)


                                CustomerDashboardFragmentDirections.actionHomeFragmentToCustomerRiderSearchFragment(
                                    cart.id
                                ).also {
                                    findNavController().navigate(it)
                                }
                    }else{

                        editProfileViewModel.getCustomerProfile.observe(viewLifecycleOwner) { result ->
                            when (result) {
                                is Result.Progress -> {
                                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                                }
                                is Result.Success -> {
                                    if (result.data != null) {
                                        iv_get_food.setOnClickListener {
                                            showAddressSelection(
                                                result.data.data.addresses,
                                                REQUEST_SELECT_ADDRESS, CartType.FOOD
                                            )
                                        }
                                        iv_get_grocery.setOnClickListener {
                                            showAddressSelection(result.data.data.addresses,
                                                REQUEST_SELECT_ADDRESS,CartType.GROCERY)
                                        }
                                        iv_get_pabili.setOnClickListener {
                                            showVehicleTypeChooser(CartType.PABILI)
                                        }
                                        iv_get_delivery.setOnClickListener {
                                            showVehicleTypeChooser(CartType.DELIVERY)
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


                        appSharedPref.getPendingBooking()?.let {
                            cartViewModel.getPendingBooking(it)
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

        cartViewModel.initCartResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {
                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                }
                is Result.Success -> {
                    if (result.data != null) {
                        Timber.d("Cart Initialized! Redirecting..")
                        val cart = result.data.data
                        val cartType = cart.cartTypeId.idToCartType()

                        cartViewModel.setCartInfo(result)
                        cartViewModel.clearFromToLocations()
                        when (cartType) {
                            CartType.CAR -> TODO()
                            CartType.GROCERY, CartType.FOOD -> {
                                CustomerDashboardFragmentDirections.actionHomeFragmentToStoresFragment(
                                    cart.cartTypeId,
                                    cart.id
                                ).also {
                                    findNavController().navigate(it)
                                }
                            }
                            CartType.PABILI -> {
                                cartViewModel.clearPabiliItems()
                                CustomerDashboardFragmentDirections.actionHomeFragmentToPabiliFormFragment(
                                    cart.id,
                                    cart.vehicleId
                                ).also {
                                    findNavController().navigate(it)
                                }
                            }
                            CartType.DELIVERY -> {
                                CustomerDashboardFragmentDirections.actionHomeFragmentToDeliveryFormFragment(
                                    cart.id,
                                    cart.vehicleId
                                ).also {
                                    findNavController().navigate(it)
                                }
                            }
                        }
                    }
                }
                is Result.Error -> {
                    Timber.e(result.metaResponse.message)
                    val errorMeta = result.metaResponse
                    if(errorMeta.status == 400){
                        Snackbar.make(requireView(),errorMeta.message,Snackbar.LENGTH_LONG).show()
                    }
                }
                is Result.HttpError -> {
                    Timber.e(result.error.message)
                }
            }
        }
    }

    private fun initClickListener() {

        (activity as MainActivity).tv_history_side.setOnClickListener {
/*            Navigation.findNavController(requireActivity(), R.id.nav_host_container).navigate(
                R.id.action_global_customerProfileFragment,
                Bundle().also { it.putBoolean("is_history", true) })*/
            CustomerDashboardFragmentDirections.actionGlobalCustomerProfileFragment(true).also {
                findNavController().navigate(it)
            }
            if ((activity as MainActivity).drawer_layout.isDrawerOpen(GravityCompat.END)) {
                (activity as MainActivity).drawer_layout.closeDrawers()
            } else {
                (activity as MainActivity).drawer_layout.openDrawer(GravityCompat.END)
            }
        }

    }


    private fun showVehicleTypeChooser(cartType: CartType) {
        val dialogVehicleOptions = VehicleOptionsDialog.newInstance(cartType)
        dialogVehicleOptions.setTargetFragment(this@CustomerDashboardFragment, REQUEST_VEHICLE_TYPE)
        dialogVehicleOptions.show(parentFragmentManager, TAG_VEHICLE_OPTIONS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VEHICLE_TYPE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val ext = data.extras
                val vehicleType =
                    ext?.getString(VehicleOptionsDialog.EXTRA_VEHICLE_ID)?.idToVehicleType()
                val cartType = ext?.getString(VehicleOptionsDialog.EXTRA_SERVICE_ID)?.idToCartType()

                if (vehicleType != null && cartType != null) {
                    Timber.d(cartType.label + " - " + vehicleType.label)
                    cartViewModel.initCart(
                        cartType.id,
                        vehicleType.id,
                        cartViewModel.toLocation.value?.toCartLocation()
                    )
                }
            }
        }

        if (requestCode == REQUEST_SELECT_ADDRESS && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val ext = data.extras
                val cartType = ext?.getParcelable<CartType>(EXTRA_CART_TYPE)!!
                val selectedPlace = ext.getParcelable<Place>(EXTRA_ENTER_ADDRESS)
                showVehicleTypeChooser(cartType)
                cartViewModel.updateToLocation(selectedPlace!!)
            }
        }
    }

    private fun enableViews(enable: Boolean){
//        iv_get_car.isEnabled = enable
        iv_get_food.isEnabled = enable
        iv_get_delivery.isEnabled = enable
        iv_get_pabili.isEnabled = enable
        iv_get_grocery.isEnabled = enable
    }
}