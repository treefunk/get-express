package com.myoptimind.get_express.features.customer.food_grocery.selected_store

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.food_grocery.data.Product
import com.myoptimind.get_express.features.customer.food_grocery.selected_store.data.ItemPayload
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import com.myoptimind.get_express.features.shared.initMultilineEditText
import com.myoptimind.get_express.features.shared.toMoneyFormat
import kotlinx.android.synthetic.main.dialog_product_grocery.*


class ProductGroceryDialog: BaseDialogFragment() {

    companion object {

        private const val ARGS_PRODUCT = "args_product"
        const val PRODUCT_DATA_PAYLOAD = "product_data_payload"
        private const val ARGS_FOR_VIEWING_ONLY = "args_for_viewing_only"


        fun newInstance(product: Product, forViewingOnly: Boolean = false): ProductGroceryDialog {
            val args = Bundle()
            args.putParcelable(ARGS_PRODUCT,product)
            args.putBoolean(ARGS_FOR_VIEWING_ONLY,forViewingOnly)
            val fragment = ProductGroceryDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_product_grocery,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_notes_to_driver.initMultilineEditText()
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
            }
        }
        ib_close.setOnClickListener {
            this@ProductGroceryDialog.dismiss()
        }

        val forViewingOnly = arguments?.getBoolean(ARGS_FOR_VIEWING_ONLY)!!

        val product = arguments?.getParcelable<Product>(ARGS_PRODUCT).also { product ->
            if(product != null){


                tv_product_name.text = product.productName
                tv_product_description.text = product.description
                tv_product_price.text = product.basePrice.toMoneyFormat()

                Glide.with(requireContext())
                    .load(product.image)
                    .into(iv_item_image)

                if(forViewingOnly){
                    et_quantity.visibility = View.INVISIBLE
                    btn_add_to_cart.isEnabled = false
                    btn_add_to_cart.text = ""
                    label_notes_to_rider.visibility = View.INVISIBLE
                    et_notes_to_driver.visibility = View.INVISIBLE
                    btn_plus.visibility = View.INVISIBLE
                    btn_minus.visibility = View.INVISIBLE
                }else{
                    et_quantity.setText(product.quantity)

                    btn_add_to_cart.setOnClickListener {
                        val quantity = if(product.cartQuantity != null) (product.cartQuantity!!.toInt() + et_quantity.text.toString().toInt()).toString() else et_quantity.text.toString()
                        targetFragment?.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(
                                PRODUCT_DATA_PAYLOAD,
                                ItemPayload(
                                    product.productName,
                                    product.id,
                                    quantity,
                                    et_notes_to_driver.text.toString(),
                                    null,
                                    product.cartItemId
                                )
                            )
                        )
                        this@ProductGroceryDialog.dismiss()
                    }
                }




            }
        }
    }
}

