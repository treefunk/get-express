package com.myoptimind.getexpress.features.customer.food_grocery.selected_store

import ProductCategoryAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.customer.cart.CartViewModel
import com.myoptimind.getexpress.features.customer.food_grocery.StoresViewModel
import com.myoptimind.getexpress.features.customer.food_grocery.data.Product
import com.myoptimind.getexpress.features.customer.food_grocery.data.ProductCategory
import com.myoptimind.getexpress.features.customer.food_grocery.selected_store.data.ItemPayload
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.data.CartType
import com.myoptimind.getexpress.features.shared.data.idToCartType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_selected_customer_request.*
import kotlinx.android.synthetic.main.fragment_selected_store.*
import timber.log.Timber

@AndroidEntryPoint
class SelectedStoreFragment: TitleOnlyFragment() {

    private val args: SelectedStoreFragmentArgs by navArgs()
    private val storeViewModel by activityViewModels<StoresViewModel>()
    private val cartViewModel by activityViewModels<CartViewModel>()


    companion object {

        const val REQUEST_GROCERY_ADD_TO_CART = 101

        const val REQUEST_FOOD_ADD_TO_CART = 201
        const val REQUEST_FOOD_UPDATE_CART = 202
        const val REQUEST_FOOD_TRAY = 222

    }

