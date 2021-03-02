package com.myoptimind.get_express.features.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.Customer
import com.myoptimind.get_express.features.login.data.User
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.izNotBlank
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_otp_verification.*
import kotlinx.android.synthetic.main.fragment_otp_verification.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class OtpVerificationFragment: BaseLoginFragment(UserType.CUSTOMER) {

    private val args by navArgs<OtpVerificationFragmentArgs>()
    private val viewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_otp_verification,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        //GenericTextWatcher here works only for moving to next EditText when a number is entered
//first parameter is the current EditText and second parameter is next EditText
        etp_otp_1.addTextChangedListener(GenericTextWatcher(etp_otp_1, etp_otp_2))
        etp_otp_2.addTextChangedListener(GenericTextWatcher(etp_otp_2, etp_otp_3))
        etp_otp_3.addTextChangedListener(GenericTextWatcher(etp_otp_3, etp_otp_4))
        etp_otp_4.addTextChangedListener(GenericTextWatcher(etp_otp_4, null))

//GenericKeyEvent here works for deleting the element and to switch back to previous EditText
//first parameter is the current EditText and second parameter is previous EditText
        etp_otp_1.setOnKeyListener(GenericKeyEvent(etp_otp_1, null))
        etp_otp_2.setOnKeyListener(GenericKeyEvent(etp_otp_2, etp_otp_1))
        etp_otp_3.setOnKeyListener(GenericKeyEvent(etp_otp_3, etp_otp_2))
        etp_otp_4.setOnKeyListener(GenericKeyEvent(etp_otp_4,etp_otp_3))

        val user = args.user

        val userType =
            if(user is Customer)
                UserType.CUSTOMER
            else
                UserType.RIDER

        btn_send.setOnClickListener {
            val verificationCode = StringBuilder()

            if(etp_otp_1.izNotBlank())
                verificationCode.append(etp_otp_1.text.toString())
            if(etp_otp_2.izNotBlank())
                verificationCode.append(etp_otp_2.text.toString())
            if(etp_otp_3.izNotBlank())
                verificationCode.append(etp_otp_3.text.toString())
            if(etp_otp_4.izNotBlank())
                verificationCode.append(etp_otp_4.text.toString())



            viewModel.solveOtpChallenge(
                user.email,verificationCode.toString(),userType
            )
        }

        tv_resend_link.setOnClickListener {
            resendOtp(user,userType)
        }

        viewModel.resendOtpResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                }
                is Result.Success -> {
                    if(result.data != null){
                        Snackbar.make(requireView(),result.data.meta.message, Snackbar.LENGTH_SHORT).show()
                        lifecycleScope.launch {
                            for(i in 10 downTo 1){
                                withContext(Main){
                                    tv_resend_link.setOnClickListener {
                                        Toast.makeText(requireContext(), "Please try again after ${i} seconds...",Toast.LENGTH_SHORT).show()
                                    }
                                }
                                delay(1000)
                            }
                            withContext(Main){
                                tv_resend_link.setOnClickListener {
                                    resendOtp(user,userType)
                                }
                            }
                            initCenterProgress(false)
                            enableViews(true)
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

        viewModel.solveOtpChallengeResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                }
                is Result.Success -> {
                    if(result.data != null){
//                        Snackbar.make(requireView(),result.data.meta.message, Snackbar.LENGTH_SHORT).show()
                        Toast.makeText(requireContext(),result.data.meta.message,Toast.LENGTH_LONG).show()
                        when(userType){
                            UserType.CUSTOMER -> {
                                viewModel.signInCustomer(user.email,args.userPassword)
                            }
                            UserType.RIDER -> {
                                if(result.data.meta.status == 200){
                                    viewModel.signInRider(user.email,args.userPassword)
                                }else{
                                    findNavController().navigate(R.id.action_global_welcomeFragment)
                                }
                            }
                        }
                        initCenterProgress(false)
                        enableViews(true)
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
        viewModel.signInCustomerResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    Timber.v("loading ${result.isLoading}")
                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                }
                is Result.Success -> {
                    if (result.data != null) {
                        val code = result.data.meta.code
                        initCenterProgress(false)
                        enableViews(true)
                        when (code) {
                            "ok" -> {
                                Toast.makeText(requireContext(),"Login Successful", Toast.LENGTH_SHORT).show()
                                requireActivity().finish()
                                startActivity(
                                    Intent(requireContext(), MainActivity::class.java)
                                )
                            }
                            "safe_to_sign_up" -> {

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
                    Toast.makeText(requireContext(),result.error.message, Toast.LENGTH_SHORT).show()
                    initCenterProgress(false)
                    enableViews(true)
                }
            }
        }

        viewModel.signInRiderResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
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
                                viewModel.clearLoginPayloads()
                                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_LONG)
                                    .show()
                                requireActivity().finish()
                                startActivity(
                                    Intent(requireContext(), MainActivity::class.java)
                                )
                            }
                            "safe_to_sign_up" -> {

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

    private fun resendOtp(user: User, userType: UserType) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("")
            .setMessage("Resend Otp Verification Code?")
            .setNeutralButton("NO") { dialog, which ->
                // Respond to neutral button press
            }
            .setPositiveButton("YES") { dialog, which ->
                viewModel.resendOtp(user.email,userType)
            }
            .show()
    }

    class GenericTextWatcher internal constructor(private val currentView: View, private val nextView: View?) : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            when (currentView.id) {
                R.id.etp_otp_1 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.etp_otp_2 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.etp_otp_3 -> if (text.length == 1) nextView!!.requestFocus()
                //You can use EditText4 same as above to hide the keyboard
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

    }

    class GenericKeyEvent internal constructor(private val currentView: EditText, private val previousView: EditText?) : View.OnKeyListener{
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if(event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.etp_otp_1 && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }
    }

    private fun enableViews(enable: Boolean){
        etp_otp_1.isEnabled = enable
        etp_otp_2.isEnabled = enable
        etp_otp_3.isEnabled = enable
        etp_otp_4.isEnabled = enable
        btn_send.isEnabled = enable
        tv_resend_link.isEnabled = enable
    }
}

