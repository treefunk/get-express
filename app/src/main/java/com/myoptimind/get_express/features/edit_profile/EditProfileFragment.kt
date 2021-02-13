package com.myoptimind.get_express.features.edit_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.Address
import com.myoptimind.get_express.features.login.data.RiderVehicle
import com.myoptimind.get_express.features.login.data.User
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.rider.rider_dashboard.RiderDashboardFragment
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.ProfileFragment
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.et_fullname
import kotlinx.android.synthetic.main.fragment_edit_profile.et_location
import kotlinx.android.synthetic.main.fragment_sign_up_rider.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class EditProfileFragment: TitleOnlyFragment() {

    companion object {
        const val REQUEST_ADD_VEHICLE = 111
        const val REQUEST_ADD_ADDRESS = 222
    }

    private val viewModel by activityViewModels<ProfileViewModel>()
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
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
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
//            Toast.makeText(requireContext(),"Password does not match",Toast.LENGTH_LONG).show()
            Snackbar.make(requireView(), "Password does not match", Snackbar.LENGTH_SHORT).show()
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
        setNewTitle("Edit Profile")
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
                    if (result.data != null) {
                        if (result.data.meta.code.equals("ok")) {
/*
                            Toast.makeText(requireContext(),"Profile Successfully Updated!",Toast.LENGTH_SHORT).also {
                                it.setGravity(Gravity.CENTER,0,0)
                                it.show()
                            }
*/

                            Snackbar.make(
                                requireView(),
                                "Profile Successfully Updated!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            profileFragment?.updateUserLabels(result.data.data)
                            (activity as MainActivity).updateSideNavName(result.data.data.fullName)
                        }
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

        myVehicleAdapter = RiderVehicleAdapter(
            ArrayList(),
            object : RiderVehicleAdapter.RiderVehicleListener {
                override fun onPressed(riderVehicle: RiderVehicle, index: Int) {
                    Timber.v("riderVehicle $riderVehicle")
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
            },
            unverifiedVehicles = ArrayList()
        )
        val linearLayoutManager = object : LinearLayoutManager(requireContext()){
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                val margin = 8
                lp?.height = (height / 2) - margin * 2
                lp?.bottomMargin = margin
                lp?.topMargin = margin
                return true
            }
        }.also {
            it.orientation = RecyclerView.VERTICAL
        }
        rv_my_vehicles.layoutManager = linearLayoutManager
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
                        myVehicleAdapter.unverifiedVehicles = result.data.data.unverifiedVehicles
                        myVehicleAdapter.activeVehicle = result.data.data.activeVehicle
                        myVehicleAdapter.notifyDataSetChanged()
                        tv_add_vehicle_link.setOnClickListener {
                            val dialogAddVehicle = AddVehicleDialog.newInstance()
                            dialogAddVehicle.setTargetFragment(this, REQUEST_ADD_VEHICLE)
                            dialogAddVehicle.show(parentFragmentManager, "add_vehicle")
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
                    if (result.data != null) {
                        Snackbar.make(
                            requireView(),
                            "Active Vehicle Successfully Updated.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        val response = result.data.data
                        val vehicleList = response.vehicles
                        val activeVehicle = response.activeVehicle
                        myVehicleAdapter.activeVehicle = activeVehicle
                        myVehicleAdapter.myVehicles = vehicleList
                        myVehicleAdapter.notifyDataSetChanged()
                        (profileFragment as RiderDashboardFragment).updateActiveVehicle(
                            activeVehicle
                        )
                    }
                }
                is Result.Error -> {
                    Snackbar.make(requireView(), result.metaResponse.message, Snackbar.LENGTH_SHORT)
                        .show()
                }
                is Result.HttpError -> {
                    Snackbar.make(
                        requireView(),
                        result.error.message.toString(),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }


    }

    private fun initCustomer() {

        group_my_vehicle.visibility = View.GONE
        group_address.visibility = View.VISIBLE
        btn_save_edits_rider.visibility = View.GONE

        myAddressesAdapter = CustomerAddressAdapter(
            ArrayList(),
            object : CustomerAddressAdapter.AddressListener {
                override fun onPressed(address: Address, index: Int) {
                    Timber.v("address $address")
                    val addAddressDialog = AddAddressDialog.newInstance(address)
                    addAddressDialog.setTargetFragment(
                        this@EditProfileFragment,
                        REQUEST_ADD_ADDRESS
                    )
                    addAddressDialog.show(parentFragmentManager, "ADD_ADDRESS")
                }
            })
        rv_addresses.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )
        rv_addresses.adapter = myAddressesAdapter

        viewModel.getCustomerProfile.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
                        val customer = result.data.data
                        fillCommonFields(customer)
                        profileFragment?.updateUserLabels(result.data.data)
                        myAddressesAdapter.addresses = result.data.data.addresses
//                        myAddressesAdapter.activeVehicle = result.data.data.activeVehicle
                        myAddressesAdapter.notifyDataSetChanged()
                    }


                    tv_add_address_link.setOnClickListener {
                        val addAddressDialog = AddAddressDialog.newInstance()
                        addAddressDialog.setTargetFragment(
                            this@EditProfileFragment,
                            REQUEST_ADD_ADDRESS
                        )
                        addAddressDialog.show(parentFragmentManager, "ADD_ADDRESS")
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