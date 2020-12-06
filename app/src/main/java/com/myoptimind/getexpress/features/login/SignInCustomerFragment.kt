package com.myoptimind.getexpress.features.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.myoptimind.getexpress.MainActivity
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.android.synthetic.main.fragment_sign_in_customer.*
import timber.log.Timber

class SignInCustomerFragment : Fragment() {

    private val viewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in_customer,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        initObservers()
    }

    private fun initObservers() {
        /**
         * CUSTOMER SIGN IN
         */
        viewModel.signInCustomerResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    Timber.v("loading ${result.isLoading}")
                    //TODO: ADD LOADING
                }
                is Result.Success -> {
                    if(result.data != null){
                        Toast.makeText(requireContext(), "success", Toast.LENGTH_LONG).show()
                        requireActivity().finish()
                        startActivity(
                            Intent(requireContext(), MainActivity::class.java)
                        )
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.metaResponse.message, Toast.LENGTH_LONG)
                        .show()
                }
                is Result.HttpError -> {
                    Toast.makeText(requireContext(), result.error.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initClickListeners() {
        tv_sign_up_link.setOnClickListener {
            findNavController().navigate(R.id.action_signInCustomerFragment_to_signUpCustomerFragment)
        }
        tv_forgot_password_link.setOnClickListener {
            findNavController().navigate(R.id.action_signInCustomerFragment_to_forgotPasswordFragment)
        }
        btn_sign_in.setOnClickListener {
            viewModel.signInCustomer(et_email_address.text.toString(),et_password.text.toString())
        }
        btn_rider.setOnClickListener {
            findNavController().navigate(R.id.action_signInCustomerFragment_to_signInRiderFragment)
        }
    }
}