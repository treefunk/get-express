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
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.hideKeyboard
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import timber.log.Timber

class ForgotPasswordFragment: Fragment() {

    internal lateinit var parentActivity: LoginActivity

    private val viewModel by activityViewModels<LoginViewModel>()
    private val args by navArgs<ForgotPasswordFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            parentActivity = (activity as LoginActivity)
        } catch (exception: TypeCastException) {
            Timber.e("Parent Activity must be of type \"LoginActivity\"!!")
        }
    }

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
                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                }
                is Result.Success -> {
                    if(result.data != null){
                        initCenterProgress(false)
                        enableViews(true)
                        et_email_address.setText("")
                        Snackbar.make(requireView(),result.data.meta.message,Snackbar.LENGTH_SHORT).show()
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
            hideKeyboard(requireActivity())
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_signUpCustomerFragment)
        }
        tv_sign_in_link.setOnClickListener {
            hideKeyboard(requireActivity())
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_signInCustomerFragment)
        }

        btn_send.setOnClickListener {
            hideKeyboard(requireActivity())
            if(et_email_address.text.toString().isNotBlank()){
                viewModel.resetPassword(et_email_address.text.toString().trim(),args.userType)
            }else{
                Snackbar.make(requireView(),"Please enter your Email Address.",Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableViews(enable: Boolean){
        btn_send.isEnabled = enable
        tv_sign_in_link.isEnabled = enable
        tv_sign_up_link.isEnabled =enable
    }

    fun initCenterProgress(showLoading: Boolean){
        if(showLoading)
            parentActivity.showLoading()
        else
            parentActivity.hideLoading()
    }
}