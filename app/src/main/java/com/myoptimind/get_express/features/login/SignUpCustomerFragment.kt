package com.myoptimind.get_express.features.login

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.lifecycle.observe
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.shared.DatePickerDialogFragment.Companion.EXTRA_DATE
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.clearTextViews
import com.myoptimind.get_express.features.shared.initDatePicker
import com.myoptimind.get_express.features.shared.izBlank
import com.myoptimind.get_express.features.shared.validateEmail
import kotlinx.android.synthetic.main.fragment_sign_up_customer.*
import kotlinx.android.synthetic.main.fragment_sign_up_customer.btn_fb_signup
import kotlinx.android.synthetic.main.fragment_sign_up_customer.btn_google_signup
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

class SignUpCustomerFragment : BaseLoginFragment(UserType.CUSTOMER) {

    val viewModel: LoginViewModel by activityViewModels()
    val args by navArgs<SignUpCustomerFragmentArgs>()

    companion object {
//        private const val REQUEST_LOCATION = 150
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up_customer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(args.isSocialLogin.not()){
            viewModel.clearLoginPayloads()
        }

        initClickListeners()
        initObservers()
    }

    private fun autofillFieldsFromSocial(
            email: String,
            fullname: String,
            token: String
    ){
        et_fullname.setText(fullname)
        et_email_address.setText(email)
        et_email_address.isEnabled = false
        et_password.isEnabled = false
        et_password.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.grey_200_alpha20))


        btn_sign_up.setOnClickListener {

            if(et_fullname.izBlank() ||
                    et_email_address.izBlank() ||
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
                    socialToken = token,
                    isEmailVerified = "1"
            )
        }
    }
    private fun initObservers() {
        //facebook

        viewModel.googleUserPayload.observe(viewLifecycleOwner){ googleUserPayload ->
            if(googleUserPayload != null){
                autofillFieldsFromSocial(
                        googleUserPayload.email,
                        googleUserPayload.name,
                        googleUserPayload.googleToken
                )
            }
        }

        viewModel.facebookUserPayload.observe(viewLifecycleOwner) { facebookUserPayload ->
            if(facebookUserPayload != null){
                autofillFieldsFromSocial(
                        facebookUserPayload.email,
                        facebookUserPayload.firstName + " " + facebookUserPayload.lastName,
                        facebookUserPayload.fbToken
                )
            }
        }

        viewModel.signInCustomerResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                    Timber.v("loading ${result.isLoading}")
                }
                is Result.Success -> {
                    if (result.data != null) {
                        val code = result.data.meta.code
                        when (code) {
                            "ok" -> {
                                Toast.makeText(requireContext(), "success", Toast.LENGTH_LONG)
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
                    Toast.makeText(requireContext(), result.metaResponse.message, Toast.LENGTH_LONG)
                            .show()
                }
                is Result.HttpError -> {
                    Toast.makeText(requireContext(), result.error.message, Toast.LENGTH_LONG).show()
                }
            }
        }



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

        btn_google_signup.setOnClickListener {
            loginWithGoogle()
        }

        btn_fb_signup.setOnClickListener {
            loginWithFacebook()
        }

/*
        et_location.setOnClickListener {
            showPlacesAutocomplete(REQUEST_LOCATION)
        }
*/



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
/*        et_birth_date.setOnClickListener {
            DatePickerDialog(requireContext(),DatePickerDialog.OnDateSetListener({ d, year, month, dayOfMonth ->

            }),2001,1,1).show()
        }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_DATE_PICKER && resultCode == Activity.RESULT_OK) {
            et_birth_date.setText(data?.getStringExtra(EXTRA_DATE))
        }
/*        else if(requestCode == REQUEST_LOCATION && resultCode == Activity.RESULT_OK){
            if(data != null){
                val place = Autocomplete.getPlaceFromIntent(data)
                Timber.d("Place: ${place.name}, ${place.id}")
                et_location.setText(place.name)
            }
        }*/
    }

    private fun showPlacesAutocomplete(requestCode: Int){
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        startActivityForResult(intent, requestCode)
    }
}