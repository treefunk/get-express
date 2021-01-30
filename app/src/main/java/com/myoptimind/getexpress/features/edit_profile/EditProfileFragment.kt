package com.myoptimind.getexpress.features.edit_profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.getexpress.BaseFragment
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.login.DialogViewUrl
import com.myoptimind.getexpress.features.login.data.Address
import com.myoptimind.getexpress.features.login.data.RiderVehicle
import com.myoptimind.getexpress.features.login.data.User
import com.myoptimind.getexpress.features.login.data.UserType
import com.myoptimind.getexpress.features.rider.rider_dashboard.RiderDashboardFragment
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.ProfileFragment
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.et_fullname
import kotlinx.android.synthetic.main.fragment_edit_profile.et_location

import kotlinx.android.synthetic.main.fragment_sign_up_rider.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class EditProfileFragment: TitleOnlyFragment() {

    companion object {
        const val REQUEST_ADD_VEHICLE = 111
    }

    private val viewModel by activityViewModels<EditProfileViewModel>()
    override fun getTitle() = "Edit Profile"


    private lateinit var myVehicleAdapter: RiderVehicleAdapter
    private lateinit var myAddressesAdapter: CustomerAddressAdapter

    @Inject
    lateinit var appSharedPref: AppSharedPref

    var profileFragment: ProfileFragment? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile,container,false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(appSharedPref.getUserType() == UserType.RIDER){
            profileFragment = (requireParentFragment().requireParentFragment() as RiderDashboardFragment)
        }else{
            profileFragment = (requireParentFragment().requireParentFragment() as CustomerProfileFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initClickListeners()
    }

    private fun initClickListeners() {
        btn_save_edits_rider.setOnClickListener {
            updateProfile()
        }

        btn_save_edits_customer.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile(){
        if(et_new_password.text.toString().isNotBlank() &&
                (!et_new_password.text.toString().equals(et_retype_password.text.toString()))
        ){
            Toast.makeText(requireContext(),"Password does not match",Toast.LENGTH_LONG).show()
            return
        }
        viewModel.updateProfile(
                et_fullname.text.toString().ifBlank { null },
                et_email.text.toString().ifBlank { null },
                et_mobile.text.toString().ifBlank { null },
                et_birthdate.text.toString().ifBlank { null },
                et_location.text.toString().ifBlank { null },
                et_new_password.text.toString().ifBlank { null }
        )
    }



    override fun onResume() {
        super.onResume()
        when(appSharedPref.getUserType()){
            UserType.CUSTOMER -> {
                viewModel.getCustomerProfile()
            }
            UserType.RIDER -> {
                viewModel.getRiderProfile()
            }
        }
    }

    private fun initObservers() {
        when(appSharedPref.getUserType()){
            UserType.CUSTOMER -> {
                initCustomer()
            }
            UserType.RIDER -> {
                initRider()
            }
        }

        viewModel.updateProfileResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                 //todo
                }
                is Result.Success -> {
                    if(result.data != null){
                        Toast.makeText(requireContext(),result.data.meta.message,Toast.LENGTH_LONG).show()

                        profileFragment?.updateUserLabels(result.data.data)
//                        val user = result.data.data
//                        appSharedPref.storeLoginCredentials(
//                                user.id,
//                                appSharedPref.getUserType().label,
//                                user.profilePicture,z
//                                user.fullName
//                        )
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

    private fun initRider() {

        myVehicleAdapter = RiderVehicleAdapter(ArrayList(), object: RiderVehicleAdapter.RiderVehicleListener{
            override fun onPressed(riderVehicle: RiderVehicle, index: Int) {
                Timber.v("riderVehicle $riderVehicle"  )
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("My Vehicles")
                        .setMessage("Are you sure you want to change the active vehicle?")
                        .setNeutralButton("CANCEL") { dialog, which ->
                            // Respond to neutral button press
                        }
                        .setPositiveButton("ACCEPT") { dialog, which ->
                            viewModel.changeActiveVehicle(riderVehicle.id)
                        }
                        .show()
            }
        })
        rv_my_vehicles.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        rv_my_vehicles.adapter = myVehicleAdapter

        viewModel.getRiderProfile.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {

                        val rider = result.data.data
                        fillCommonFields(rider)
//                                et_vehicle_model.setText(rider.activeVehicle.vehicleModel)
//                                et_plate_number.setText(rider.activeVehicle.plateNumber)
//                                initMyVehicles()
                        myVehicleAdapter.myVehicles = result.data.data.vehicles
                        myVehicleAdapter.activeVehicle = result.data.data.activeVehicle
                        myVehicleAdapter.notifyDataSetChanged()
                        tv_add_vehicle_link.setOnClickListener {
                            val dialogAddVehicle = DialogAddVehicle()
                            dialogAddVehicle.show(parentFragmentManager,"add_vehicle")
                        }
                    }
                }
                is Result.Error -> {

                }
                is Result.HttpError -> {

                }
            }
        }

        viewModel.changeActiveVehicleResponse.observe(viewLifecycleOwner){ result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        val response = result.data.data
                        val vehicleList = response.vehicles
                        val activeVehicle = response.activeVehicle
                        Toast.makeText(requireContext(),result.data.meta.message,Toast.LENGTH_LONG).show()
                        myVehicleAdapter.activeVehicle = activeVehicle
                        myVehicleAdapter.myVehicles = vehicleList
                        myVehicleAdapter.notifyDataSetChanged()
                        (profileFragment as RiderDashboardFragment).updateActiveVehicle(activeVehicle)
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(),result.metaResponse.message,Toast.LENGTH_LONG).show()
                }
                is Result.HttpError -> {
                    Toast.makeText(requireContext(),result.error.message,Toast.LENGTH_LONG).show()
                }
            }
        }


    }

    private fun initCustomer() {

        group_my_vehicle.visibility = View.GONE
        group_address.visibility = View.VISIBLE
        btn_save_edits_rider.visibility = View.GONE

        myAddressesAdapter = CustomerAddressAdapter(ArrayList(), object: CustomerAddressAdapter.AddressListener{
            override fun onPressed(address: Address, index: Int) {
                Timber.v("address $address"  )
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("My Vehicles")
                        .setMessage("Are you sure you want to change the active vehicle?")
                        .setNeutralButton("CANCEL") { dialog, which ->
                            // Respond to neutral button press
                        }
                        .setPositiveButton("ACCEPT") { dialog, which ->
//                            viewModel.changeActiveVehicle(address.id)
                        }
                        .show()
            }
        })
        rv_addresses.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        rv_addresses.adapter = myAddressesAdapter

        viewModel.getCustomerProfile.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        val customer = result.data.data
                        fillCommonFields(customer)
                        profileFragment?.updateUserLabels(result.data.data)
                        myAddressesAdapter.addresses = result.data.data.addresses
//                        myAddressesAdapter.activeVehicle = result.data.data.activeVehicle
                        myAddressesAdapter.notifyDataSetChanged()
                    }


                    tv_add_address_link.setOnClickListener {
                        val addAddressDialog = DialogAddAddress.newInstance()
                        addAddressDialog.setTargetFragment(this@EditProfileFragment, REQUEST_ADD_VEHICLE)
                        addAddressDialog.show(parentFragmentManager,"ADD_ADDRESS")
                    }

                }
                is Result.Error -> {

                }
                is Result.HttpError -> {

                }
            }
        }
        viewModel.getCustomerProfile()
    }


    private fun fillCommonFields(user: User){
        et_fullname.setText(user.fullName)
        et_email.setText(user.email)
        et_mobile.setText(user.mobileNum)
        et_birthdate.setText(user.birthdate)
        et_location.setText(user.location)

    }





}