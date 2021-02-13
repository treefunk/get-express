package com.myoptimind.get_express.features.customer.food_grocery.selected_store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.food_grocery.data.Product
import com.myoptimind.get_express.features.customer.food_grocery.selected_store.data.ItemPayload
import kotlinx.android.synthetic.main.dialog_products_tray.*
import timber.log.Timber


class ProductsTrayBottomDialog: BottomSheetDialogFragment() {

    var adapter: TrayAddOnAdapter? = null

    companion object {
        private const val ARGS_PRODUCT = "args_product"
        private const val ARGS_ITEM_PAYLOAD_LIST = "args_item_payload_list"

        fun newInstance(product: Product, itemPayloads: ArrayList<ItemPayload>): ProductsTrayBottomDialog {
            val args = Bundle()
            args.putParcelable(ARGS_PRODUCT,product)
            args.putParcelableArrayList(ARGS_ITEM_PAYLOAD_LIST,itemPayloads)
            val fragment = ProductsTrayBottomDialog ()
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_products_tray,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = requireArguments().getParcelable<Product>(ARGS_PRODUCT)
        val itemPayloads = requireArguments().getParcelableArrayList<ItemPayload>(ARGS_ITEM_PAYLOAD_LIST)

        Timber.d("product - ${product}")
        Timber.d("itemPayloads - ${itemPayloads}")

        if(product != null && itemPayloads != null){

            tv_product_name.text = product.productName
            tv_product_price.text = product.basePrice
            rv_tray_addons.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
            adapter = TrayAddOnAdapter(itemPayloads.map {
                   val description = if(it.addons!!.isNotEmpty()) {
                       it.addons?.map{ addon ->
                           addon.productName
                       }?.reduce{ acc, s ->
                           "$s, $acc"
                       }!!
                   }else{
                       "None"
                   }
                TrayAddOnAdapter.TrayAddOn(
                        it!!.cartItemId!!,
                        it.quantity,
                        description,
                        it.notes,
                        product.basePrice,
                        addons = it.addons
                )
            }, object: TrayAddOnAdapter.AddOnTrayListener {
                override fun onSelectAddon(trayAddOn: TrayAddOnAdapter.TrayAddOn) {
                    val dialogGrocery = ProductFoodDialog.newInstance(product,trayAddOn)
                    dialogGrocery.setTargetFragment(this@ProductsTrayBottomDialog.targetFragment, SelectedStoreFragment.REQUEST_FOOD_UPDATE_CART)
                    dialogGrocery.show(parentFragmentManager,"FOODDIALOG")
                    this@ProductsTrayBottomDialog.dismiss()
                }
            })
            rv_tray_addons.adapter = adapter
            adapter?.notifyDataSetChanged()

            btn_make_another_order.setOnClickListener {
                val dialogGrocery = ProductFoodDialog.newInstance(product)
                dialogGrocery.setTargetFragment(this.targetFragment, SelectedStoreFragment.REQUEST_FOOD_ADD_TO_CART)
                dialogGrocery.show(parentFragmentManager,"FOODDIALOG")
                this.dismiss()
            }
        }

    }
}