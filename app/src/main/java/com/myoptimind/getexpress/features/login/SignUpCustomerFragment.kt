package com.myoptimind.getexpress.features.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.navigation.fragment.findNavController
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.clearTextViews
import com.myoptimind.getexpress.features.shared.initDatePicker
import com.myoptimind.getexpress.features.shared.izBlank
import com.myoptimind.getexpress.features.shared.validateEmail
import com.myoptimind.suzuki_app.features.shared.DatePickerDialogFragment
import kotlinx.android.synthetic.main.fragment_sign_up_customer.*
import kotlinx.android.synthetic.main.fragment_sign_up_customer.btn_rider
import kotlinx.android.synthetic.main.fragment_sign_up_customer.btn_sign_up
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_birth_date
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_email_address
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_fullname
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_location
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_mobile_number
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_password
import kotlinx.android.synthetic.main.fragment_sign_up_customer.tv_sign_in_link
import timber.log.Timber

private const val REQUEST_CODE_DATE_PICKER = 767

class SignUpCustomerFragment : Fragment() {

    val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up_customer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initObservers()
    }


    private fun initObservers() {
        /**
         *  CUSTOMER SIGN UP
         */
        viewModel.signUpCustomerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {
                    Timber.v("loading ${result.isLoading}")
                    //TODO: ADD LOADING
                }
                is Result.Success -> {
                    if(result.data != null){
                        arrayOf(
                            et_fullname,
                            et_email_address,
                            et_password,
                            et_mobile_number,
                            et_birth_date,
                            et_location
                        ).clearTextViews()
                        Toast.makeText(requireContext(), result.data.meta.message, Toast.LENGTH_LONG).show()
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
        btn_rider.setOnClickListener {
            findNavController().navigate(R.id.action_signUpCustomerFragment_to_signUpRiderFragment)
        }
        tv_sign_in_link.setOnClickListener {
            findNavController().navigate(R.id.action_signUpCustomerFragment_to_signInCustomerFragment)
        }
        btn_sign_up.setOnClickListener {

            if(et_fullname.izBlank() ||
                et_email_address.izBlank() ||
                et_password.izBlank() ||
                et_birth_date.izBlank() ||
                et_location.izBlank()
               ){
                Toast.makeText(requireContext(),"Please fill in required fields.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(et_mobile_number.text.toString().length != 13){
                Toast.makeText(requireContext(),"Invalid Mobile Number.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(!et_email_address.text.toString().validateEmail()){
                Toast.makeText(requireContext(),"Invalid E-mail format.",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            viewModel.signUpCustomer(
                et_fullname.text.toString(),
                et_email_address.text.toString(),
                et_mobile_number.text.toString(),
                et_birth_date.text.toString(),
                et_location.text.toString(),
                et_password.text.toString()
            )
        }

        et_birth_date.initDatePicker(
            parentFragmentManager,
            this@SignUpCustomerFragment,
            "birth_date",
            REQUEST_CODE_DATE_PICKER
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_DATE_PICKER && resultCode == Activity.RESULT_OK) {
            et_birth_date.setText(data?.getStringExtra(DatePickerDialogFragment.EXTRA_DATE))
        }
    }
}