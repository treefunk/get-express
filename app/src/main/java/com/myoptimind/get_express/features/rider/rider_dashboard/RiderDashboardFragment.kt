package com.myoptimind.get_express.features.rider.rider_dashboard

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.edit_profile.ProfileViewModel
import com.myoptimind.get_express.features.login.data.Rider
import com.myoptimind.get_express.features.login.data.RiderVehicle
import com.myoptimind.get_express.features.rider.customer_requests_list.data.CustomerRequest
import com.myoptimind.get_express.features.rider.topup.MyWalletDialog
import com.myoptimind.get_express.features.rider.topup.RiderTopUpDialog
import com.myoptimind.get_express.features.rider.topup.TOPUP_PAYMENT_TYPE
import com.myoptimind.get_express.features.rider.topup.data.WalletOffer
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.ProfileFragment
import com.myoptimind.get_express.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.partial_nav_top.*
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
class RiderDashboardFragment : ProfileFragment() {

    @Inject
    lateinit var appSharedPref: AppSharedPref

    private val viewModel by activityViewModels<ProfileViewModel>()
    private val riderDashBoardViewModel by activityViewModels<RiderDashboardViewModel>()


    companion object {
        const val REQUEST_IMAGE_CAPTURE = 111
        const val REQUEST_IMAGE_GET = 222
        const val REQUEST_TOP_UP = 999
    }

    private lateinit var currentPhotoPath: String


    fun navigateToSelectedCustomerRequest(
        cartId: String,
        isAccepted: Boolean = false,
        historyOnly: Boolean = false
    ) {
        findNavController().navigate(
            RiderDashboardFragmentDirections.actionRiderDashboardFragmentToSelectedCustomerRequestFragment(
                cartId,
                isAccepted,
                historyOnly
            )
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_rider_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUpload()

        handleEditProfileHistoryUnderline()



        viewModel.getRiderProfile()

        tv_edit_profile.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_admin_container)
                .navigate(R.id.action_global_editProfileFragment)
        }