    override fun getTitle() = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_selected_store,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        storeViewModel.getProductsByStore(
                args.storeId,
                null,
                null
        )
    }

    private fun initObservers() {

        val cartType = args.serviceId.idToCartType()


        storeViewModel.productCategoryResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){

                        var adapter: ProductCategoryAdapter? = null
                        when(cartType){
                            CartType.CAR -> TODO()
                            CartType.GROCERY -> {
                                adapter = ProductCategoryAdapter(ArrayList(),cartType, object: ProductAdapter.ProductListener{
                                    override fun onPressProduct(product: Product) {
                                        product.quantity = "1"
                                                Timber.d("PRODUCT -- ${product}")
                                                val dialogGrocery = ProductGroceryDialog.newInstance(product)
                                                dialogGrocery.setTargetFragment(this@SelectedStoreFragment, REQUEST_GROCERY_ADD_TO_CART)
                                                dialogGrocery.show(parentFragmentManager,"GROCERYDIALOG")
                                            }
                                })
                            }
                            CartType.FOOD -> {
                                adapter = ProductCategoryAdapter(ArrayList(),cartType, object: ProductAdapter.ProductListener{
                                    override fun onPressProduct(product: Product) {
                                        product.quantity = "1"
                                        val sameProducts = cartViewModel.cartItemList!!.filter { product.id == it.productId }
                                        if(cartViewModel.cartItemList != null
                                                && sameProducts.isNotEmpty()
                                                && product.addons.isNotEmpty()
                                        ){
                                            val productsTrayBottomDialog = ProductsTrayBottomDialog.newInstance(product,ArrayList(sameProducts))
                                            productsTrayBottomDialog.setTargetFragment(this@SelectedStoreFragment, REQUEST_FOOD_TRAY)
                                            productsTrayBottomDialog.show(parentFragmentManager,"FOODTRAY")
                                        }else{
                                            Timber.d("PRODUCT -- ${product}")
                                            val dialogGrocery = ProductFoodDialog.newInstance(product)
                                            dialogGrocery.setTargetFragment(this@SelectedStoreFragment, REQUEST_FOOD_ADD_TO_CART)
                                            dialogGrocery.show(parentFragmentManager,"FOODDIALOG")
                                        }
                                    }
                                })
                            }
                            CartType.PABILI -> TODO()
                            CartType.DELIVERY -> TODO()

                        }
                        rv_categories.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL, false)
                        rv_categories.adapter = adapter

                        val productCategories = result.data.data.map {
                            ProductCategory(it.key,it.value)
                        }
                        adapter.productCategories = productCategories
                        adapter.notifyDataSetChanged()

                    }
                }
                is Result.Error -> {

                }
                is Result.HttpError -> {

                }
            }
        }

        cartViewModel.activeStore.observe(viewLifecycleOwner){ activeStore ->
            if(activeStore != null){
                tv_store_name.text = activeStore.name
                tv_store_caption.text = activeStore.locationText
                tv_store_description.text = "30 mins  |  5.4 km" //TODO
                Glide.with(requireContext())
                        .load(activeStore.banner)
                        .centerCrop()
                        .into(iv_store_banner)
                Glide.with(requireContext())
                        .load(activeStore.banner)
                        .centerCrop()
                        .into(iv_store_image)
            }
        }

        cartViewModel.cart.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    if(result.isLoading){
                        Timber.d("loading cart..")
                    }else{
                        Timber.d("done loading cart..")
                    }
                }
                is Result.Success -> {
                    if(result.data != null){
                        val cart = result.data.data
                        val cartType = cart.cartTypeId.idToCartType()
                        cartViewModel.clearCartItemList()


                        when (cartType) {
                            CartType.CAR -> {
                                //
                            }
                            CartType.GROCERY -> {
                                val itemsBasket = cart.initBasketForGrocery()

                                cartViewModel.updateCartItemList(itemsBasket.items.map{ it.toItemPayload() } )

                                if(itemsBasket.items.isNotEmpty()){

                                    group_basket.visibility = View.VISIBLE
                                    tv_basket_label.text = "View Basket (${itemsBasket.totalItems} items)"
                                    tv_basket_total.text = "${itemsBasket.grandTotal}"

                                    box_basket.setOnClickListener {
                                        SelectedStoreFragmentDirections.actionSelectedStoreFragmentToCustomerCartFragment(cart.pickUpLocation.toPlace(),cart.deliveryLocation.toPlace(),cart.notes).also {
                                            findNavController().navigate(it)
                                        }
//                                        findNavController().navigate(R.id.action_selectedStoreFragment_to_customerCartFragment)
                                    }

                                }else{
                                    group_basket.visibility = View.GONE
                                }

                            }
                            CartType.FOOD -> {
                            val itemsBasket = cart.initBasketForFood()

                            cartViewModel.updateCartItemList(itemsBasket.items.map{ it.toItemPayload() } )

                            if(itemsBasket.items.isNotEmpty()){

                                group_basket.visibility = View.VISIBLE
                                tv_basket_label.text = "View Basket (${itemsBasket.totalItems} items)"
                                tv_basket_total.text = "${itemsBasket.grandTotal}"

/*                                box_basket.setOnClickListener {
                                    findNavController().navigate(R.id.action_selectedStoreFragment_to_customerCartFragment)
                                }*/
                                box_basket.setOnClickListener {
                                    SelectedStoreFragmentDirections.actionSelectedStoreFragmentToCustomerCartFragment(
                                            null,null,
                                            notes = cart.notes
                                    ).also {
                                        findNavController().navigate(it)
                                    }
//                                        findNavController().navigate(R.id.action_selectedStoreFragment_to_customerCartFragment)
                                }

                            }else{
                                group_basket.visibility = View.GONE
                            }
                            }
                            CartType.PABILI -> {
                                val pabiliBasket = cart.initBasketForPabili()
                            }
                            CartType.DELIVERY -> {
                                val deliveryBasket = cart.initBasketForDelivery()
                            }
                        }

                        Timber.d("Successfully added item.. to cart ${cart.id}")
                    }
                }
                is Result.Error -> {
                    Timber.d(result.metaResponse.message)
                }
                is Result.HttpError -> {
                    Timber.d(result.error.message)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onactivityresult")
        if(requestCode == REQUEST_GROCERY_ADD_TO_CART && resultCode == Activity.RESULT_OK){
            if(data != null){
                val itemPayload = data.getParcelableExtra<ItemPayload>(ProductFoodDialog.PRODUCT_DATA_PAYLOAD)

                if(itemPayload != null){
                    Toast.makeText(requireContext(),"Added ${itemPayload.productName}.",Toast.LENGTH_SHORT).show()
                    Timber.d("adding item...")
                    var cartItemId: String? = null
                    var quantity = itemPayload.quantity
                    if(cartViewModel.cartItemList != null){
                        Timber.d("cart item list " + cartViewModel.cartItemList)
                        val matchedProduct = cartViewModel.cartItemList!!.filter {
                            it.productId == itemPayload.productId && it.notes == itemPayload.notes
                        }
                        if(
                                matchedProduct.isNotEmpty()
                        ){
                            cartItemId   = matchedProduct[0].cartItemId
                            quantity = (matchedProduct[0].quantity.toInt() + itemPayload.quantity.toInt()).toString()
                        }
                    }
                    cartViewModel.addItemToCart(
                            args.cartId,
                            itemPayload.productId,
                            quantity,
                            itemPayload.notes,
                            cartItemId = cartItemId,
                            addOnIds = itemPayload.addons?.map { it.id }
                    )
                }
            }
        }else if(requestCode == REQUEST_FOOD_ADD_TO_CART && resultCode == Activity.RESULT_OK){
            if(data != null){
                val itemPayload = data.getParcelableExtra<ItemPayload>(ProductFoodDialog.PRODUCT_DATA_PAYLOAD)
                if(itemPayload != null){
                    Toast.makeText(requireContext(),"Added ${itemPayload.productName}.",Toast.LENGTH_SHORT).show()
                    Timber.d("addons: ${itemPayload.addons}")
                    Timber.d("adding item...")
                    if(cartViewModel.cartItemList.isNullOrEmpty().not()){
                        val matchedProduct = cartViewModel.cartItemList!!.filter { payload ->
                            Timber.d("cartItem - ${payload.cartItemId}")
                            Timber.d("compare \n1 - ${itemPayload.addons!!.map { it.id }}\n2 - ${payload.addons!!.map{ it.id }}")
                            payload.productId == itemPayload.productId && itemPayload.addons!!.size == payload.addons!!.size && itemPayload.addons!!.map { it.id }.containsAll(payload.addons!!.map { it.id })
                        }
                        var cartItemId: String? = null
                        var quantity = itemPayload.quantity
                        if(matchedProduct.isNotEmpty()){
                            cartItemId = matchedProduct[0].cartItemId!!
                            quantity = (matchedProduct[0].quantity.toInt() + itemPayload.quantity.toInt()).toString()
                            if(matchedProduct[0].notes.trim().equals(itemPayload.notes.trim(),false).not()){
                                cartItemId = null
                            }
                        }
                            cartViewModel.addItemToCart(
                                    args.cartId,
                                    itemPayload.productId,
                                    quantity,
                                    "",
                                    cartItemId = cartItemId,
                                    addOnIds = itemPayload.addons?.map { it.id }
                            )
                    }else{
                        cartViewModel.addItemToCart(
                                args.cartId,
                                itemPayload.productId,
                                itemPayload.quantity,
                                itemPayload.notes,
                                cartItemId = itemPayload.cartItemId,
                                addOnIds = itemPayload.addons?.map { it.id }
                        )
                    }
                }
            }
        }else if(requestCode == REQUEST_FOOD_UPDATE_CART && resultCode == Activity.RESULT_OK){
            val itemPayload = data?.getParcelableExtra<ItemPayload>(ProductFoodDialog.PRODUCT_DATA_PAYLOAD)
            if(data != null && itemPayload != null ){
                cartViewModel.addItemToCart(
                    args.cartId,
                    itemPayload.productId,
                    itemPayload.quantity,
                    itemPayload.notes,
                    cartItemId = itemPayload.cartItemId,
                    addOnIds = itemPayload.addons?.map { it.id }
                )
            }
        }

    }
}