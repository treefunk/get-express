package com.myoptimind.getexpress.features.customer.food_grocery.selected_store

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.food_grocery.data.Product
import com.myoptimind.getexpress.features.customer.food_grocery.selected_store.data.ItemPayload
import com.myoptimind.getexpress.features.shared.BaseDialogFragment
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.dialog_product_grocery.*


class ProductGroceryDialog: BaseDialogFragment() {

    companion object {

        private const val ARGS_PRODUCT = "args_product"
        val PRODUCT_DATA_PAYLOAD = "product_data_payload"

        fun newInstance(product: Product): ProductGroceryDialog {
            val args = Bundle()
            args.putParcelable(ARGS_PRODUCT,product)
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

        val product = arguments?.getParcelable<Product>(ARGS_PRODUCT).also { product ->
            if(product != null){
                tv_product_name.text = product.productName
                tv_product_description.text = product.description
                tv_product_price.text = product.basePrice
                et_quantity.setText(product.quantity)
                Glide.with(requireContext())
                        .load(product.image)
                        .into(iv_item_image)
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

