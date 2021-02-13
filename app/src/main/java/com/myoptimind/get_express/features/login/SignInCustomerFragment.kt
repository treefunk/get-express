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
import kotlinx.android.synthetic.main.fragment_sign_in_customer.*
import timber.log.Timber

@AndroidEntryPoint
class SignInCustomerFragment : BaseLoginFragment(UserType.CUSTOMER) {

    private val viewModel by activityViewModels<LoginViewModel>()
//    override fun getLoginType() = LoginType.CUSTOMER

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
                    if (result.data != null) {
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
                                SignInCustomerFragmentDirections.actionSignInCustomerFragmentToSignUpCustomerFragment(true).also {
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
            findNavController().navigate(R.id.action_signInCustomerFragment_to_signUpCustomerFragment)
        }
        tv_forgot_password_link.setOnClickListener {
//            findNavController().navigate(R.id.action_signInCustomerFragment_to_forgotPasswordFragment)
            SignInCustomerFragmentDirections.actionSignInCustomerFragmentToForgotPasswordFragment(UserType.CUSTOMER).also {
                findNavController().navigate(it)
            }
        }
        btn_sign_in.setOnClickListener {
            viewModel.signInCustomer(et_email_address.text.toString(),et_password.text.toString())
        }
        btn_rider.setOnClickListener {
            findNavController().navigate(R.id.action_signInCustomerFragment_to_signInRiderFragment)
        }



        btn_fb_signup.setOnClickListener {
//            LoginManager.getInstance().logInWithReadPermissions(this@SignInCustomerFragment, listOf("email"))
            loginWithFacebook()
        }

        btn_google_signup.setOnClickListener {
            loginWithGoogle()
        }

    }

}