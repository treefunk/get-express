package com.myoptimind.getexpress.features.rider.rider_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.rider.rider_history.data.RiderHistory
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_rider_history.*

@AndroidEntryPoint
class RiderHistoryFragment: TitleOnlyFragment() {
    override fun getTitle(): String = "History"

    lateinit var adapter: RiderHistoryAdapter
    private val viewModel by viewModels<RiderHistoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_rider_history,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RiderHistoryAdapter(ArrayList(), object: RiderHistoryAdapter.RiderHistoryListener{
            override fun onSelectItem(riderHistory: RiderHistory) {
/*
                findNavController().navigate(
                        RiderHistoryFragmentDirections.actionRiderHistoryFragmentToSelectedCustomerRequestFragment(riderHistory.ca)
                )
*/
            }
        })
        rv_history.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        rv_history.adapter = adapter

        initObservers()

        viewModel.getRiderHistory()
    }

    private fun initObservers() {
        viewModel.getRiderHistoryResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    //todo
                }
                is Result.Success -> {
                    if(result.data != null){
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