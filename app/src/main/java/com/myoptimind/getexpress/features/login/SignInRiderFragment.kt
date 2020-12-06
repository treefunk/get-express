package com.myoptimind.getexpress.features.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.myoptimind.getexpress.BaseFragment
import com.myoptimind.getexpress.MainActivity
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.android.synthetic.main.fragment_sign_in_rider.*
import timber.log.Timber

class SignInRiderFragment: Fragment() {

    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sign_in_rider,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        initObservers()
    }

    private fun initObservers() {
        viewModel.signInRiderResult.observe(viewLifecycleOwner){ result ->
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
            findNavController().navigate(R.id.action_signInRiderFragment_to_signUpCustomerFragment)
        }
        tv_forgot_password_link.setOnClickListener {
            findNavController().navigate(R.id.action_signInRiderFragment_to_forgotPasswordFragment)
        }
        btn_customer.setOnClickListener {
            findNavController().navigate(R.id.action_signInRiderFragment_to_signInCustomerFragment)
        }
        btn_sign_in.setOnClickListener {
            viewModel.signInRider(et_email_address.text.toString(),et_password.text.toString())
        }
    }
}