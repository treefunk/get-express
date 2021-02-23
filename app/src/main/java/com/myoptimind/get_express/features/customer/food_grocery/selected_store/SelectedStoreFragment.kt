package com.myoptimind.get_express.features.customer.food_grocery.selected_store

import ProductCategoryAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.CartViewModel
import com.myoptimind.get_express.features.customer.cart.data.CartLocation
import com.myoptimind.get_express.features.customer.food_grocery.StoresViewModel
import com.myoptimind.get_express.features.customer.food_grocery.data.Product
import com.myoptimind.get_express.features.customer.food_grocery.data.ProductCategory
import com.myoptimind.get_express.features.customer.food_grocery.selected_store.data.ItemPayload
import com.myoptimind.get_express.features.rider.customer_requests_list.data.DeliveryLocation
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.CartType
import com.myoptimind.get_express.features.shared.data.idToCartType
import com.myoptimind.get_express.features.shared.toCartLocation
import com.myoptimind.get_express.features.shared.toMoneyFormat
import dagger.hilt.android.AndroidEntryPoint
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
        val v = inflater.inflate(R.layout.fragment_selected_store,container,false)
        v.findViewById<RecyclerView>(R.id.rv_categories).visibility = View.GONE
        return inflater.inflate(R.layout.fragment_selected_store,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        storeViewModel.clearFilters()
        storeViewModel.getProductsByStore(
                args.storeId
        )
        if(args.forViewingOnly){
            cartViewModel.getStoreById(args.storeId)
            group_basket.visibility = View.GONE
        }

        storeViewModel.getCategoriesByStore(args.storeId)
    }

    private fun initObservers() {

        val cartType = args.serviceId.idToCartType()


        storeViewModel.storeCategories.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        val list = ArrayList(listOf("All") + result.data.data)
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, list)
                        et_food_category.setAdapter(adapter)
                        et_food_category.setOnClickListener {
                            et_food_category.showDropDown()
                        }
                        et_food_category.setOnItemClickListener { _, _, index, id ->
                            var selectedCategory = list[index]
                            selectedCategory = if(selectedCategory == "All") null else selectedCategory
                            et_food_category.tag = selectedCategory?.toString()
                            if(selectedCategory.isNullOrBlank().not()){
                                if(selectedCategory != "All"){
                                    et_food_category.setText(selectedCategory, false)
                                }else{
                                    et_food_category.setText("", false)
                                }
                            }
                            storeViewModel.updateCategory(et_food_category.text.toString().trim())
                            storeViewModel.getProductsByStore(args.storeId)
                        }
                    }
                }
                is Result.Error -> Timber.e(result.metaResponse.message)
                is Result.HttpError -> Timber.e(result.error.message)
            }
        }

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
                                adapter = ProductCategoryAdapter(ArrayList(),cartType,args.forViewingOnly, object: ProductAdapter.ProductListener{
                                    override fun onPressProduct(product: Product, isInstant: Boolean) {
                                        if(product.isOutStock){
                                            Snackbar.make(requireView(),"This item is out of stock.", Snackbar.LENGTH_SHORT).show()
                                            return
                                        }
                                        product.quantity = "1"
                                                Timber.d("PRODUCT -- ${product}")
                                                val dialogGrocery = ProductGroceryDialog.newInstance(product)
                                                dialogGrocery.setTargetFragment(this@SelectedStoreFragment, REQUEST_GROCERY_ADD_TO_CART)
                                                dialogGrocery.show(parentFragmentManager,"GROCERYDIALOG")
                                            }
                                })
                            }
                            CartType.FOOD -> {

                                adapter = ProductCategoryAdapter(ArrayList(), cartType, args.forViewingOnly, object: ProductAdapter.ProductListener{
                                    override fun onPressProduct(product: Product, isInstant: Boolean) {
                                        val dialogGrocery = ProductFoodDialog.newInstance(product, forViewingOnly = args.forViewingOnly)
                                        dialogGrocery.setTargetFragment(this@SelectedStoreFragment, REQUEST_FOOD_ADD_TO_CART)

                                        if(args.forViewingOnly){
                                            dialogGrocery.show(parentFragmentManager,"FOODDIALOG")
                                            return
                                        }else{
                                            if(product.isOutStock){
                                                Snackbar.make(requireView(),"This item is out of stock.", Snackbar.LENGTH_SHORT).show()
                                                return
                                            }
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
                                                dialogGrocery.show(parentFragmentManager,"FOODDIALOG")
                                            }
                                        }

                                    }
                                })

                                group_select_category.visibility = View.VISIBLE
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

                        if(productCategories.isEmpty()){
                            label_no_results_found.visibility = View.VISIBLE
                            if(cartType == CartType.FOOD){
                                group_select_category.visibility = View.GONE
                            }
                        }else{
                            label_no_results_found.visibility = View.GONE
                            if(cartType == CartType.FOOD){
                                group_select_category.visibility = View.VISIBLE
                            }
                        }

                        adapter.notifyDataSetChanged()
                        rv_categories.visibility = View.VISIBLE
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
                setNewTitle(activeStore.name)
                tv_store_caption.text = activeStore.category