        tv_history.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_admin_container)
                .navigate(R.id.action_global_riderHistoryFragment)
        }

        (activity as MainActivity).findViewById<TextView>(R.id.tv_history_side).setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_admin_container)
                .navigate(R.id.action_global_riderHistoryFragment)
            if ((activity as MainActivity).drawer_layout.isDrawerOpen(GravityCompat.END)) {
                (activity as MainActivity).drawer_layout.closeDrawers()
            } else {
                (activity as MainActivity).drawer_layout.openDrawer(GravityCompat.END)
            }
        }



        initObservers()
        initClickListeners()

    }

    private fun handleEditProfileHistoryUnderline() {
        Navigation.findNavController(requireActivity(), R.id.nav_host_admin_container)
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.editProfileFragment -> {
                        tv_edit_profile.paintFlags = 0
                        tv_history.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    }
                    R.id.riderHistoryFragment -> {
                        tv_history.paintFlags = 0
                        tv_edit_profile.paintFlags = Paint.UNDERLINE_TEXT_FLAG

                    }
                    else -> {
                        tv_edit_profile.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                        tv_history.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    }
                }
            }
    }

    private fun initClickListeners() {

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun initObservers() {

        riderDashBoardViewModel.topUpWalletByBalanceResult.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
                        if(result.data.meta.status == 200){
                            Toast.makeText(requireContext(),"Processing payment... Please Wait.",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(),result.metaResponse.message,Toast.LENGTH_SHORT).show()
                }
                is Result.HttpError -> {
                    Toast.makeText(requireContext(),result.error.message,Toast.LENGTH_SHORT).show()
                }
            }
        }

        riderDashBoardViewModel.walletOffersResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
                        val dialogRiderTopUp = RiderTopUpDialog.newInstance(
                            result.data.data,
                            result.data.meta.topUpDescription
                        )
                        dialogRiderTopUp.setTargetFragment(
                            this@RiderDashboardFragment,
                            REQUEST_TOP_UP
                        )
                        dialogRiderTopUp.show(parentFragmentManager, "TOPUP_DIALOG")
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

        riderDashBoardViewModel.topUpWalletResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
                        if (result.data.meta.status == 200) {
                            val topUpResult = result.data.data
//                            val fragment = TopUpWebViewFragment.newInstance(topUpResult.checkoutUrl)
                            RiderDashboardFragmentDirections.actionRiderDashboardFragmentToTopUpWebViewFragment(
                                topUpResult.checkoutUrl
                            ).also {
                                findNavController().navigate(it)
                            }
                        }
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

        riderDashBoardViewModel.updateCashOnHandResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
                        if (result.data.meta.status == 200) {
                            val topUpResult = result.data.data
//                            val fragment = TopUpWebViewFragment.newInstance(topUpResult.checkoutUrl)
                            Toast.makeText(
                                requireContext(),
                                "Successfully Updated Cash on hand.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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

        riderDashBoardViewModel.addWalletBalanceResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
                        if (result.data.meta.status == 200) {
                            val topUpResult = result.data.data
//                            val fragment = TopUpWebViewFragment.newInstance(topUpResult.checkoutUrl)
                            RiderDashboardFragmentDirections.actionRiderDashboardFragmentToTopUpWebViewFragment(
                                topUpResult.checkoutUrl
                            ).also {
                                findNavController().navigate(it)
                            }
                        }
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



        viewModel.getRiderProfile.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Progress -> {

                }
                is Result.Success -> {
                    if (result.data != null) {
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


    fun updateActiveVehicle(activeVehicle: RiderVehicle) {
        tv_caption.text = "${activeVehicle.vehicleModel} | ${activeVehicle.plateNumber}"
        appSharedPref.updateActiveVehicle(activeVehicle)
    }

    fun updateRiderLabels(rider: Rider) {
        Glide.with(requireContext())
            .load(rider.profilePicture)
            .into(iv_profile_picture)

        tv_profile_full_name.text = "Welcome, ${rider.fullName}!"
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
        } else {
            // clear images if there's any
            storageDir.listFiles { file ->
                file.delete()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val uploadedPhoto = File(currentPhotoPath)
            if (uploadedPhoto.exists()) {
                Timber.v("uploaded file from camera exists")
            }
            viewModel.setUploadedPicture(uploadedPhoto)
            Timber.v("success upload")
            Glide.with(requireContext())
                .load(uploadedPhoto)
                .into(iv_profile_picture)
        } else if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK && data != null) {
            val thumbnail: Bitmap? = data.getParcelableExtra("data")

            val fullPhotoUri: Uri? = data.data
            Glide.with(requireContext())
                .load(fullPhotoUri)
                .into(iv_profile_picture)
            if (fullPhotoUri != null) {
                createImageFile()
                val input = requireContext().contentResolver.openInputStream(fullPhotoUri)
                val output = FileOutputStream(currentPhotoPath)
                val buf = ByteArray(1024)
                var len: Int
                if (input != null) {
                    while (input.read(buf).also { len = it } > 0) {
                        output.write(buf, 0, len)
                    }
                    output.close()
                    input.close()
                }
                val uploadedPhoto = File(currentPhotoPath)
                viewModel.setUploadedPicture(uploadedPhoto)
            }
        } else if (requestCode == REQUEST_TOP_UP && resultCode == Activity.RESULT_OK) {
            val walletOffer =
                data?.getParcelableExtra<WalletOffer>(RiderTopUpDialog.DATA_WALLET_OFFER)
            val paymentType =
                data?.getParcelableExtra<TOPUP_PAYMENT_TYPE>(RiderTopUpDialog.DATA_PAYMENT_TYPE)

            if (walletOffer != null) {
                when (paymentType) {
                    TOPUP_PAYMENT_TYPE.GCASH -> riderDashBoardViewModel.topUpWallet(
                        appSharedPref.getUserId(),
                        walletOffer.name,
                        walletOffer.price,
                        walletOffer.durationInHours
                    )
                    TOPUP_PAYMENT_TYPE.WALLET -> riderDashBoardViewModel.topUpWalletByBalance(
                        appSharedPref.getUserId(),
                        walletOffer.name,
                        walletOffer.price,
                        walletOffer.durationInHours
                    )
                    null -> Toast.makeText(requireContext(), "Invalid Payment", Toast.LENGTH_SHORT)
                        .show()
                }

            }

        }
    }


}