package com.myoptimind.getexpress.features.customer.food_grocery.selected_store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.food_grocery.data.Product
import com.myoptimind.getexpress.features.customer.food_grocery.data.ProductCategory
import com.myoptimind.getexpress.features.customer.cart.data.AddOn
import kotlinx.android.synthetic.main.item_form_addon.view.*

class AddOnAdapter constructor(
        var addons: List<ProductCategory>,
        val listener: AddonListener? = null,
        var selected: List<AddOn>? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AddOnFieldViewHolder(inflater.inflate(R.layout.item_form_addon,parent,false),listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return (holder as AddOnFieldViewHolder).bind(addons[position],position)
    }

    override fun getItemCount() = addons.size

    inner class AddOnFieldViewHolder constructor(
            private val itemView: View,
            private val listener: AddonListener?
    ) : RecyclerView.ViewHolder(itemView) {

        var lastSelected: Product? = null

        fun bind (product: ProductCategory, position: Int) {
            itemView.label_addon_name.text = "Select ${product.categoryName}"
            val addOnChoices = arrayOf("None") + product.products.map { it.productName }
            val choices = ArrayAdapter(itemView.context,android.R.layout.simple_dropdown_item_1line,addOnChoices)
            itemView.et_addon_field.setAdapter(choices)
            if(selected != null){
                product.products.map {
                    val choice = product.products.filter{ ch ->
                        selected!!.map { it.id }.contains(ch.id)
                    }
                    if(choice.isNotEmpty()){
                        itemView.et_addon_field.setText(choice[0].productName,false)
                        choice[0].basePrice.toDouble().toFloat()
                        lastSelected = choice[0]
                    }else{
                        itemView.et_addon_field.setText(addOnChoices[0],false)
                    }
                }
            }
            itemView.et_addon_field.setOnClickListener {
                (it as AutoCompleteTextView).showDropDown()
            }
            itemView.et_addon_field.setOnItemClickListener { _, _, position, _ ->

                val selectedProduct = if(position != 0) product.products[position - 1] else null
                if(lastSelected != null && lastSelected?.id != selectedProduct?.id ){
                    listener?.onRemoveAddon(lastSelected)
                }
                if(lastSelected == null || selectedProduct != lastSelected){
                    listener?.onSelectAddon(selectedProduct)
                }
                lastSelected = selectedProduct
            }
        }
    }

    interface AddonListener {
        fun onSelectAddon(product: Product?)
        fun onRemoveAddon(product: Product?)
    }
}