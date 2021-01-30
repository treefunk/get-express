package com.myoptimind.getexpress.features.rider.customer_requests_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.rider.rider_dashboard.RiderDashboardFragment
import com.myoptimind.getexpress.features.rider.rider_dashboard.RiderDashboardFragmentDirections
import com.myoptimind.getexpress.features.rider.customer_requests_list.data.CustomerRequest
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.LogoOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_customer_request_list.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

private const val REFRESH_INTERVAL_SECOND = 10

@AndroidEntryPoint
class CustomerRequestListFragment: LogoOnlyFragment() {

    private var job: Job? = null

    private val viewModel by viewModels<CustomerRequestViewModel>()
    lateinit var adapter: CustomerRequestAdapter
    lateinit var parentFrag: RiderDashboardFragment

    @Inject
    lateinit var appSharedPref: AppSharedPref

    private fun refresh() = lifecycleScope.launchWhenResumed {
        for(i in REFRESH_INTERVAL_SECOND downTo 1){
            Timber.d("hashcode - ${this.hashCode()} refreshing in $i..")
            delay(1000)
        }
        viewModel.getCustomerRequests()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_customer_request_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initAdapter()
        viewModel.getCustomerRequests()

        parentFrag = requireParentFragment().requireParentFragment() as RiderDashboardFragment
    }



    override fun onResume() {
        super.onResume()
        adapter.let {
            it.customerRequests = ArrayList()
            it.notifyDataSetChanged()
        }
    }

    private fun initAdapter() {
        adapter = CustomerRequestAdapter(ArrayList(), object: CustomerRequestAdapter.CustomerRequestListener{
            override fun onSelectItem(customerRequest: CustomerRequest) {
                parentFrag.navigateToSelectedCustomerRequest(customerRequest.cartId)
            }

            override fun onPressButton(accepted: Boolean, customerRequest: CustomerRequest) {
                MaterialAlertDialogBuilder(requireContext())
                        .setMessage("Accept this request?")
                        .setNeutralButton("CANCEL") { _, _ ->
                            // Respond to neutral button press
                        }
                        .setPositiveButton("ACCEPT") { _, _ ->
                            parentFrag.navigateToSelectedCustomerRequest(customerRequest.cartId,accepted)
                        }
                        .show()
            }
        })
        rv_customer_request.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)
        }
        rv_customer_request.adapter = adapter

    }

    private fun initObservers() {
        viewModel.customerRequestsResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        if(
                                job == null || // if initial request
                                job!!.isCompleted // if request is finished
                        ){
                            if(result.data.activeCartId.isNotBlank()){
                                if(findNavController().currentDestination?.id != R.id.selectedCustomerRequestFragment){
                                    parentFrag.navigateToSelectedCustomerRequest(result.data.activeCartId)
/*                                    findNavController().navigate(
                                            RiderDashboardFragmentDirections.actionRiderDashboardFragmentToSelectedCustomerRequestFragment(result.data.activeCartId)
                                    )*/
                                }
                            }
                            Timber.v("refreshing list..")
                            adapter.customerRequests = result.data.data
                            adapter.notifyDataSetChanged()
                            job.run {
                                this?.cancel()
                                job = refresh()
                                job?.start()
                            }
                        }
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