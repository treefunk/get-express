package com.myoptimind.getexpress.features.edit_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.ProfileFragment
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerProfileFragment: ProfileFragment() {

    private val viewModel by activityViewModels<EditProfileViewModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_customer_profile,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCustomerProfile()
        if(arguments != null){
            if(requireArguments().getBoolean("is_history") == true){
                Navigation.findNavController(requireActivity(),R.id.nav_host_customer_container).navigate(R.id.action_global_riderHistoryFragment)
            }
        }
    }
}