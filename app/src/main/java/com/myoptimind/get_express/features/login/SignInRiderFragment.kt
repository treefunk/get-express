package com.myoptimind.get_express.features.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.hideKeyboard
import com.myoptimind.get_express.features.shared.izNotBlank
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sign_in_customer.*
import kotlinx.android.synthetic.main.fragment_sign_in_rider.*
import kotlinx.android.synthetic.main.fragment_sign_in_rider.btn_customer
import kotlinx.android.synthetic.main.fragment_sign_in_rider.btn_fb_signup
import kotlinx.android.synthetic.main.fragment_sign_in_rider.btn_google_signup
import kotlinx.android.synthetic.main.fragment_sign_in_rider.btn_sign_in
import kotlinx.android.synthetic.main.fragment_sign_in_rider.et_email_address
import kotlinx.android.synthetic.main.fragment_sign_in_rider.et_password
import kotlinx.android.synthetic.main.fragment_sign_in_rider.tv_forgot_password_link
import kotlinx.android.synthetic.main.fragment_sign_in_rider.tv_sign_up_link
import timber.log.Timber

@AndroidEntryPoint
class SignInRiderFragment: BaseLoginFragment(UserType.RIDER) {

//    override fun getLoginType() = LoginType.RIDER

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
                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                }
                is Result.Success -> {
                    if(result.data != null){
                        initCenterProgress(false)
                        enableViews(true)
                        val code = result.data.meta.code
                        when (code) {
                            "ok" -> {
                                Toast.makeText(requireContext(),"Login Successful", Toast.LENGTH_SHORT).show()

                                requireActivity().finish()
                                startActivity(
                                    Intent(requireContext(), MainActivity::class.java)
                                )
                            }
                            "safe_to_sign_up" -> {
                                SignInRiderFragmentDirections.actionSignInRiderFragmentToSignUpRiderFragment().also {
                                    findNavController().navigate(it)
                                }
                            }
                            "otp_challenge" -> {
                                SignInRiderFragmentDirections.actionSignInRiderFragmentToOtpVerificationFragment2(result.data.data,et_password.text.toString()).also {
                                    findNavController().navigate(it)
                                }
                                Toast.makeText(requireContext(),result.data.meta.message,Toast.LENGTH_LONG).show()
                            }
                        }

                    }
                }
                is Result.Error -> {
                        Toast.makeText(requireContext(),result.metaResponse.message, Toast.LENGTH_SHORT).show()
                    initCenterProgress(false)
                    enableViews(true)
                }
                is Result.HttpError -> {
                    Toast.makeText(requireContext(),result.error.message.toString(), Toast.LENGTH_SHORT).show()
                    initCenterProgress(false)
                    enableViews(true)
                }
            }
        }
    }

    private fun initClickListeners() {
        tv_sign_up_link.setOnClickListener {
            findNavController().navigate(R.id.action_signInRiderFragment_to_signUpCustomerFragment)
        }
        tv_forgot_password_link.setOnClickListener {
//            findNavController().navigate(R.id.action_signInRiderFragment_to_forgotPasswordFragment)
            SignInRiderFragmentDirections.actionSignInRiderFragmentToForgotPasswordFragment(UserType.RIDER).also {
                findNavController().navigate(it)
            }
        }
        btn_customer.setOnClickListener {
            findNavController().navigate(R.id.action_signInRiderFragment_to_signInCustomerFragment)
        }
        btn_sign_in.setOnClickListener {
            hideKeyboard(requireActivity())
            if(et_email_address.izNotBlank() && et_password.izNotBlank()){
                viewModel.signInRider(et_email_address.text.toString(),et_password.text.toString())
            }else{
                Snackbar.make(requireView(),"Please fill in required fields.", Snackbar.LENGTH_SHORT).show()
            }
        }
        btn_fb_signup.setOnClickListener {
            loginWithFacebook()
        }

        btn_google_signup.setOnClickListener {
            loginWithGoogle()
        }
    }

    private fun enableViews(enable: Boolean){
        btn_google_signup.isEnabled = enable
        btn_fb_signup.isEnabled = enable
        btn_customer.isEnabled = enable
        btn_sign_in.isEnabled = enable
        tv_forgot_password_link.isEnabled = enable
        tv_sign_up_link.isEnabled = enable
        et_email_address.isEnabled = enable
        et_password.isEnabled = enable
    }
}