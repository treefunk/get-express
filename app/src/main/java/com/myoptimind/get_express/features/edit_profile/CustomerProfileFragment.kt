package com.myoptimind.get_express.features.edit_profile

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.shared.ProfileFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_customer_profile.*
import kotlinx.android.synthetic.main.partial_profile_customer.tv_edit_profile
import kotlinx.android.synthetic.main.partial_profile_customer.tv_history


@AndroidEntryPoint
class CustomerProfileFragment : ProfileFragment() {

    private val viewModel by activityViewModels<ProfileViewModel>()
    private val args by navArgs<CustomerProfileFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_customer_profile, container, false)

        val navHostFragment = (childFragmentManager.findFragmentById(R.id.nav_host_customer_container) as NavHostFragment)
        val navInflater = findNavController().navInflater
        val customerGraph = navInflater.inflate(R.navigation.nav_customer_profile_graph)

        if(args.isHistory){
            customerGraph.startDestination = R.id.customerHistoryFragment
        }else{
            customerGraph.startDestination = R.id.editProfileFragment
        }

        navHostFragment.navController.graph = customerGraph

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.editProfileFragment -> {
                    view.findViewById<TextView>(R.id.tv_edit_profile).paintFlags = 0
                    view.findViewById<TextView>(R.id.tv_history).paintFlags = Paint.UNDERLINE_TEXT_FLAG
                }
                R.id.customerHistoryFragment -> {
                    view.findViewById<TextView>(R.id.tv_history).paintFlags = 0
                    view.findViewById<TextView>(R.id.tv_edit_profile).paintFlags = Paint.UNDERLINE_TEXT_FLAG

                }
                else -> {
                    view.findViewById<TextView>(R.id.tv_edit_profile).paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    view.findViewById<TextView>(R.id.tv_history).paintFlags = Paint.UNDERLINE_TEXT_FLAG
                }
            }
        }

        return view
    }

    fun navigateToSelectedCustomerRequest(
        cartId: String,
        isAccepted: Boolean = false,
        historyOnly: Boolean = false
    ) {
        findNavController().navigate(
            CustomerProfileFragmentDirections.actionGlobalSelectedCustomerRequestFragment(
                cartId,
                isAccepted,
                historyOnly
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getCustomerProfile()

        tv_history.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_customer_container)
                .navigate(
                    R.id.action_global_customerHistoryFragment
                )
        }
        tv_edit_profile.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_customer_container)
                .navigate(
                    R.id.action_global_editProfileFragment
                )
        }


    }

    override fun onResume() {
        super.onResume()
    }
}