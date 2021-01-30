package com.myoptimind.getexpress.features.customer.food_grocery.selected_store

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.food_grocery.data.Product
import com.myoptimind.getexpress.features.customer.food_grocery.data.ProductCategory
import com.myoptimind.getexpress.features.customer.food_grocery.selected_store.data.ItemPayload
import com.myoptimind.getexpress.features.customer.cart.data.AddOn
import com.myoptimind.getexpress.features.shared.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_product_food.*
import timber.log.Timber


class ProductFoodDialog: BaseDialogFragment() {

    var adapter: AddOnAdapter? = null
    var addOns = ArrayList<AddOn>()

    companion object {

        private const val ARGS_PRODUCT = "args_product"
        private const val ARGS_TRAY_ADD_ON = "args_tray_addon"
        val PRODUCT_DATA_PAYLOAD = "product_data_payload"

        fun newInstance(product: Product, trayAddOn: TrayAddOnAdapter.TrayAddOn? = null): ProductFoodDialog {
            val args = Bundle()
            if(trayAddOn != null){
                args.putParcelable(ARGS_TRAY_ADD_ON, trayAddOn)
            }
            args.putParcelable(ARGS_PRODUCT,product)
            val fragment = ProductFoodDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.dialog_product_food,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ib_close.setOnClickListener {
            this@ProductFoodDialog.dismiss()
        }

        val trayAddOn = arguments?.getParcelable<TrayAddOnAdapter.TrayAddOn>(ARGS_TRAY_ADD_ON)

        if(trayAddOn != null){
            btn_add_to_cart.text = "Update Cart"
        }

        arguments?.getParcelable<Product>(ARGS_PRODUCT).also { product ->
            if(product != null){
                val addons = product.addons.map {
                    ProductCategory(it.key,it.value)
                }

                if(trayAddOn != null){
                    addOns = ArrayList(trayAddOn.addons)
                }

                Timber.d("initial addons: ${addOns}")

                adapter = AddOnAdapter(addons, object: AddOnAdapter.AddonListener{
                    override fun onSelectAddon(product: Product?) {
                        if(product != null){
                            Toast.makeText(requireContext(),"selected ${product.productName} addon",Toast.LENGTH_SHORT).show()
                            addOns.add(AddOn(
                                    product.id,
                                    product.productName,
                                    product.basePrice.toDouble().toString()
                            ))
                        }
                    }

                    override fun onRemoveAddon(product: Product?) {
                        if(product != null){
                            Toast.makeText(requireContext(),"remove ${product.productName} addon",Toast.LENGTH_SHORT).show()
                            addOns.remove(AddOn(
                                    product.id,
                                    product.productName,
                                    product.basePrice.toDouble().toString()
                            ))
                            Timber.d("after remove: ${addOns}")
                        }
                    }
                },addOns)
                rv_available_addons.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
                rv_available_addons.adapter = adapter
                adapter?.notifyDataSetChanged()

                tv_product_name.text = product.productName
//                tv_product_description.text = product.description
                tv_product_price.text = product.basePrice.toDouble().toString()
                var quantity = product.quantity
                if(trayAddOn != null){
                    quantity = trayAddOn.quantity
                    product.cartItemId = trayAddOn.cartItemId
                }
                et_quantity.setText(quantity)
                Glide.with(requireContext())
                        .load(product.image)
                        .into(iv_item_image)
                btn_plus.setOnClickListener { _ ->
                    et_quantity.apply {
                        setText((this.text.toString().toInt() + 1).toString())
                    }
                }
                btn_minus.setOnClickListener { _ ->
                    et_quantity.apply {
                        val qty = this.text.toString().toInt()
                        if(qty > 1){
                            setText((qty - 1).toString())
                        }
                        if(trayAddOn!= null && qty == 1){
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("")
                                .setMessage("Remove this item from the cart?")
                                .setNeutralButton("NO") { dialog, which ->
                                    // Respond to neutral button press
                                }
                                .setPositiveButton("YES") { dialog, which ->
                                    setText((qty - 1).toString())
                                    updateCart(product,addOns)
                                }
                                .show()
                        }
                    }
                }
                btn_add_to_cart.setOnClickListener {
                    updateCart(product,addOns)
                }

            }
        }
    }

    private fun updateCart(product: Product, addOns: ArrayList<AddOn>) {
        Timber.d("on update ${addOns}")
        val updateQuantity = if(product.cartQuantity != null) (product.cartQuantity!!.toInt() + et_quantity.text.toString().toInt()).toString() else et_quantity.text.toString()
        targetFragment?.onActivityResult(
            targetRequestCode,
            Activity.RESULT_OK,
            Intent().putExtra(
                PRODUCT_DATA_PAYLOAD,
                ItemPayload(
                    product.productName,
                    product.id,
                    updateQuantity,
                    et_special_instructions.text.toString(),
                    addOns,
                    product.cartItemId
                )
            )
        )
        this@ProductFoodDialog.dismiss()
    }
}
