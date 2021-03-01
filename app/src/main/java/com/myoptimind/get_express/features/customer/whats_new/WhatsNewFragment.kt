package com.myoptimind.get_express.features.customer.whats_new

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.internal.Utility
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.home.CustomerDashboardFragmentDirections
import com.myoptimind.get_express.features.customer.whats_new.data.WhatsNew
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import com.myoptimind.get_express.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_customer_dashboard.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber


@AndroidEntryPoint
class WhatsNewFragment: TitleOnlyFragment() {

    private val viewModel by activityViewModels<WhatsNewViewModel>()
    lateinit var adapter: WhatsNewAdapter

    override fun getTitle() = "What's New"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_whats_new,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WhatsNewAdapter(Utility.arrayList(), object: WhatsNewAdapter.WhatsNewListener {
            override fun onSelectWhatsNew(whatsNew: WhatsNew) {
                WhatsNewFragmentDirections.actionWhatsNewFragmentToSelectedWhatsNewFragment(whatsNew).also {
                    findNavController().navigate(it)
                }
            }
        })
        rv_whats_new.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)
        rv_whats_new.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.whatsNewListResult.collect { result ->
                when(result){
                    is Result.Progress -> {
                        initCenterProgress(result.isLoading)
                    }
                    is Result.Success -> {
                        if(result.data != null){
                            adapter.whatsNewsList = result.data.data
                            adapter.notifyDataSetChanged()
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
}