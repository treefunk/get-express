package com.myoptimind.getexpress.features.login

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.*
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.izNotBlank
import com.myoptimind.suzuki_app.features.shared.DatePickerDialogFragment
import kotlinx.android.synthetic.main.fragment_sign_up_customer.*
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
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val REQUEST_IMAGE_CAPTURE = 111
private const val REQUEST_IMAGE_GET     = 222
private const val REQUEST_CODE_DATE_PICKER = 767

class SignUpRiderFragment : Fragment(){

    private val viewModel by activityViewModels<LoginViewModel>()
    private lateinit var currentPhotoPath: String

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

    private fun initObservers() {
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

        viewModel.signUpRiderResult.observe(viewLifecycleOwner){ result ->
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
                                et_location,
                                et_password,
                                et_vehicle_type,
                                et_vehicle_model,
                                et_plate_number
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
        btn_customer.setOnClickListener {
            findNavController().navigate(R.id.action_signUpRiderFragment_to_signUpCustomerFragment)
        }
        tv_sign_in_link.setOnClickListener {
            findNavController().navigate(R.id.action_signUpRiderFragment_to_signInCustomerFragment)
        }

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
                        "com.myoptimind.getexpress.fileprovider",
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
            et_birth_date.setText(data?.getStringExtra(DatePickerDialogFragment.EXTRA_DATE))
        }
    }


}