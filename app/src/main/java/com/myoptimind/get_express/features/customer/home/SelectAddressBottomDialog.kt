package com.myoptimind.get_express.features.customer.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.Address
import com.myoptimind.get_express.features.shared.data.CartType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_select_address.*


@AndroidEntryPoint
class SelectAddressBottomDialog: BottomSheetDialogFragment() {



    companion object {

        private const val ARGS_CART_TYPE = "ARGS_CART_TYPE"
        private const val ARGS_ADDRESS_LIST = "ARGS_ADDRESS_LIST"
        private const val DELIVERY_LOCATION_REQUEST = 313
        const val EXTRA_ENTER_ADDRESS = "EXTRA_ENTER_ADDRESS"
        const val EXTRA_CART_TYPE     = "EXTRA_CART_TYPE"


        fun newInstance(addressList: List<Address>,cartType: CartType? = null ): SelectAddressBottomDialog {
            val args = Bundle()
            args.putParcelable(ARGS_CART_TYPE, cartType)
            args.putParcelableArrayList(ARGS_ADDRESS_LIST, ArrayList(addressList))
            val fragment = SelectAddressBottomDialog()
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_select_address, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addressList = requireArguments().getParcelableArrayList<Address>(ARGS_ADDRESS_LIST)

        if(addressList.isNullOrEmpty().not()){
            btn_select_from_address_book.setOnClickListener {
                showAddressSelection(addressList!!.toList())
            }
        }else{
            btn_select_from_address_book.visibility = View.GONE
        }

        btn_enter_new_address.setOnClickListener {
//            showPlacesAutocomplete(DELIVERY_LOCATION_REQUEST)
            val dialogEnterAddress = EnterAddressDialog.newInstance()
            dialogEnterAddress.setTargetFragment(this@SelectAddressBottomDialog, DELIVERY_LOCATION_REQUEST)
            dialogEnterAddress.show(parentFragmentManager, "Enter address")
        }

    }

    private fun showAddressSelection(addressList: List<Address>){
        val singleItems = addressList.map { "${it.label}" }.toTypedArray()
        val checkedItem = 0

        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Address:")
                .setNeutralButton("CANCEL") { dialog, which ->
                    // Respond to neutral button press
                }
                .setPositiveButton("OK") { dialog, which ->
                    // Respond to positive button
                    val lw: ListView = (dialog as AlertDialog).getListView()
                    val cartType = requireArguments().getParcelable<CartType>(ARGS_CART_TYPE)

                    targetFragment?.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().also {
                                it.putExtra(EXTRA_ENTER_ADDRESS, addressList[lw.getCheckedItemPosition()].toPlace())
                                if(cartType != null){
                                    it.putExtra(EXTRA_CART_TYPE,cartType as Parcelable)
                                }
                            }
                    )
                    this@SelectAddressBottomDialog.dismiss()
                }
                // Single-choice items (initialized with checked item)
                .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->

                }.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                })
                .show()
    }

    private fun showPlacesAutocomplete(requestCode: Int){
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext())
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == DELIVERY_LOCATION_REQUEST && resultCode == Activity.RESULT_OK){
            val validPlace = data?.getParcelableExtra<Place>(EXTRA_ENTER_ADDRESS)
            val cartType = requireArguments().getParcelable<CartType>(ARGS_CART_TYPE)
            val intent = Intent().apply {
                putExtra(EXTRA_ENTER_ADDRESS, validPlace)
                if(cartType != null){
                    putExtra(EXTRA_CART_TYPE, cartType as Parcelable)
                }
            }
//            Toast.makeText(requireContext(), "selected ${validPlace!!.name}", Toast.LENGTH_SHORT).show()
            targetFragment?.onActivityResult(
                    targetRequestCode,
                    Activity.RESULT_OK,
                    // PARSE THIS ("yyyy M d")
                    intent
            )
            this@SelectAddressBottomDialog.dismiss()
        }
    }
}