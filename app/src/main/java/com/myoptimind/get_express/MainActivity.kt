package com.myoptimind.get_express

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.Places
import com.myoptimind.get_express.features.login.LoginActivity
import com.myoptimind.get_express.features.login.LoginViewModel
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.rider.selected_customer_request.RiderTrackingService
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.hide
import com.myoptimind.get_express.features.shared.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.partial_nav_top.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appSharedPref: AppSharedPref

    private val loginViewModel by viewModels<LoginViewModel>()


    companion object {
        private const val ABOUT_URL                = "https://getexpress.ph/about-us/"
        private const val FAQS_URL                 = "https://getexpress.ph/faqs/"
        private const val CUSTOMER_SERVICE_URL     = "https://getexpress.ph/contact-us/"
        private const val TERMS_AND_CONDITIONS_URL = "https://getexpress.ph/terms-and-conditions/"
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        val fragmentList: List<*> = supportFragmentManager.fragments

        var handled = false
        for (f in fragmentList) {
            if (f is BaseFragment) {
                handled = (f as BaseFragment).onBackPressed()
                if (handled) {
                    break
                }
            }
        }

        if (!handled) {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSideNav()
        initPlaces()



        if(appSharedPref.getUserType() == UserType.RIDER){

            findNavController(R.id.nav_host_container).navigate(R.id.action_homeFragment_to_riderDashboardFragment)
//            tv_history.setOnClickListener {
//                findNavController(R.id.nav_host_container).navigate(R.id.action_global_riderHistoryFragment)
//            }
        }
        ib_nav_back.setOnClickListener {
            onBackPressed()
        }

        navigateToCustomerRequestList(intent)

        ib_top_menu.setOnClickListener {
            if(drawer_layout.isDrawerVisible(GravityCompat.END)){
                drawer_layout.closeDrawers()
            }else{
                drawer_layout.openDrawer(GravityCompat.END)
            }
        }

        loginViewModel.logoutResult.observe(this){ result ->
            when(result){
                is Result.Progress -> {
                }
                is Result.Success -> {
                    if (result.data != null) {
                        if (result.data.meta.status == 200) {
                            this.finish()
                            startActivity(
                                Intent(this, LoginActivity::class.java)
                            )
                            appSharedPref.removeUserId()
                        }
                    }
                }
                is Result.Error -> {
                    Toast.makeText(this, result.metaResponse.message, Toast.LENGTH_SHORT).show()
                }
                is Result.HttpError -> {
                    Toast.makeText(this, result.error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    fun updateSideNavName(fullname: String){
        tv_profile_full_name.text = fullname
    }

    private fun initPlaces() {
        // Initialize the SDK
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)

//        // Create a new PlacesClient instance
//        val placesClient = Places.createClient(this)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToCustomerRequestList(intent)
    }

    private fun initSideNav() {

        updateSideNavName(appSharedPref.getUserFullname())



        if(appSharedPref.getUserType() == UserType.RIDER){
            tv_edit_profile.setOnClickListener {
                findNavController(R.id.nav_host_admin_container).navigate(R.id.action_global_editProfileFragment)
                if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
                    drawer_layout.closeDrawers()
                } else {
                    drawer_layout.openDrawer(GravityCompat.END)
                }
            }
        }else {
            tv_edit_profile.setOnClickListener {
                findNavController(R.id.nav_host_container).navigate(R.id.action_global_customerProfileFragment)
                if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
                    drawer_layout.closeDrawers()
                } else {
                    drawer_layout.openDrawer(GravityCompat.END)
                }
            }
        }

        tv_about_get_express.setOnClickListener {
            openWebPage(ABOUT_URL)
        }

        tv_faqs.setOnClickListener {
            openWebPage(FAQS_URL)
        }

        tv_customer_service.setOnClickListener {
            openWebPage(CUSTOMER_SERVICE_URL)
        }

        tv_terms_and_conditions.setOnClickListener {
            openWebPage(TERMS_AND_CONDITIONS_URL)
        }






        tv_logout.setOnClickListener {
            loginViewModel.logout()
        }
    }

    internal fun showTitleOnly() {
        iv_nav_logo.hide()
        ib_top_menu.hide()

        ib_nav_back.show()
        nav_top_title.show()
    }

    internal fun showLogoOnly() {

        ib_nav_back.hide()
        nav_top_title.hide()

        iv_nav_logo.show()
        ib_top_menu.show()
    }

    private fun navigateToCustomerRequestList(intent: Intent?){
        if(intent?.action == RiderTrackingService.ACTION_SHOW_CUSTOMER_REQUEST){
            nav_host_container.findNavController().navigate(R.id.action_global_riderDashboardFragment)
        }
    }

    private fun openWebPage(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }



}