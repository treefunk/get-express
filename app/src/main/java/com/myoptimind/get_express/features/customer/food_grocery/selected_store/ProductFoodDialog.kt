package com.myoptimind.get_express.features.customer.food_grocery.selected_store

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.food_grocery.data.Product
import com.myoptimind.get_express.features.customer.food_grocery.data.ProductCategory
import com.myoptimind.get_express.features.customer.food_grocery.selected_store.data.ItemPayload
import com.myoptimind.get_express.features.customer.cart.data.AddOn
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import com.myoptimind.get_express.features.shared.initMultilineEditText
import com.myoptimind.get_express.features.shared.toMoneyFormat
import kotlinx.android.synthetic.main.dialog_product_food.*
import kotlinx.android.synthetic.main.fragment_stores.*
import timber.log.Timber


class ProductFoodDialog: BaseDialogFragment() {

    var adapter: AddOnAdapter? = null
    var addOns = ArrayList<AddOn>()

    companion object {

        private const val ARGS_PRODUCT = "args_product"
        private const val ARGS_TRAY_ADD_ON = "args_tray_addon"
        private const val ARGS_FOR_VIEWING_ONLY = "args_for_viewing_only"
        val PRODUCT_DATA_PAYLOAD = "product_data_payload"

        fun newInstance(product: Product, trayAddOn: TrayAddOnAdapter.TrayAddOn? = null, forViewingOnly: Boolean = false): ProductFoodDialog {
            val args = Bundle()
            if(trayAddOn != null){
                args.putParcelable(ARGS_TRAY_ADD_ON, trayAddOn)
            }
            args.putBoolean(ARGS_FOR_VIEWING_ONLY,forViewingOnly)
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

        et_special_instructions.initMultilineEditText()


        ib_close.setOnClickListener {
            this@ProductFoodDialog.dismiss()
        }

        val trayAddOn = arguments?.getParcelable<TrayAddOnAdapter.TrayAddOn>(ARGS_TRAY_ADD_ON)
        val forViewingOnly = arguments?.getBoolean(ARGS_FOR_VIEWING_ONLY)!!
        val product = arguments?.getParcelable<Product>(ARGS_PRODUCT)!!

        tv_product_name.text = product.productName
        tv_product_price.text = product.basePrice.toMoneyFormat()
        tv_bottom_price.text = product.basePrice.toMoneyFormat()
        Glide.with(requireContext())
            .load(product.image)
            .into(iv_item_image)

        if(forViewingOnly){
            btn_add_to_cart.text = ""
            btn_add_to_cart.isEnabled = false
            btn_plus.visibility = View.GONE
            btn_minus.visibility = View.GONE
            et_quantity.visibility = View.GONE
            label_special_instructions.visibility = View.INVISIBLE
            et_special_instructions.visibility = View.INVISIBLE
        }else{
            if(trayAddOn != null){
                btn_add_to_cart.text = "Update Cart"
            }

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
//                            Toast.makeText(requireContext(),"selected ${product.productName} addon",Toast.LENGTH_SHORT).show()
                        addOns.add(AddOn(
                            product.id,
                            product.productName,
                            product.basePrice.toDouble().toString()
                        ))
                        tv_bottom_price.text = (addOns.sumByDouble { it.basePrice.toDouble() } + product.basePrice.toDouble()).toMoneyFormat()
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
                        (addOns.sumByDouble { it.basePrice.toDouble() } + product.basePrice.toDouble()).toMoneyFormat()
                    }
                }
            }, addOns)
            rv_available_addons.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
            rv_available_addons.adapter = adapter
            adapter?.notifyDataSetChanged()


//                tv_product_description.text = product.description
            var quantity = product.quantity
            if(trayAddOn != null){
                quantity = trayAddOn.quantity
                product.cartItemId = trayAddOn.cartItemId
            }
            et_quantity.setText(quantity)

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
