package com.myoptimind.get_express.features.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.shared.api.Result
import kotlinx.android.synthetic.main.fragment_forgot_password.*

class ForgotPasswordFragment: Fragment() {

    private val viewModel by activityViewModels<LoginViewModel>()
    private val args by navArgs<ForgotPasswordFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgot_password,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        initObservers()
        when(args.userType){
            UserType.CUSTOMER -> et_email_address.hint = "Customer Email Address"
            UserType.RIDER -> et_email_address.hint = "Rider Email Address"
        }
    }

    private fun initObservers() {
        viewModel.forgotPasswordResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        et_email_address.setText("")
                        Toast.makeText(requireContext(),result.data.meta.message,Toast.LENGTH_LONG).show()
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(),result.metaResponse.message,Toast.LENGTH_LONG).show()
                }
                is Result.HttpError -> {
                    Toast.makeText(requireContext(),result.error.message,Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initClickListeners() {
        tv_sign_up_link.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_signUpCustomerFragment)
        }
        tv_sign_in_link.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_signInCustomerFragment)
        }

        btn_send.setOnClickListener {
            if(et_email_address.text.toString().isNotBlank()){
                viewModel.resetPassword(et_email_address.text.toString().trim(),args.userType)
            }else{
                Toast.makeText(requireContext(),"Please enter your Email Address.",Toast.LENGTH_LONG).show()
            }
        }
    }
}