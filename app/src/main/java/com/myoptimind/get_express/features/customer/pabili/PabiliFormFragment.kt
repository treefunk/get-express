package com.myoptimind.get_express.features.customer.pabili

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.CartViewModel
import com.myoptimind.get_express.features.customer.cart.data.ItemInPabili
import com.myoptimind.get_express.features.customer.food_grocery.selected_store.SelectedStoreFragmentDirections
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog
import com.myoptimind.get_express.features.edit_profile.ProfileViewModel
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.idToCartType
import com.myoptimind.get_express.features.shared.data.toCartStatus
import com.myoptimind.get_express.features.shared.initMultilineEditText
import com.myoptimind.get_express.features.shared.toCartLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_pabili_form.*
import kotlinx.android.synthetic.main.fragment_pabili_form.iv_receiver_icon
import kotlinx.android.synthetic.main.fragment_pabili_form.iv_sender_icon
import kotlinx.android.synthetic.main.fragment_pabili_form.tv_receiver_address
import kotlinx.android.synthetic.main.fragment_pabili_form.tv_receiver_name
import kotlinx.android.synthetic.main.fragment_pabili_form.tv_sender_address
import kotlinx.android.synthetic.main.fragment_pabili_form.tv_sender_name
import timber.log.Timber
import androidx.lifecycle.observe
import com.myoptimind.get_express.features.shared.getSoftInputMode

@AndroidEntryPoint
class PabiliFormFragment: TitleOnlyFragment() {

