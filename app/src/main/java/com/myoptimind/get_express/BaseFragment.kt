package com.myoptimind.get_express

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.myoptimind.get_express.features.customer.delivery.PERSON_TYPE
import com.myoptimind.get_express.features.customer.delivery.RecipientDeliveryDialog
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog
import com.myoptimind.get_express.features.login.data.Address
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import com.myoptimind.get_express.features.shared.data.CartType
import com.myoptimind.get_express.features.shared.hideKeyboard
import timber.log.Timber

abstract class BaseFragment: Fragment() {
    internal lateinit var parentActivity: MainActivity


    companion object {
        internal const val REQUEST_SELECT_ADDRESS = 222
        internal const val TAG_SELECT_ADDRESS = "SELECT_ADDRESS_DIALOG"

        internal const val REQUEST_VEHICLE_TYPE = 333
        internal const val TAG_VEHICLE_OPTIONS = "VEHICLE_OPTIONS_DIALOG"
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    internal fun sendCommandToService(action: String, cartId: String?, sendCoordinates: Boolean) = RiderTrackingService.createIntent(
        requireContext().applicationContext,
        action,
        cartId,
        sendCoordinates
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        try {
            parentActivity = (activity as MainActivity)
        } catch (exception: TypeCastException) {
            Timber.e("Parent Activity must be of type \"MainActivity\"!!")
        }
    }

    internal fun showAddressSelection(
        addressList: List<Address>,
        requestCode: Int = REQUEST_SELECT_ADDRESS,
        cartType: CartType? = null
    ){
        val selectAddressBottomSheet = SelectAddressBottomDialog.newInstance(
            addressList,
            cartType
        )
//        val frag = (parentFragmentManager.findFragmentByTag(TAG_SELECT_ADDRESS) as SelectAddressBottomDialog)
            selectAddressBottomSheet.setTargetFragment(this, requestCode)
            selectAddressBottomSheet.show(parentFragmentManager, TAG_SELECT_ADDRESS)
    }

    internal fun showAddressSelectionWithMap(
        personType: PERSON_TYPE,
        requestCode: Int = REQUEST_SELECT_ADDRESS
    ){
        val recipientDeliveryDialog = RecipientDeliveryDialog.newInstance(personType)
        recipientDeliveryDialog.setTargetFragment(this, requestCode)
        recipientDeliveryDialog.show(parentFragmentManager, TAG_SELECT_ADDRESS)
    }

    fun showLoading(){
        parentActivity.showLoading()
    }

    fun hideLoading(){
        parentActivity.hideLoading()
    }

    override fun onResume() {
        hideLoading()
        hideKeyboard(requireActivity())
        super.onResume()
    }

    fun initCenterProgress(showLoading: Boolean){
        if(showLoading)
            showLoading()
        else
            hideLoading()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }



}