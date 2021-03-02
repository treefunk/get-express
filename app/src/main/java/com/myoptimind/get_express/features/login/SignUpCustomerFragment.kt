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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import com.myoptimind.get_express.features.shared.*
import com.myoptimind.get_express.features.shared.DatePickerDialogFragment.Companion.EXTRA_DATE
import com.myoptimind.get_express.features.shared.api.Result
import kotlinx.android.synthetic.main.fragment_sign_up_customer.*
import kotlinx.android.synthetic.main.fragment_sign_up_customer.btn_fb_signup
import kotlinx.android.synthetic.main.fragment_sign_up_customer.btn_google_signup
import kotlinx.android.synthetic.main.fragment_sign_up_customer.btn_rider
import kotlinx.android.synthetic.main.fragment_sign_up_customer.btn_sign_up
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_birth_date
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_email_address
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_fullname
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_mobile_number
import kotlinx.android.synthetic.main.fragment_sign_up_customer.et_password
import kotlinx.android.synthetic.main.fragment_sign_up_customer.tv_sign_in_link
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
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
                    et_mobile_number.izBlank() ||
                    et_birth_date.izBlank()
            ){
                Snackbar.make(requireView(),"Please fill in required fields.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(et_mobile_number.text.toString().isNotEmpty() && et_mobile_number.text.toString().replace("#","").length != 13){
                Snackbar.make(requireView(),"Invalid Mobile Number.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(!et_email_address.text.toString().validateEmail()){
                Snackbar.make(requireView(),"Invalid E-mail format.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            viewModel.signUpCustomer(
                    et_fullname.text.toString(),
                    et_email_address.text.toString(),
                    et_mobile_number.text.toString(),
                    et_birth_date.text.toString(),
                    "",
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

/*                viewModel.signUpCustomer(
                    googleUserPayload.name,
                    googleUserPayload.email,
                    "",
                    "",
                    "",
                    null,
                    socialToken = googleUserPayload.googleToken,
                    isEmailVerified = "1"
                )*/

            }else{
                et_email_address.isEnabled = true
                et_password.isEnabled = true
                et_password.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                et_password.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_grey_border)
            }
        }

        viewModel.facebookUserPayload.observe(viewLifecycleOwner) { facebookUserPayload ->
            if(facebookUserPayload != null){
                autofillFieldsFromSocial(
                        facebookUserPayload.email,
                        facebookUserPayload.firstName + " " + facebookUserPayload.lastName,
                        facebookUserPayload.fbToken
                )

/*                viewModel.signUpCustomer(
                    facebookUserPayload.firstName + " " + facebookUserPayload.lastName,
                    facebookUserPayload.email,
                    "",
                    "",
                    "",
                    null,
                    socialToken = facebookUserPayload.fbToken,
                    isEmailVerified = "1"
                )*/
            }else{
                et_email_address.isEnabled = true
                et_password.isEnabled = true
                et_password.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                et_password.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_grey_border)
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



        /**
         *  CUSTOMER SIGN UP
         */
        viewModel.signUpCustomerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {
                    initCenterProgress(result.isLoading)
                    enableViews(result.isLoading.not())
                }
                is Result.Success -> {
                    if (result.data != null) {
                        val meta = result.data.meta
                        if (meta.code == "otp_challenge") {
                            SignUpCustomerFragmentDirections.actionSignUpCustomerFragmentToOtpVerificationFragment(result.data.data,et_password.text.toString()).also {
                                findNavController().navigate(it)
                            }
                        } else {
                            if (!et_password.isEnabled) {
                                viewModel.signInCustomerSocial(
                                    result.data.data.email,
                                    result.data.data.socialToken
                                )
                            } else {
//                            viewModel.signInCustomer(result.data.data.email,et_password.text.toString())
                            }

                            arrayOf(
                                et_fullname,
                                et_email_address,
                                et_password,
                                et_mobile_number,
                                et_birth_date
                            ).clearTextViews()
                            viewModel.clearLoginPayloads()
                            Toast.makeText(
                                requireContext(),
                                "Account Successfully created.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        initCenterProgress(false)
                        enableViews(true)


                    }
                }
                is Result.Error -> {
                    val metaResponse = result.metaResponse
                    Toast.makeText(requireContext(),metaResponse.message, Toast.LENGTH_SHORT).show()
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
            hideKeyboard(requireActivity())
            if(et_fullname.izBlank() ||
                et_email_address.izBlank() ||
                et_password.izBlank() ||
                et_birth_date.izBlank() ||
                et_mobile_number.izBlank()
               ){
                Snackbar.make(requireView(),"Please fill in required fields.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(et_mobile_number.text.toString().isNotEmpty() && et_mobile_number.text.toString().replace("#","").length != 13){
                Snackbar.make(requireView(),"Invalid Mobile Number.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(!et_email_address.text.toString().validateEmail()){
                Snackbar.make(requireView(),"Invalid E-mail format.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            viewModel.signUpCustomer(
                et_fullname.text.toString(),
                et_email_address.text.toString(),
                et_mobile_number.text.toString(),
                et_birth_date.text.toString(),
                "",
                et_password.text.toString()
            )
        }

/*        et_birth_date.initDatePicker(
            parentFragmentManager,
            this@SignUpCustomerFragment,
            "birth_date",
            REQUEST_CODE_DATE_PICKER
        )*/
        et_birth_date.initDatePickerSpinner(requireContext())
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

        var locationBias: LocationBias? = if(RiderTrackingService.latLong.value != null){
            val latLng = RiderTrackingService.latLong.value!!

            val meters = 500.0

            val coef = meters * 0.0000089

            val latMax = latLng.latitude + coef
            val lngMax = latLng.longitude + coef / Math.cos(latLng.latitude * 0.18)

            val latMin = latLng.latitude - coef
            val lngMin = latLng.longitude - coef / Math.cos(latLng.latitude * 0.18)


            /*  val northSide = SphericalUtil.computeOffset(latLng,5000.0,0.0)
              val southSide = SphericalUtil.computeOffset(latLng,5000.0,180.0)
  */

            val northSide = LatLng(latMax,lngMax)
            val southSide = LatLng(latMin,lngMin)
            val bounds = LatLngBounds.builder()
                .include(northSide)
                .include(southSide)
                .build()
            Timber.d("location bias detected.")

            Timber.d("northside: https://www.google.com/maps/place/${northSide.latitude},${northSide.longitude}")
            Timber.d("southside: https://www.google.com/maps/place/${southSide.latitude},${southSide.longitude}")
            Timber.d("path northside to southside: https://www.google.com/maps/dir/?api=1&origin=${northSide.latitude},${northSide.longitude}&destination=${southSide.latitude},${southSide.longitude}")
            RectangularBounds.newInstance(bounds)
        }else{
            null
        }
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountry("PH")
//        intent.build(requireContext())
        startActivityForResult(intent.setLocationBias(locationBias).build(requireContext()), requestCode)
    }

    private fun enableViews(enable: Boolean){
        btn_fb_signup.isEnabled = enable
        btn_google_signup.isEnabled = enable
        btn_rider.isEnabled = enable
        tv_sign_in_link.isEnabled = enable
    }
}