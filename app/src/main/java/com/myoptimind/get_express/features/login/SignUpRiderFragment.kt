package com.myoptimind.get_express.features.login

import android.app.Activity
import android.app.Activity.RESULT_OK
import androidx.lifecycle.observe
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import com.myoptimind.get_express.features.shared.*
import com.myoptimind.get_express.features.shared.DatePickerDialogFragment.Companion.EXTRA_DATE
import com.myoptimind.get_express.features.shared.api.Result
import kotlinx.android.synthetic.main.fragment_sign_up_rider.*
import kotlinx.android.synthetic.main.fragment_sign_up_rider.btn_customer
import kotlinx.android.synthetic.main.fragment_sign_up_rider.btn_sign_up
import kotlinx.android.synthetic.main.fragment_sign_up_rider.et_birth_date
import kotlinx.android.synthetic.main.fragment_sign_up_rider.et_email_address
import kotlinx.android.synthetic.main.fragment_sign_up_rider.et_fullname
import kotlinx.android.synthetic.main.fragment_sign_up_rider.et_location
import kotlinx.android.synthetic.main.fragment_sign_up_rider.et_mobile_number
import kotlinx.android.synthetic.main.fragment_sign_up_rider.et_password
import kotlinx.android.synthetic.main.fragment_sign_up_rider.tv_sign_in_link
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*



class SignUpRiderFragment : BaseLoginFragment(UserType.RIDER), EasyPermissions.PermissionCallbacks {

    private val viewModel by activityViewModels<LoginViewModel>()
    private lateinit var currentPhotoPath: String

    companion object {
//        private const val REQUEST_LOCATION = 150
        private const val REQUEST_IMAGE_CAPTURE = 111
        private const val REQUEST_IMAGE_GET     = 222
        private const val REQUEST_CODE_DATE_PICKER = 767
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && perms.contains(android.Manifest.permission.CAMERA) && perms.contains(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            initCaptureImage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(requestCode == REQUEST_IMAGE_CAPTURE){
            Toast.makeText(requireContext(),"Permission denied.",Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up_rider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        initObservers()
    }

    fun autoFillFieldsFromSocial(
            fullName: String,
            email: String,
            token: String
    ){

        et_fullname.setText(fullName)
        et_email_address.setText(email)
        et_email_address.isEnabled = false
        et_password.isEnabled = false
        et_password.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.grey_200_alpha20))

        btn_sign_up.setOnClickListener {
            if(et_fullname.izBlank() ||
                    et_email_address.izBlank() ||
                    et_birth_date.izBlank() ||
                    et_location.izBlank() ||
                    et_vehicle_type.izBlank() ||
                    et_vehicle_model.izBlank() ||
                    et_plate_number.izBlank()){
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


            viewModel.signUpRider(
                    et_fullname.text.toString(),
                    et_email_address.text.toString(),
                    et_mobile_number.text.toString(),
                    et_birth_date.text.toString(),
                    et_location.text.toString(),
                    null,
                    token,
                    "1",
                    et_vehicle_type.tag.toString(),
                    et_vehicle_model.text.toString(),
                    et_plate_number.text.toString()
            )
        }

    }

    private fun initObservers() {
        viewModel.facebookUserPayload.observe(viewLifecycleOwner) { facebookUserPayload ->
            if(facebookUserPayload != null){
                autoFillFieldsFromSocial(
                        facebookUserPayload.firstName + " " + facebookUserPayload.lastName,
                        facebookUserPayload.email,
                        facebookUserPayload.fbToken
                )
            }
        }

        viewModel.googleUserPayload.observe(viewLifecycleOwner){ googleUserPayload ->
            if(googleUserPayload != null){
                autoFillFieldsFromSocial(
                        googleUserPayload.name,
                        googleUserPayload.email,
                        googleUserPayload.googleToken
                )
            }
        }



        viewModel.uploadedLicense.observe(viewLifecycleOwner){ uploaded ->
            val choiceTakePhoto = getString(R.string.take_photo_label)
            val choiceChooseFromLib = getString(R.string.choose_from_library_label)
            val choiceViewCurrentPhoto = getString(R.string.view_current_photo_label)

            var choices = arrayOf(choiceTakePhoto, choiceChooseFromLib)

            if(uploaded != null){
                choices = arrayOf(choiceViewCurrentPhoto) + choices
                et_drivers_license.setText("Tap to view image...")
            }


            et_drivers_license.setOnClickListener {
                val permissions = arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if(EasyPermissions.hasPermissions(requireContext(),*permissions)){
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Select Photo source:")
                        .setItems(choices) { _, which ->
                            when (choices[which]) {
                                choiceTakePhoto -> {
                                    initCaptureImage()
                                }
                                choiceChooseFromLib -> {
                                    selectImage()
                                }
                                choiceViewCurrentPhoto -> {
                                    val viewUrlDialog = DialogViewUrl.newInstance(uploaded.absolutePath)
                                    viewUrlDialog.show(childFragmentManager,"view current photo")
                                }
                            }
                        }.show()
                }else{
                    EasyPermissions.requestPermissions(
                        PermissionRequest.Builder(this, REQUEST_IMAGE_CAPTURE, android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .build()
                    )
                }


            }
        }

        viewModel.vehicleList.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
//
                }
                is Result.Success -> {
                    if(result.data != null){
                        val vehicleListString = result.data.data.map { it.name }
                        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,vehicleListString)
                        et_vehicle_type.setAdapter(adapter)
                        et_vehicle_type.setOnClickListener {
                            et_vehicle_type.showDropDown()
                        }
                        et_vehicle_type.setOnItemClickListener { _, _, index, id ->
                            val selectedVehicle = result.data.data[index]
                                et_vehicle_type.tag = selectedVehicle.id.toString()
                                Timber.v("vehicle id selected: ${et_vehicle_type.tag}")
                                et_vehicle_type.setText(selectedVehicle.name,false)
                        }
                    }
                }
                is Result.Error -> {
                    //
                }
                is Result.HttpError -> {

                }
            }
        }

