package com.myoptimind.get_express.features.rider.rider_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.home.CustomerDashboardFragment
import com.myoptimind.get_express.features.edit_profile.AddAddressDialog
import com.myoptimind.get_express.features.edit_profile.CustomerProfileFragment
import com.myoptimind.get_express.features.edit_profile.EditProfileFragment
import com.myoptimind.get_express.features.edit_profile.ProfileViewModel
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.rider.rider_dashboard.RiderDashboardFragment
import com.myoptimind.get_express.features.rider.rider_history.data.RiderHistory
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.ProfileFragment
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.CartType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_rider_history.*
import javax.inject.Inject

@AndroidEntryPoint
class RiderHistoryFragment : TitleOnlyFragment() {
    override fun getTitle(): String = "History"

    lateinit var adapter: RiderHistoryAdapter
    private val viewModel by viewModels<RiderHistoryViewModel>()
    private val editProfileViewModel by activityViewModels<ProfileViewModel>()

    @Inject
    lateinit var appSharedPref: AppSharedPref

    var profileFragment: ProfileFragment? = null

//    var selectedServiceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(appSharedPref.getUserType() == UserType.RIDER){
            profileFragment = (requireParentFragment().requireParentFragment() as RiderDashboardFragment)
        }else{
            profileFragment = (requireParentFragment().requireParentFragment() as CustomerProfileFragment)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_rider_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var parentFrag: Fragment

        if (appSharedPref.getUserType() == UserType.CUSTOMER) {
            parentFrag = requireParentFragment().requireParentFragment() as CustomerProfileFragment

            editProfileViewModel.getCustomerProfile.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Result.Progress -> {

                    }
                    is Result.Success -> {
                        if (result.data != null) {
                            val customer = result.data.data
                            profileFragment?.updateUserLabels(result.data.data)
                        }


                    }
                    is Result.Error -> {

                    }
                    is Result.HttpError -> {

                    }
                }
            }
        } else{
             parentFrag = requireParentFragment().requireParentFragment() as RiderDashboardFragment
        }


        adapter = RiderHistoryAdapter(ArrayList(), object : RiderHistoryAdapter.RiderHistoryListener {
            override fun onSelectItem(riderHistory: RiderHistory) {

                if(appSharedPref.getUserType() == UserType.RIDER){
                    (parentFrag as RiderDashboardFragment).navigateToSelectedCustomerRequest(riderHistory.cartId, historyOnly = true)
                }else{
//                    findNavController().navigate(R.id.action_global_riderHistoryFragment,riderHistory.cartId,true)
                }

/*
                findNavController().navigate(
                        RiderHistoryFragmentDirections.actionRiderHistoryFragmentToSelectedCustomerRequestFragment(riderHistory.ca)
                )
*/
            }
        })
        rv_history.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rv_history.adapter = adapter

        initObservers()
        initClickListeners()
    }

    private fun initClickListeners() {
        label_get_delivery.setOnClickListener { viewModel.getRiderHistory(CartType.DELIVERY) }
        label_get_car.setOnClickListener { viewModel.getRiderHistory(CartType.CAR) }
        label_get_food.setOnClickListener { viewModel.getRiderHistory(CartType.FOOD) }
        label_get_grocery.setOnClickListener { viewModel.getRiderHistory(CartType.GROCERY) }
        label_get_pabili.setOnClickListener { viewModel.getRiderHistory(CartType.PABILI) }
    }

    private fun initObservers() {

        viewModel.selectedCartType.observe(viewLifecycleOwner){ cartType ->
            if(cartType != null){
                label_get_delivery.setTextColor(ContextCompat.getColor(requireContext(),android.R.color.white))
                label_get_car.setTextColor(ContextCompat.getColor(requireContext(),android.R.color.white))
                label_get_food.setTextColor(ContextCompat.getColor(requireContext(),android.R.color.white))
                label_get_grocery.setTextColor(ContextCompat.getColor(requireContext(),android.R.color.white))
                label_get_pabili.setTextColor(ContextCompat.getColor(requireContext(),android.R.color.white))

                when(cartType){
                    CartType.CAR -> label_get_car.setTextColor(ContextCompat.getColor(requireContext(),R.color.yellow_200))
                    CartType.GROCERY -> label_get_grocery.setTextColor(ContextCompat.getColor(requireContext(),R.color.yellow_200))
                    CartType.PABILI -> label_get_pabili.setTextColor(ContextCompat.getColor(requireContext(),R.color.yellow_200))
                    CartType.DELIVERY -> label_get_delivery.setTextColor(ContextCompat.getColor(requireContext(),R.color.yellow_200))
                    CartType.FOOD -> label_get_food.setTextColor(ContextCompat.getColor(requireContext(),R.color.yellow_200))
                }
            }
        }

        viewModel.getRiderHistoryResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {
                    //todo
                }
                is Result.Success -> {
                    if (result.data != null) {
                        adapter.riderHistoryList = result.data.data
                        adapter.notifyDataSetChanged()
                    }
                }
                is Result.Error -> {

                }
                is Result.HttpError -> {

                }
            }
        }
    }
}