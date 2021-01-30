package com.myoptimind.getexpress.features.rider.rider_dashboard

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.getexpress.MainActivity
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.edit_profile.EditProfileViewModel
import com.myoptimind.getexpress.features.login.data.Rider
import com.myoptimind.getexpress.features.login.data.RiderVehicle
import com.myoptimind.getexpress.features.login.data.User
import com.myoptimind.getexpress.features.login.data.UserType
import com.myoptimind.getexpress.features.rider.customer_requests_list.data.CustomerRequest
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.LogoOnlyFragment
import com.myoptimind.getexpress.features.shared.ProfileFragment
import com.myoptimind.getexpress.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.partial_profile_admin.*
import kotlinx.android.synthetic.main.partial_profile_admin.iv_profile_picture
import kotlinx.android.synthetic.main.partial_profile_admin.tv_edit_profile
import kotlinx.android.synthetic.main.partial_profile_admin.tv_history
import kotlinx.android.synthetic.main.partial_profile_admin.tv_profile_full_name
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val REFRESH_INTERVAL_SECOND = 10
@AndroidEntryPoint
class RiderDashboardFragment: ProfileFragment() {

    @Inject
    lateinit var appSharedPref: AppSharedPref

    var customerRequestListener: CustomerRequestListener? = null
    private val viewModel by activityViewModels<EditProfileViewModel>()


    companion object {
        const val REQUEST_IMAGE_CAPTURE = 111
        const val REQUEST_IMAGE_GET     = 222
    }

    private lateinit var currentPhotoPath: String


    fun navigateToSelectedCustomerRequest(cartId: String , isAccepted: Boolean = false, historyOnly: Boolean = false){
        findNavController().navigate(
                RiderDashboardFragmentDirections.actionRiderDashboardFragmentToSelectedCustomerRequestFragment(cartId,isAccepted,historyOnly)
        )
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_rider_dashboard,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUpload()

        viewModel.getRiderProfile()

        tv_edit_profile.setOnClickListener {
            Navigation.findNavController(requireActivity(),R.id.nav_host_admin_container).navigate(R.id.action_global_editProfileFragment)
        }

        tv_history.setOnClickListener {
            Navigation.findNavController(requireActivity(),R.id.nav_host_admin_container).navigate(R.id.action_global_riderHistoryFragment)
        }

        (activity as MainActivity).findViewById<TextView>(R.id.tv_history_side).setOnClickListener {
            Navigation.findNavController(requireActivity(),R.id.nav_host_admin_container).navigate(R.id.action_global_riderHistoryFragment)
            if ((activity as MainActivity).drawer_layout.isDrawerOpen(GravityCompat.END)) {
                (activity as MainActivity).drawer_layout.closeDrawers()
            } else {
                (activity as MainActivity).drawer_layout.openDrawer(GravityCompat.END)
            }
        }



        initObservers()

    }

    private fun initObservers() {
        viewModel.getRiderProfile.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if(result.data != null){
                        val rider = result.data.data
                        updateRiderLabels(rider)
                    }
                }
                is Result.Error -> {

                }
                is Result.HttpError -> {

                }
            }
        }
    }



    fun updateActiveVehicle(activeVehicle: RiderVehicle){
        tv_caption.text = "${activeVehicle.vehicleModel} | ${activeVehicle.plateNumber}"

    }

    fun updateRiderLabels(rider: Rider) {
        Glide.with(requireContext())
                .load(rider.profilePicture)
                .into(iv_profile_picture)

        tv_profile_full_name.text = "Welcome, ${rider.fullName}"
        updateActiveVehicle(rider.activeVehicle)
    }

    interface CustomerRequestListener {
        fun onClickCustomerRequest(customerRequest: CustomerRequest)
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



}