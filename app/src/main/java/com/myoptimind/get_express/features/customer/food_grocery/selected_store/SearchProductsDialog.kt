package com.myoptimind.get_express.features.customer.food_grocery.selected_store

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.maps.android.SphericalUtil
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.food_grocery.StoresViewModel
import com.myoptimind.get_express.features.customer.food_grocery.data.Store
import com.myoptimind.get_express.features.customer.home.SelectAddressBottomDialog.Companion.EXTRA_ENTER_ADDRESS
import com.myoptimind.get_express.features.edit_profile.ProfileRepository
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.izNotBlank
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_add_address.*
import kotlinx.android.synthetic.main.dialog_search_products.*
import kotlinx.android.synthetic.main.dialog_search_products.ib_close
import timber.log.Timber

@AndroidEntryPoint
class SearchProductsDialog: BaseDialogFragment() {

    private val storeViewModel by activityViewModels<StoresViewModel>()

    companion object {

        private val ARGS_STORE = "args_store"

        fun newInstance(store: Store): SearchProductsDialog {
            val args = Bundle()

            args.putParcelable(ARGS_STORE, store)
            val fragment = SearchProductsDialog ()
            fragment.arguments = args
            return fragment
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_search_products,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val store = requireArguments().getParcelable<Store>(ARGS_STORE)!!


        ib_close.setOnClickListener {
            this@SearchProductsDialog.dismiss()
        }

        storeViewModel.productSearch.observe(viewLifecycleOwner){ search ->
            if(search != null){
                et_search.setText(search)
            }
        }

        storeViewModel.productMinPrice.observe(viewLifecycleOwner){ minPrice ->
            if(minPrice != null){
                et_min_price.setText(minPrice)
            }
        }

        storeViewModel.productMaxPrice.observe(viewLifecycleOwner){ maxPrice ->
            if(maxPrice != null){
                et_max_price.setText(maxPrice)
            }
        }

        label_filter.text = "Search ${store.name}"

        btn_search_product.setOnClickListener {
            val search = et_search.text.toString().trim()
            val minPrice = et_min_price.text.toString()
            val maxPrice = et_max_price.text.toString()
                storeViewModel.updateSearch(
                    if(search.isBlank()) null else search
                )
                storeViewModel.updateMinPrice(
                    if(minPrice.isBlank()) null else minPrice
                )
                storeViewModel.updateMaxPrice(
                    if(maxPrice.isBlank()) null else maxPrice
                )
                storeViewModel.getProductsByStore(store.id)
                this.dismiss()
        }
    }


}