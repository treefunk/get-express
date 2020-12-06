package com.myoptimind.getexpress.features.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.myoptimind.getexpress.MainActivity
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity: AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var job: Job
    @Inject
    lateinit var sharedPref: AppSharedPref

    private suspend fun switchScreen(){
        for (i in 1..3){
//            Log.v("Splashscreen", "${i}.. before switch")
//            delay(1000)
        }
        if(isUserLoggedIn()){
            redirectAuthenticatedUser()
        }else{
            setContentView(R.layout.activity_login)
            setUpDestinationListener()
            loadFeaturedServices()

        }

    }

    private fun setUpDestinationListener() {
        findNavController(R.id.nav_host_login).addOnDestinationChangedListener { controller, destination, arguments ->
            Timber.v("going to ${destination.label}")
            when(destination.id){
                R.id.signInCustomerFragment, R.id.signInRiderFragment, R.id.forgotPasswordFragment -> {
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(parent_login_view)
                    constraintSet.constrainHeight(R.id.nav_host_login, MATCH_CONSTRAINT)
                    constraintSet.connect(R.id.nav_host_login, BOTTOM, PARENT_ID, BOTTOM)
                    constraintSet.applyTo(parent_login_view)
                }
                else -> {
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(parent_login_view)
                    constraintSet.constrainHeight(R.id.nav_host_login, WRAP_CONTENT)
                    constraintSet.clear(R.id.nav_host_login, BOTTOM)
                    constraintSet.applyTo(parent_login_view)
                }
            }
        }
    }


    private fun loadFeaturedServices() {
        val adapter = LoginFeaturedServicesAdapter(ArrayList())

        vp_login_featured.adapter = adapter
        vp_login_featured.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(tab_layout_login_featured,vp_login_featured){ _,_ ->
            // empty
        }.attach()

        viewModel.servicesResult.observe(this){ result ->
            when(result){
                is Result.Progress -> {
                    Timber.v("loading services ${result.isLoading}")
                }
                is Result.Success -> {
                    if(result.data != null){
                        adapter.loginFeaturedServices = result.data.data
                        vp_login_featured.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                super.onPageSelected(position)
                                Timber.v("page #${position + 1}")
                            }
                        })
                        adapter.notifyDataSetChanged()
                    }
                }
                is Result.Error -> {
                    Timber.e(result.metaResponse.message)
                }
                is Result.HttpError -> {
                    Timber.e(result.error)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("login oncreate")
//        viewModel.getServices()
        job = lifecycleScope.launchWhenResumed {
            switchScreen()
        }
    }

    private fun isUserLoggedIn(): Boolean = sharedPref.getUserId().isNotEmpty()

    private fun redirectAuthenticatedUser(){
        this@LoginActivity.finish()
        startActivity(
            Intent(this, MainActivity::class.java)
        )
    }

}