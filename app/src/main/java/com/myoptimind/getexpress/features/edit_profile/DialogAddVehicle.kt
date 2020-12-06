package com.myoptimind.getexpress.features.edit_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.BaseDialogFragment
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.izNotBlank
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_add_vehicle.*
import timber.log.Timber


@AndroidEntryPoint
class DialogAddVehicle: BaseDialogFragment() {

    private val viewModel by activityViewModels<EditProfileViewModel>()
    
    companion object {
        fun newInstance(): DialogAddVehicle{
            val args = Bundle()
            
            val fragment = DialogAddVehicle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_add_vehicle,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ib_close.setOnClickListener {
            this.dismiss()
        }
        initObservers()
        initClickListeners()
    }

    private fun initClickListeners() {
        btn_submit_for_approval.setOnClickListener {
            if(et_vehicle_type.izNotBlank() && et_vehicle_model.izNotBlank() && et_plate_number.izNotBlank()){
                viewModel.addVehicle(
                        et_vehicle_type.tag.toString(),
                        et_vehicle_model.text.toString(),
                        et_plate_number.text.toString()
                )
            }else{
                Toast.makeText(requireContext(),"Please fill in required fields.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initObservers() {
        viewModel.vehicleTypeList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {
                    //todo
                }
                is Result.Success -> {
                    if(result.data != null){
                        val vehicleListString = result.data.data.map { it.name }
                                val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,vehicleListString)
                                et_vehicle_type.setAdapter(adapter)
                                et_vehicle_type.setOnClickListener {
                                    et_vehicle_type.showDropDown()
                                }
                                et_vehicle_type.setOnItemClickListener { _, _, index, id ->
                                    val selectedVehicle = result.data.data[index]
                                    et_vehicle_type.tag = selectedVehicle.id.toString()
                                    Timber.v("vehicle id selected: ${et_vehicle_type.tag}")
                                    et_vehicle_type.setText(selectedVehicle.name,false)
                                }
                    }
                }
                is Result.Error -> {
                    //todo
                }
                is Result.HttpError -> {
                    //todo
                }
            }

            viewModel.addVehicleResult.observe(viewLifecycleOwner){ result ->
                when (result) {
                    is Result.Progress -> {
                        //todo
                    }
                    is Result.Success -> {
                        Timber.v(result.data?.meta?.message)
                        if(result.data != null){
                            Toast.makeText(requireContext(),result.data.meta.message,Toast.LENGTH_LONG).show()
                        }
                    }
                    is Result.Error -> {
                        Timber.v(result.metaResponse.message)
                    }
                    is Result.HttpError -> {
                        Timber.v(result.error.message)
                    }
                }
            }
        }


    }


}