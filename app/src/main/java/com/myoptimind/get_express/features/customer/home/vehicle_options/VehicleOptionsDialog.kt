package com.myoptimind.get_express.features.customer.home.vehicle_options

import android.app.Activity
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.home.vehicle_options.data.VehicleOption
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.CartType
import com.myoptimind.get_express.features.shared.data.idToCartType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_vehicle_options.*
import timber.log.Timber
import kotlin.math.floor
import kotlin.random.Random

@AndroidEntryPoint
class VehicleOptionsDialog: BaseDialogFragment() {

    private val viewModel by viewModels<VehicleOptionsViewModel>()

    companion object {

        private const val ARGS_CART_TYPE_ID = "args_cart_type_id"
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        const val EXTRA_SERVICE_ID = "extra_service_id"

        fun newInstance(cartType: CartType): VehicleOptionsDialog {
            val args = Bundle()
            args.putString(ARGS_CART_TYPE_ID,cartType.id)
            val fragment = VehicleOptionsDialog ()
            fragment.arguments = args
            return fragment
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_vehicle_options,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_vehicle_options.layoutManager = GridLayoutManager(requireContext(),2,RecyclerView.VERTICAL,false)

        val cartType = arguments?.getString(ARGS_CART_TYPE_ID)?.idToCartType().also {
            if(it != null){
                label_options.text = "${it.label} Options"
                viewModel.getVehicleOptions(it.id)
            }
        }
        val vehicleOptionsAdapter = VehicleOptionsAdapter(ArrayList(), object: VehicleOptionsAdapter.VehicleOptionListener {
            override fun onPressed(vehicleType: VehicleOption, index: Int) {
                val intent = Intent().apply {
                    putExtra(EXTRA_VEHICLE_ID,vehicleType.vehicleId)
                    putExtra(EXTRA_SERVICE_ID,cartType?.id)
                }
                targetFragment?.onActivityResult(targetRequestCode,Activity.RESULT_OK, intent)
                this@VehicleOptionsDialog.dismiss()
            }
        })

        rv_vehicle_options.adapter = vehicleOptionsAdapter



        ib_close.setOnClickListener {
            this.dismiss()
        }

        viewModel.vehicleOptionsResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    if(result.isLoading){
                        view_loading_vehicle_options.visibility = View.VISIBLE
                    }else{
                        view_loading_vehicle_options.visibility = View.GONE
                    }
                }
                is Result.Success -> {
                    if(result.data != null){
                        val vehicleOptions = result.data.data
/*                        val vehicleOptions = ArrayList<VehicleOption>() // TEST
                        for(i in 1..Random.nextInt(3,7)){
                            vehicleOptions.add(VehicleOption("1","vehicle ${i.toString()}","http://getapp.betaprojex.com/uploads/vehicles/motorcycle-icon.png"))
                        }*/
                        vehicleOptionsAdapter.vehicleTypes = vehicleOptions
                        if(vehicleOptions.size % 2 != 0){
                            rv_vehicle_options.layoutManager = GridLayoutManager(requireContext(), if(vehicleOptions.size < 5) vehicleOptions.size % 2 else 2,RecyclerView.VERTICAL,false)
                        }
                        vehicleOptionsAdapter.notifyDataSetChanged()
                    }
                }
                is Result.Error -> {
                    Timber.e(result.metaResponse.message)
                }
                is Result.HttpError -> {
                    Timber.e(result.error.message)
                }
            }
        }


    }
}