//                tv_store_description.text = "30 mins  |  5.4 km" //TODO
                Glide.with(requireContext())
                        .load(activeStore.banner)
                        .centerCrop()
                        .into(iv_store_banner)
                Glide.with(requireContext())
                        .load(activeStore.image)
                        .centerCrop()
                        .into(iv_store_image)

                ib_store_info.setOnClickListener {
                    AboutStoreDialog.newInstance(activeStore).show(parentFragmentManager,"ABOUT_TAG")
                }

                ib_store_search.setOnClickListener {
                    SearchProductsDialog.newInstance(activeStore).show(parentFragmentManager,"SEARCH_PRODUCT")
                }
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

                        if(result.data.meta.code.equals("ok2") && result.data.meta.status == 200){
                            if(result.data != null && result.data.meta.status == 200){
                                Timber.v("pre finalizing...")
                                SelectedStoreFragmentDirections.actionSelectedStoreFragmentToCustomerCartFragment(result.data.data.notes).also {
                                    findNavController().navigate(it)
                                }
                                result.data.meta.code = "ok"
                                cartViewModel.setCartInfo(result)
                            }
                            return@observe
                        }

                        val cartType = cart.cartTypeId.idToCartType()
                        cartViewModel.clearCartItemList()


                        when (cartType) {
                            CartType.CAR -> {
                                //
                            }
                            CartType.GROCERY -> {
                                val itemsBasket = cart.initBasketForGrocery()

                                cartViewModel.updateCartItemList(itemsBasket.items.map{ it.toItemPayload() } )


                                cartViewModel.shouldEmptyCartFirst = (cart.partnerId != "" && cart.partnerId != args.storeId)

                                if(itemsBasket.items.isNotEmpty() && (cart.partnerId != "" && cart.partnerId == args.storeId) && args.forViewingOnly.not()){

                                    group_basket.visibility = View.VISIBLE
                                    tv_basket_label.text = "View Basket (${itemsBasket.totalItems} items)"
                                    tv_basket_total.text = itemsBasket.grandTotal.toMoneyFormat()


                                    box_basket.setOnClickListener {

                                        cartViewModel.finalizeCart(
                                            cart.id,
                                            cart.notes,
                                            cartViewModel.fromLocation.value!!.toCartLocation(),
                                            cartViewModel.toLocation.value!!.toCartLocation(),
                                            "COD",
                                            false
                                        )
//                                        findNavController().navigate(R.id.action_selectedStoreFragment_to_customerCartFragment)
                                    }

                                }else{
                                    group_basket.visibility = View.GONE
                                }

                            }
                            CartType.FOOD -> {
                            val itemsBasket = cart.initBasketForFood()

                            cartViewModel.updateCartItemList(itemsBasket.items.map{ it.toItemPayload() } )

                                cartViewModel.shouldEmptyCartFirst = (cart.partnerId != "" && cart.partnerId != args.storeId)

                            if(itemsBasket.items.isNotEmpty()  && (cart.partnerId != "" && cart.partnerId == args.storeId) && args.forViewingOnly.not()){

                                group_basket.visibility = View.VISIBLE
                                tv_basket_label.text = "View Basket (${itemsBasket.totalItems} items)"
                                tv_basket_total.text = "${itemsBasket.grandTotal.toMoneyFormat()}"

/*                                box_basket.setOnClickListener {
                                    findNavController().navigate(R.id.action_selectedStoreFragment_to_customerCartFragment)
                                }*/
                                box_basket.setOnClickListener {
                                    cartViewModel.finalizeCart(
                                        cart.id,
                                        cart.notes,
                                        cartViewModel.fromLocation.value!!.toCartLocation(),
                                        cartViewModel.toLocation.value!!.toCartLocation(),
                                        "COD",
                                        false
                                    )
/*                                    SelectedStoreFragmentDirections.actionSelectedStoreFragmentToCustomerCartFragment(
                                            notes = cart.notes
                                    ).also {
                                        findNavController().navigate(it)
                                    }*/
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
                    val errorMeta = result.metaResponse
                    if(errorMeta.status == 400){
                        Snackbar.make(requireView(),errorMeta.message, Snackbar.LENGTH_LONG).show()
                    }
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
//                    Toast.makeText(requireContext(),"Added ${itemPayload.productName}.",Toast.LENGTH_SHORT).show()
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

/*                    cartViewModel.addItemToCart(
                            args.cartId,
                            itemPayload.productId,
                            quantity,
                            itemPayload.notes,
                            cartItemId = cartItemId,
                            addOnIds = itemPayload.addons?.map { it.id }
                    )*/

                    addItemToCart(
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
//                    Toast.makeText(requireContext(),"Added ${itemPayload.productName}.",Toast.LENGTH_SHORT).show()
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
                            addItemToCart(
                                args.cartId,
                                itemPayload.productId,
                                quantity,
                                "",
                                cartItemId = cartItemId,
                                addOnIds = itemPayload.addons?.map { it.id }
                            )

                    }else{
                        addItemToCart(
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
                addItemToCart(
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

    private fun addItemToCart(
        cartId: String,
        productId: String,
        quantity: String,
        notes: String,
        addOnIds: List<String>? = null,
        cartItemId: String? = null
    ){
        if(cartViewModel.shouldEmptyCartFirst){
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Warning!")
                .setMessage(requireContext().getString(R.string.label_remove_cart_warning))
                .setNeutralButton("NO") { dialog, which ->
                    // Respond to neutral button press
                }
                .setPositiveButton("YES") { dialog, which ->
                    cartViewModel.emptyThenAddItemToCart(
                        cartId, productId, quantity, notes, addOnIds, cartItemId
                    )
                }
                .show()
        }else{
            cartViewModel.addItemToCart(
                cartId, productId, quantity, notes, addOnIds, cartItemId
            )
        }
    }
}