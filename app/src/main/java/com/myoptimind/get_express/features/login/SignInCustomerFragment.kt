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
                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                }
                is Result.Success -> {
                    if (result.data != null) {
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
                                SignInCustomerFragmentDirections.actionSignInCustomerFragmentToSignUpCustomerFragment(true).also {
                                    findNavController().navigate(it)
                                }
                            }
                        }

                    }
                }
                is Result.Error -> {
                    Snackbar.make(requireView(),result.metaResponse.message, Snackbar.LENGTH_SHORT).show()
                    initCenterProgress(false)
                    enableViews(true)
                }
                is Result.HttpError -> {
                    Snackbar.make(requireView(),result.error.message.toString(), Snackbar.LENGTH_SHORT).show()
                    initCenterProgress(false)
                    enableViews(true)
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
            hideKeyboard(requireActivity())
            if(et_email_address.izNotBlank() && et_password.izNotBlank()){
                viewModel.signInCustomer(et_email_address.text.toString(),et_password.text.toString())
            }else{
                Snackbar.make(requireView(),"Please fill in required fields.",Snackbar.LENGTH_SHORT).show()
            }
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

    private fun enableViews(enable: Boolean){
        btn_google_signup.isEnabled = enable
        btn_fb_signup.isEnabled = enable
        btn_rider.isEnabled = enable
        btn_sign_in.isEnabled = enable
        tv_forgot_password_link.isEnabled = enable
        tv_sign_up_link.isEnabled = enable
    }
}