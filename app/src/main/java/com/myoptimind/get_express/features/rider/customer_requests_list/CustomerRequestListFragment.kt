package com.myoptimind.get_express.features.rider.customer_requests_list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.Rider
import com.myoptimind.get_express.features.rider.customer_requests_list.api.CustomerRequestService
import com.myoptimind.get_express.features.rider.rider_dashboard.RiderDashboardFragment
import com.myoptimind.get_express.features.rider.customer_requests_list.data.CustomerRequest
import com.myoptimind.get_express.features.rider.rider_dashboard.RiderDashboardViewModel
import com.myoptimind.get_express.features.rider.topup.MyWalletDialog
import com.myoptimind.get_express.features.rider.topup.RiderTopUpDialog
import com.myoptimind.get_express.features.rider.topup.TOPUP_PAYMENT_TYPE
import com.myoptimind.get_express.features.rider.topup.api.RiderTopupService
import com.myoptimind.get_express.features.rider.topup.data.WalletOffer
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.LogoOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.toMoneyFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_customer_request_list.*
import kotlinx.android.synthetic.main.partial_nav_top.*
import kotlinx.android.synthetic.main.partial_profile_admin.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

private const val REFRESH_INTERVAL_SECOND = 15

@AndroidEntryPoint
class CustomerRequestListFragment: LogoOnlyFragment() {

    private var job: Job? = null

    private val viewModel by viewModels<CustomerRequestViewModel>()
    private val riderDashBoardViewModel by activityViewModels<RiderDashboardViewModel>()
    lateinit var adapter: CustomerRequestAdapter
    lateinit var parentFrag: RiderDashboardFragment

    companion object {
        const val REQUEST_TOP_UP = 999
        private const val RC_WALLET_TOPUP = 111
        private const val RC_UPDATE_CASH_ON_HAND = 222
    }



    @Inject
    lateinit var appSharedPref: AppSharedPref

    private fun requestSearch() = lifecycleScope.launchWhenResumed {
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

        dashboard_top_up_now.setOnClickListener {
            riderDashBoardViewModel.getWalletOffers(
                appSharedPref.getRiderFromPrefs().activeVehicle.vehicleId
            )
        }




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
                    if(result.data != null) {
                        if (result.data is CustomerRequestService.GetCustomerRequestResponse) {

                            if (
                                job == null || // if initial request
                                job!!.isCompleted // if request is finished
                            ) {
                                if (result.data.activeCartId.isNotBlank()) {
                                    if (findNavController().currentDestination?.id != R.id.selectedCustomerRequestFragment) {
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
                                    job = requestSearch()
                                    job?.start()
                                }
                            }
                        } else if(result.data is RiderTopupService.RemainingBalanceResponse){
                            for(view: View in listOf<View>(iv_wallet,tv_my_wallet,label_my_wallet)){
                                view.setOnClickListener {
                                    val dialog = MyWalletDialog.newInstance(result.data.data)
                                    dialog.setTargetFragment(this, RC_WALLET_TOPUP)
                                    dialog.show(parentFragmentManager,"my_wallet")
                                }
                            }
                            tv_my_wallet.text = "P ${result.data.data.toMoneyFormat()}"
                        }else if (result.data is Rider){
                            for(view: View in listOf<View>(iv_cash_on_hand,tv_my_cash_on_hand,label_my_cash_on_hand)){
                                view.setOnClickListener {
                                    val dialog = MyWalletDialog.newInstance(result.data.cashOnHand,true)
                                    dialog.setTargetFragment(this, RC_UPDATE_CASH_ON_HAND)
                                    dialog.show(parentFragmentManager,"cash_on_hand")
                                }
                            }
                            tv_my_cash_on_hand.text = "P ${result.data.cashOnHand}"
                        } else if (result.data is RiderTopupService.WalletTransactionResponse) {
                            val walletResponse = result.data
                            if(walletResponse.bookingAvailableUntil.isBlank()){
                                rv_customer_request.visibility = View.INVISIBLE
                                group_top_up_notice.visibility = View.VISIBLE
                                (activity as MainActivity).ib_top_up.visibility = View.VISIBLE
                                (activity as MainActivity).group_expires.visibility = View.GONE

                                (activity as MainActivity).ib_top_up.setOnClickListener {
                                    riderDashBoardViewModel.getWalletOffers(
                                        appSharedPref.getRiderFromPrefs().activeVehicle.vehicleId
                                    )
                                }
                            }else{
                                (activity as MainActivity).ib_top_up.visibility = View.GONE
                                group_top_up_notice.visibility = View.GONE
                                rv_customer_request.visibility = View.VISIBLE

                                val expiresIn = walletResponse.getExpiresIn()
                                if(expiresIn != null){
                                        if((activity as MainActivity).group_expires.visibility == View.GONE){
                                            (activity as MainActivity).group_expires.visibility = View.VISIBLE
                                        }
                                    (activity as MainActivity).tv_expire_time.text = expiresIn
                                }else{
                                    (activity as MainActivity).group_expires.visibility = View.GONE
                                }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_WALLET_TOPUP && resultCode == Activity.RESULT_OK){
            val amount = data?.getDoubleExtra(MyWalletDialog.DATA_AMOUNT_TO_LOAD,0.00)
            if(amount != null && amount.toDouble() >= 100){
                riderDashBoardViewModel.addWalletBalance(appSharedPref.getUserId(),amount.toString())
            }
        }else if(requestCode == RC_UPDATE_CASH_ON_HAND && resultCode == Activity.RESULT_OK){
            val amount = data?.getDoubleExtra(MyWalletDialog.DATA_AMOUNT_TO_LOAD,0.00)
            if(amount != null && amount.toDouble() >= 100){
                riderDashBoardViewModel.updateCashOnHand(appSharedPref.getUserId(),amount.toString())
            }
        }else if (requestCode == RiderDashboardFragment.REQUEST_TOP_UP && resultCode == Activity.RESULT_OK) {
            val walletOffer =
                data?.getParcelableExtra<WalletOffer>(RiderTopUpDialog.DATA_WALLET_OFFER)
            val paymentType =
                data?.getParcelableExtra<TOPUP_PAYMENT_TYPE>(RiderTopUpDialog.DATA_PAYMENT_TYPE)

            if (walletOffer != null) {
                when (paymentType) {
                    TOPUP_PAYMENT_TYPE.GCASH -> riderDashBoardViewModel.topUpWallet(
                        appSharedPref.getUserId(),
                        walletOffer.name,
                        walletOffer.price,
                        walletOffer.durationInHours
                    )
                    TOPUP_PAYMENT_TYPE.WALLET -> riderDashBoardViewModel.topUpWalletByBalance(
                        appSharedPref.getUserId(),
                        walletOffer.name,
                        walletOffer.price,
                        walletOffer.durationInHours
                    )
                    null -> Toast.makeText(requireContext(), "Invalid Payment", Toast.LENGTH_SHORT)
                        .show()
                }

            }

        }
    }



}