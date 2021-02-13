
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.food_grocery.data.ProductCategory
import com.myoptimind.get_express.features.customer.food_grocery.selected_store.ProductAdapter
import com.myoptimind.get_express.features.shared.data.CartType
import kotlinx.android.synthetic.main.item_store_category.view.*

class ProductCategoryAdapter constructor(
        var productCategories: List<ProductCategory>,
        var cartType: CartType,
        val listener: ProductAdapter.ProductListener? = null
) : RecyclerView.Adapter<ProductCategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_store_category, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productCategories[position],position)
    }

    override fun getItemCount() = productCategories.size

    inner class ViewHolder constructor(
            private val itemView: View,
            private val listener: ProductAdapter.ProductListener?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind (productCategory: ProductCategory, position: Int) {
            when(cartType){
                CartType.GROCERY -> {
                    itemView.tv_store_name.text = productCategory.categoryName
                    val productAdapter = ProductAdapter(productCategory.products,cartType,listener)
                    itemView.rv_store_items.layoutManager = GridLayoutManager(itemView.context,2,RecyclerView.VERTICAL,false)
                    itemView.rv_store_items.adapter = productAdapter
                    productAdapter.notifyDataSetChanged()
                }
                CartType.FOOD -> {
                    itemView.tv_store_name.text = productCategory.categoryName
                    val productAdapter = ProductAdapter(productCategory.products,cartType,listener)
                    itemView.rv_store_items.layoutManager = LinearLayoutManager(itemView.context,RecyclerView.VERTICAL,false)
                    itemView.rv_store_items.adapter = productAdapter
                    productAdapter.notifyDataSetChanged()
                }
                else -> throw Exception("Cart type not implemented")
            }
        }
    }
}