    private var originalMode : Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalMode = activity?.window?.getSoftInputMode()
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
        originalMode?.let { activity?.window?.setSoftInputMode(it) }

    }
    companion object {
        private const val RECEIVER_ADDRESS_REQUEST = 200
        private const val SENDER_ADDRESS_REQUEST = 300
    }

    private val cartViewModel by activityViewModels<CartViewModel>()
    private val profileViewModel by activityViewModels<ProfileViewModel>()
    private val args by navArgs<PabiliFormFragmentArgs>()
    private lateinit var adapter: PabiliFormAdapter
    private lateinit var itemList: ArrayList<ItemInPabili>

    override fun getTitle() = "Get Pabili"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_pabili_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_delivery_instructions.initMultilineEditText()
        rv_pabili.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        itemList = ArrayList()
        adapter = PabiliFormAdapter(itemList, object: PabiliFormAdapter.PabiliFormListener{
            override fun onRemove(pabiliItem: ItemInPabili, index: Int) {
//                Toast.makeText(requireContext(),"index ${index}",Toast.LENGTH_SHORT).show()
                itemList.removeAt(index)
                adapter.pabiliItemList = itemList
                adapter.notifyItemRemoved(index)
                adapter.notifyItemRangeChanged(index,itemList.size)
//                adapter.notifyDataSetChanged()
                if(adapter.pabiliItemList.size > 0) {
                    Timber.d("list - \n" + adapter.pabiliItemList.map{ it.itemName }.reduce{acc,s -> acc + "\n "+ s})
                }
            }
        })
        rv_pabili.adapter = adapter



        initClickListeners()
        initObservers()
    }

    private fun initObservers() {

        cartViewModel.fromLocation.observe(viewLifecycleOwner){ fromLocation ->
            if(fromLocation != null){
                label_sender_place_holder.visibility = View.INVISIBLE
                tv_sender_name.text = fromLocation.name
                tv_sender_address.text = fromLocation.address
            }
        }

        cartViewModel.toLocation.observe(viewLifecycleOwner){ toLocation ->
            if(toLocation != null){
                label_receiver_place_holder.visibility = View.INVISIBLE
                tv_receiver_name.text = toLocation.name
                tv_receiver_address.text = toLocation.address
            }
        }

        profileViewModel.getCustomerProfile.observe(viewLifecycleOwner){ result ->
            if(result is Result.Success && result.data != null){
                val myAddresses = result.data.data.addresses
                tv_sender_address.setOnClickListener {
                    showAddressSelection(myAddresses,SENDER_ADDRESS_REQUEST)
                }
                tv_sender_name.setOnClickListener {
                    showAddressSelection(myAddresses,SENDER_ADDRESS_REQUEST)
                }
                iv_sender_icon.setOnClickListener {
                    showAddressSelection(myAddresses,SENDER_ADDRESS_REQUEST)
                }

                tv_receiver_address.setOnClickListener {
                    showAddressSelection(myAddresses, RECEIVER_ADDRESS_REQUEST)
                }

                tv_receiver_name.setOnClickListener {
                    showAddressSelection(myAddresses, RECEIVER_ADDRESS_REQUEST)
                }

                iv_receiver_icon.setOnClickListener {
                    showAddressSelection(myAddresses, RECEIVER_ADDRESS_REQUEST)
                }
            }
        }

        cartViewModel.cart.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
                        val cart = result.data.data


                        if (result.data.meta.code.equals("ok2")) {
                            if (result.data != null && result.data.meta.status == 200) {
                                Timber.v("pre finalizing...")
                                PabiliFormFragmentDirections.actionPabiliFormFragmentToCustomerCartFragment().also {
                                        findNavController().navigate(it)
                                }
                                result.data.meta.code = "ok"
                                cartViewModel.setCartInfo(result)
                            }
                            return@observe
                        }

                        cartViewModel.cartId = cart.id
                        val cartType = cart.cartTypeId.idToCartType()
                        val cartStatus = cart.status.toCartStatus()
                        val basket = cart.initBasketForPabili()
//                        adapter.pabiliItemList = ArrayList(basket.items)
                        et_est_total_amount.setText(basket.estimateTotalWithoutDeliveryFee)

/*                        if (cart.pickUpLocation.addressText.isBlank().not()) {
                            cartViewModel.updateFromLocation(cart.pickUpLocation.toPlace())
                        }
                        if (cart.deliveryLocation.addressText.isBlank().not()) {
                            cartViewModel.updateToLocation(cart.deliveryLocation.toPlace())
                        }*/
                        itemList.clear()
                        if (cartViewModel.pabiliItemList.value.isNullOrEmpty()) {
                            itemList.addAll(basket.items)
                        } else {
                            itemList.addAll(cartViewModel.pabiliItemList.value!!)
                        }
                        if (basket.items.size < 5) {
                            var y = basket.items.size
                            while (y < 5) {
                                itemList.add(ItemInPabili())
                                y++
                            }
                        }
                        adapter.pabiliItemList = itemList
                        adapter.notifyDataSetChanged()

                        btn_get_pabili.setOnClickListener {
                            if (tv_sender_address.text.toString().trim()
                                    .isEmpty() || tv_receiver_address.text.toString().trim()
                                    .isEmpty()
                            ) {
                                Snackbar.make(
                                    requireView(),
                                    "Please select an address.",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }

                            if (et_est_total_amount.text.toString().isEmpty()) {
                                Snackbar.make(
                                    requireView(),
                                    "Please enter estimate total amount.",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            } else {
                                if (et_est_total_amount.text.toString().trim().toDouble() > 2000) {
                                    Snackbar.make(
                                        requireView(),
                                        "Estimate total amount should not exceed 2,000.00",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                    return@setOnClickListener
                                }
                            }


                            val list = if (itemList.isNotEmpty())
                                itemList.map {
                                    ItemInPabili(
                                        it.itemName,
                                        if (it.quantity.isNotEmpty()) it.quantity.toInt()
                                            .toString() else ""
                                    )
                                }.filter {
                                    if (it.itemName.trim().isNotEmpty() &&
                                        (it.quantity.trim().isEmpty())
                                    ) {
                                        Snackbar.make(
                                            requireView(),
                                            "Please input a quantity for ${it.itemName}",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        return@setOnClickListener
                                    }
                                    if (it.quantity.trim().isNotEmpty() && it.quantity.trim()
                                            .toInt() <= 0
                                    ) {
                                        Snackbar.make(
                                            requireView(),
                                            "Please input a valid quantity for ${it.itemName}",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        return@setOnClickListener
                                    }
                                    it.itemName.trim().isNotEmpty() && it.quantity.trim()
                                        .isNotEmpty()
                                }
                            else
                                ArrayList<ItemInPabili>()

                            if (list.isEmpty()) {
                                Snackbar.make(
                                    requireView(),
                                    "Please add an item to the cart",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }


                            cartViewModel.createPabili(
                                cart.id,
                                list,
                                et_est_total_amount.text.toString().trim().toDouble()
                            )


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


        cartViewModel.pabiliResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> { }
                is Result.Success -> {
                    if(result.data != null){
                        Timber.d("result -> %s", result)
                        if(result.data.meta.code.equals("ok")){
                            val cart = result.data.data
                            cartViewModel.setCartInfo(result)
                            cartViewModel.finalizeCart(
                                cart.id,
                                cart.notes,
                                cartViewModel.fromLocation.value!!.toCartLocation(),
                                cartViewModel.toLocation.value!!.toCartLocation(),
                                "COD",
                                false
                            )
                        }
                    }
                }
                is Result.Error -> {
                    Timber.d("result -> %s", result.metaResponse.message)

                }
                is Result.HttpError -> {
                    Timber.d("result -> %s", result.error.message)

                }
            }
        }
    }

    private fun initClickListeners() {
        btn_add_more_items.setOnClickListener {

            itemList.add(ItemInPabili())
            adapter.pabiliItemList = itemList
            adapter.notifyItemInserted(adapter.itemCount - 1)
            Timber.d("list - \n" + adapter.pabiliItemList.map{ it.itemName }.reduce{acc,s -> acc + "\n "+ s})

        }

/*        tv_sender_address.setOnClickListener {
            showPlacesAutocomplete(SENDER_ADDRESS_REQUEST)
        }
        tv_sender_name.setOnClickListener {
            showPlacesAutocomplete(SENDER_ADDRESS_REQUEST)
        }
        iv_sender_icon.setOnClickListener {
            showPlacesAutocomplete(SENDER_ADDRESS_REQUEST)
        }

        tv_receiver_address.setOnClickListener {
            showPlacesAutocomplete(RECEIVER_ADDRESS_REQUEST)
        }

        tv_receiver_name.setOnClickListener {
            showPlacesAutocomplete(RECEIVER_ADDRESS_REQUEST)
        }

        iv_receiver_icon.setOnClickListener {
            showPlacesAutocomplete(RECEIVER_ADDRESS_REQUEST)
        }*/
    }


    private fun showPlacesAutocomplete(requestCode: Int){
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext())
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RECEIVER_ADDRESS_REQUEST || requestCode == SENDER_ADDRESS_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = it.getParcelableExtra<Place>(SelectAddressBottomDialog.EXTRA_ENTER_ADDRESS)!!
                        Timber.d("Place: ${place.name}, ${place.id}")
                        if (requestCode == SENDER_ADDRESS_REQUEST) {
                            tv_sender_name.text = place.name
                            tv_sender_address.text = place.address
                            cartViewModel.updateFromLocation(place)
                        }
                        if (requestCode == RECEIVER_ADDRESS_REQUEST) {
                            tv_receiver_name.text = place.name
                            tv_receiver_address.text = place.address
                            cartViewModel.updateToLocation(place)
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Timber.d(status.statusMessage)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}