package com.myoptimind.get_express.features.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.myoptimind.get_express.R
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment: Fragment(R.layout.fragment_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_sign_up.setOnClickListener {
            WelcomeFragmentDirections.actionWelcomeFragmentToSignUpCustomerFragment().also {
                findNavController().navigate(it)
            }
        }
        btn_sign_in.setOnClickListener {
            WelcomeFragmentDirections.actionWelcomeFragmentToSignInCustomerFragment().also {
                findNavController().navigate(it)
            }
        }
    }
}