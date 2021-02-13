package com.myoptimind.get_express.features.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sign_in_rider.*
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
                    Timber.v("loading ${result.isLoading}")
                    //TODO: ADD LOADING
                }
                is Result.Success -> {
                    if(result.data != null){

                        val code = result.data.meta.code
                        when (code) {
                            "ok" -> {
                                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_LONG)
                                    .show()
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
                        }

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
//            findNavController().navigate(R.id.action_signInRiderFragment_to_forgotPasswordFragment)
            SignInRiderFragmentDirections.actionSignInRiderFragmentToForgotPasswordFragment(UserType.RIDER).also {
                findNavController().navigate(it)
            }
        }
        btn_customer.setOnClickListener {
            findNavController().navigate(R.id.action_signInRiderFragment_to_signInCustomerFragment)
        }
        btn_sign_in.setOnClickListener {
            viewModel.signInRider(et_email_address.text.toString(),et_password.text.toString())
        }
        btn_fb_signup.setOnClickListener {
            loginWithFacebook()
        }

        btn_google_signup.setOnClickListener {
            loginWithGoogle()
        }
    }
}