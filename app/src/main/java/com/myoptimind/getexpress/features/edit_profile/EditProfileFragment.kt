package com.myoptimind.getexpress.features.edit_profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.getexpress.BaseFragment
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.login.DialogViewUrl
import com.myoptimind.getexpress.features.login.data.RiderVehicle
import com.myoptimind.getexpress.features.login.data.User
import com.myoptimind.getexpress.features.login.data.UserType
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.TitleOnlyFragment
import com.myoptimind.getexpress.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.et_fullname
import kotlinx.android.synthetic.main.fragment_edit_profile.et_location

import kotlinx.android.synthetic.main.fragment_sign_up_rider.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class EditProfileFragment: TitleOnlyFragment() {

    companion object {
        const val REQUEST_ADD_VEHICLE = 111
        const val REQUEST_IMAGE_CAPTURE = 111
        const val REQUEST_IMAGE_GET     = 222
    }

    private val viewModel by activityViewModels<EditProfileViewModel>()
    override fun getTitle() = "Edit Profile"
    private lateinit var currentPhotoPath: String


    private lateinit var myVehicleAdapter: RiderVehicleAdapter

    @Inject
    lateinit var appSharedPref: AppSharedPref

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initUpload()
        initClickListeners()
    }

    private fun initClickListeners() {
        btn_save_edits_rider.setOnClickListener {
            if(et_new_password.text.toString().isNotBlank() &&
                    (!et_new_password.text.toString().equals(et_retype_password.text.toString()))
            ){
                Toast.makeText(requireContext(),"Password does not match",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.updateProfile(
                    et_fullname.text.toString().ifBlank { null },
                    et_email.text.toString().ifBlank { null },
                    et_mobile.text.toString().ifBlank { null },
                    et_birthdate.text.toString().ifBlank { null },
                    et_location.text.toString().ifBlank { null },
                    et_new_password.text.toString().ifBlank { null }
            )
        }
    }

    private fun initUpload() {
        val choiceTakePhoto = getString(R.string.take_photo_label)
        val choiceChooseFromLib = getString(R.string.choose_from_library_label)
        var choices = arrayOf(choiceTakePhoto, choiceChooseFromLib)


        iv_profile_picture.setOnClickListener {
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
                        }
                    }.show()
        }

    }

    override fun onResume() {
        super.onResume()
        when(appSharedPref.getUserType()){
            UserType.CUSTOMER -> {
                viewModel.getCustomerProfile()
            }
            UserType.RIDER -> {
                viewModel.getRiderProfile()
            }
        }
    }

    private fun initObservers() {
        when(appSharedPref.getUserType()){
            UserType.CUSTOMER -> {
                initCustomer()
            }
            UserType.RIDER -> {
                initRider()
            }
        }

        viewModel.updateProfileResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {
                 //todo
                }
                is Result.Success -> {
                    if(result.data != null){
                        Toast.makeText(requireContext(),result.data.meta.message,Toast.LENGTH_LONG).show()
                        val user = result.data.data
                        appSharedPref.storeLoginCredentials(
                                user.id,
                                appSharedPref.getUserType().label,
                                user.profilePicture,
                                user.fullName
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.metaResponse.message)
                }
                is Result.HttpError -> {
                    Timber.e(result.error.message)
                }
            }

        }
    }

    private fun initRider() {

        myVehicleAdapter = RiderVehicleAdapter(ArrayList(), object: RiderVehicleAdapter.RiderVehicleListener{
            override fun onPressed(riderVehicle: RiderVehicle, index: Int) {
                Timber.v("riderVehicle $riderVehicle"  )
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("My Vehicles")
                        .setMessage("Are you sure you want to change the active vehicle?")
                        .setNeutralButton("CANCEL") { dialog, which ->
                            // Respond to neutral button press
                        }
                        .setPositiveButton("ACCEPT") { dialog, which ->
                            viewModel.changeActiveVehicle(riderVehicle.id)
                        }
                        .show()
            }
        })
        rv_my_vehicles.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        rv_my_vehicles.adapter = myVehicleAdapter

        viewModel.getRiderProfile.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {

                        val rider = result.data.data
                        fillCommonFields(rider)
//                                et_vehicle_model.setText(rider.activeVehicle.vehicleModel)
//                                et_plate_number.setText(rider.activeVehicle.plateNumber)
//                                initMyVehicles()
                        myVehicleAdapter.myVehicles = result.data.data.vehicles
                        myVehicleAdapter.activeVehicle = result.data.data.activeVehicle
                        myVehicleAdapter.notifyDataSetChanged()
                        tv_add_vehicle_link.setOnClickListener {
                            val dialogAddVehicle = DialogAddVehicle()
                            dialogAddVehicle.show(parentFragmentManager,"add_vehicle")
                        }
                    }
                }
                is Result.Error -> {

                }
                is Result.HttpError -> {

                }
            }
        }

        viewModel.changeActiveVehicleResponse.observe(viewLifecycleOwner){ result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        Toast.makeText(requireContext(),result.data.meta.message,Toast.LENGTH_LONG).show()
                        myVehicleAdapter.activeVehicle = result.data.data.activeVehicle
                        myVehicleAdapter.myVehicles = result.data.data.vehicles
                        myVehicleAdapter.notifyDataSetChanged()
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(),result.metaResponse.message,Toast.LENGTH_LONG).show()
                }
                is Result.HttpError -> {
                    Toast.makeText(requireContext(),result.error.message,Toast.LENGTH_LONG).show()
                }
            }
        }


    }

    private fun initCustomer() {
        group_my_vehicle.visibility = View.GONE
        group_address.visibility = View.VISIBLE
        viewModel.getCustomerProfile.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        val customer = result.data.data
                        fillCommonFields(customer)
                    }

                    tv_add_address_link.setOnClickListener {
                        val addAddressDialog = DialogAddAddress.newInstance()
                        addAddressDialog.setTargetFragment(this@EditProfileFragment, REQUEST_ADD_VEHICLE)
                        addAddressDialog.show(childFragmentManager,"ADD_ADDRESS")
                    }

                }
                is Result.Error -> {

                }
                is Result.HttpError -> {

                }
            }
        }
    }


    private fun fillCommonFields(user: User){
        et_fullname.setText(user.fullName)
        et_email.setText(user.email)
        et_mobile.setText(user.mobileNum)
        et_birthdate.setText(user.birthdate)
        et_location.setText(user.location)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val uploadedPhoto = File(currentPhotoPath)
            if(uploadedPhoto.exists()){
                Timber.v("uploaded file from camera exists")
            }
            viewModel.setUploadedPicture(uploadedPhoto)
            Timber.v("success upload")
        }else if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK && data != null) {
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
                viewModel.setUploadedPicture(uploadedPhoto)
            }
        }
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
}