        viewModel.signInRiderResult.observe(viewLifecycleOwner){ result ->
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

        viewModel.signUpRiderResult.observe(viewLifecycleOwner){ result ->
                when (result) {
                    is Result.Progress -> {
                        Timber.v("loading ${result.isLoading}")
//                        btn_sign_up.isEnabled = false
                    }
                    is Result.Success -> {
                        if(result.data != null){
                            arrayOf(
                                et_fullname,
                                et_email_address,
                                et_password,
                                et_mobile_number,
                                et_birth_date,
                                et_location,
                                et_password,
                                et_vehicle_type,
                                et_vehicle_model,
                                et_plate_number
                            ).clearTextViews()
                            et_password.isEnabled = true
                            et_password.setBackgroundColor(ContextCompat.getColor(requireContext(),android.R.color.white))
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

        btn_customer.setOnClickListener {
            findNavController().navigate(R.id.action_signUpRiderFragment_to_signUpCustomerFragment)
        }
        tv_sign_in_link.setOnClickListener {
            findNavController().navigate(R.id.action_signUpRiderFragment_to_signInCustomerFragment)
        }

        btn_fb_signup.setOnClickListener {
            loginWithFacebook()
        }
        btn_google_signup.setOnClickListener {
            loginWithGoogle()
        }
/*
        et_location.setOnClickListener {
            showPlacesAutocomplete(REQUEST_LOCATION)
        }*/


        btn_sign_up.setOnClickListener {

            if(et_fullname.izBlank() ||
                    et_email_address.izBlank() ||
                    et_password.izBlank() ||
                    et_birth_date.izBlank() ||
                    et_location.izBlank() ||
                    et_vehicle_type.izBlank() ||
                    et_vehicle_model.izBlank() ||
                    et_plate_number.izBlank()){
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


            viewModel.signUpRider(
                et_fullname.text.toString(),
                et_email_address.text.toString(),
                et_mobile_number.text.toString(),
                et_birth_date.text.toString(),
                et_location.text.toString(),
                et_password.text.toString(),
                null,
                null,
                et_vehicle_type.tag.toString(),
                et_vehicle_model.text.toString(),
                et_plate_number.text.toString()
            )
        }

        et_birth_date.initDatePicker(
            parentFragmentManager,
            this@SignUpRiderFragment,
            "birth_date",
            REQUEST_CODE_DATE_PICKER
        )


    }

    private fun initCaptureImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.myoptimind.get_express.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(
                        takePictureIntent,
                        REQUEST_IMAGE_CAPTURE
                    )
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = timeStamp + ".jpg"
        val storageDir = File(requireContext().filesDir, "uploaded")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }else{
            // clear images if there's any
            storageDir.listFiles { file -> file.delete()
            }
        }
        val image = File(
            storageDir,
            imageFileName
        )
        currentPhotoPath = image.absolutePath
        return image
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
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





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val uploadedPhoto = File(currentPhotoPath)
            if(uploadedPhoto.exists()){
                Timber.v("uploaded file from camera exists")
            }
            viewModel.setUploadedLicense(uploadedPhoto)
            Timber.v("success upload")
        }else if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK && data != null) {
            val thumbnail: Bitmap? = data.getParcelableExtra("data")
            val fullPhotoUri: Uri? = data.data

            if(fullPhotoUri != null){
                createImageFile()
                val input = requireContext().contentResolver.openInputStream(fullPhotoUri)
                val output = FileOutputStream(currentPhotoPath)
                val buf = ByteArray(1024)
                var len: Int
                if(input != null){
                    while (input.read(buf).also { len = it } > 0) {
                        output.write(buf, 0, len)
                    }
                    output.close()
                    input.close()
                }
                val uploadedPhoto = File(currentPhotoPath)
                viewModel.setUploadedLicense(uploadedPhoto)
            }
        } else if(requestCode == REQUEST_CODE_DATE_PICKER && resultCode == Activity.RESULT_OK) {
            et_birth_date.setText(data?.getStringExtra(EXTRA_DATE))
        }
/*        else if(requestCode == REQUEST_LOCATION && resultCode == RESULT_OK){
            if(data != null){
                val place = Autocomplete.getPlaceFromIntent(data)
                Timber.d("Place: ${place.name}, ${place.id}")
                et_location.setText(place.name)
            }
        }*/
    }


}