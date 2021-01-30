package com.myoptimind.getexpress

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.Places
import com.myoptimind.getexpress.features.login.LoginActivity
import com.myoptimind.getexpress.features.login.data.UserType
import com.myoptimind.getexpress.features.rider.selected_customer_request.RiderTrackingService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.hide
import com.myoptimind.getexpress.features.shared.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.partial_nav_top.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appSharedPref: AppSharedPref

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




        tv_logout.setOnClickListener {
            this.finish()
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
            appSharedPref.removeUserId()
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



}