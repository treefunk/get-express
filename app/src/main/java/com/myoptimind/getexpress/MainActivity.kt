package com.myoptimind.getexpress

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.myoptimind.getexpress.features.login.data.UserType
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

        if(appSharedPref.getUserType() == UserType.RIDER){
            findNavController(R.id.nav_host_container).navigate(R.id.riderDashboardFragment)
        }

        tv_history.setOnClickListener {
            findNavController(R.id.nav_host_container).navigate(R.id.action_global_riderHistoryFragment)
        }

        ib_nav_back.setOnClickListener {
            onBackPressed()
        }

    }

    private fun initSideNav() {
        tv_edit_profile.setOnClickListener {
            findNavController(R.id.nav_host_container).navigate(R.id.action_global_editProfileFragment)
            if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
                drawer_layout.closeDrawers()
            } else {
                drawer_layout.openDrawer(GravityCompat.END)
            }
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

}