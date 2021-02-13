package com.myoptimind.get_express

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.myoptimind.get_express.features.customer.delivery.PERSON_TYPE
import com.myoptimind.get_express.features.customer.delivery.RecipientDeliveryDialog
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog
import com.myoptimind.get_express.features.login.data.Address
import com.myoptimind.get_express.features.shared.data.CartType
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

/*    fun showLoading(){
        parentActivity.showLoading()
    }

    fun hideLoading(){
        parentActivity.hideLoading()
    }*/

}