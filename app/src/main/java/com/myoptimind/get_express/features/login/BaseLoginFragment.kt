package com.myoptimind.get_express.features.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.myoptimind.get_express.features.login.data.FacebookUserPayload
import com.myoptimind.get_express.features.login.data.GoogleUserPayload
import com.myoptimind.get_express.features.login.data.UserType
import timber.log.Timber


open class BaseLoginFragment(private val userType: UserType): Fragment() {

    companion object {
        private const val RC_SIGN_IN = 100
    }

    private val viewModel by activityViewModels<LoginViewModel>()

    private var facebookCallbackManager: CallbackManager? = null
    private var googleSignInClient: GoogleSignInClient? = null

    internal var customerFacebookListener: FacebookListener? = null
    internal var customerGoogleListener: GoogleListener? = null

    internal var riderFacebookListener: FacebookListener? = null
    internal var riderGoogleListener: GoogleListener? = null

    interface FacebookListener {
        fun onSuccessLogin(facebookUserPayload: FacebookUserPayload)
    }

    interface GoogleListener {
        fun onSuccessLogin(googleUserPayload: GoogleUserPayload)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFacebookLogin()
        initGoogleLogin()

        customerFacebookListener = object: FacebookListener {
            override fun onSuccessLogin(facebookUserPayload: FacebookUserPayload) {
                viewModel.updateFacebookPayload(
                        facebookUserPayload
                )
                viewModel.signInCustomerSocial (
                        facebookUserPayload.email,
                        facebookUserPayload.fbToken
                )
            }
        }
        customerGoogleListener = object : GoogleListener {
            override fun onSuccessLogin(googleUserPayload: GoogleUserPayload) {
                viewModel.updateGoogleUserPayload(
                        googleUserPayload
                )
                viewModel.signInCustomerSocial (
                        googleUserPayload.email,
                        googleUserPayload.googleToken
                )
            }
        }

        riderFacebookListener = object : FacebookListener{
            override fun onSuccessLogin(facebookUserPayload: FacebookUserPayload) {
                viewModel.updateFacebookPayload(
                        facebookUserPayload
                )
                viewModel.signInRiderSocial (
                        facebookUserPayload.email,
                        facebookUserPayload.fbToken
                )
            }
        }

        riderGoogleListener = object: GoogleListener{
            override fun onSuccessLogin(googleUserPayload: GoogleUserPayload) {
                viewModel.updateGoogleUserPayload(
                        googleUserPayload
                )
                viewModel.signInRiderSocial (
                        googleUserPayload.email,
                        googleUserPayload.googleToken
                )
            }
        }
    }

    private fun initGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }




    private fun initFacebookLogin() {

        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired

        facebookCallbackManager = CallbackManager.Factory.create()


        LoginManager.getInstance().registerCallback(facebookCallbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                Timber.v(" " + result.toString())
                val request = GraphRequest.newMeRequest(
                    result!!.accessToken
                ) { _, response ->
                    // Application code
                    Log.v("fblogin", "response ${response.rawResponse}")
                    val res = response.jsonObject

                    when(userType){
                        UserType.CUSTOMER -> {
                            customerFacebookListener?.onSuccessLogin(FacebookUserPayload(
                                    fbToken = res.getString("id"),
                                    email = res.getString("email"),
                                    firstName = res.getString("first_name"),
                                    lastName = res.getString("last_name")
                            ))
                        }
                        UserType.RIDER -> {
                            riderFacebookListener?.onSuccessLogin(FacebookUserPayload(
                                    fbToken = res.getString("id"),
                                    email = res.getString("email"),
                                    firstName = res.getString("first_name"),
                                    lastName = res.getString("last_name")
                            ))
                        }
                    }




//                    viewModel.loginSocialUser(res.getString("email"), res.getString("id"), res.getString("first_name") + " " + res.getString("last_name"))
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,email,first_name,last_name")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Timber.v("fb login cancelled")
            }

            override fun onError(error: FacebookException?) {
                Timber.v("ON ERROR")
            }
        })
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val name  = if(account.displayName != null) account.displayName else account.givenName
            val email = if(account.email != null) account.email else return
            val id    = if(account.id != null) account.id else return
            if(account.email != null && account.id != null && account.displayName != null){
                when(userType){
                    UserType.CUSTOMER -> {
                        customerGoogleListener?.onSuccessLogin(GoogleUserPayload(
                                id!!,
                                email!!,
                                name!!
                        ))
                    }
                    UserType.RIDER -> {
                        riderGoogleListener?.onSuccessLogin(GoogleUserPayload(
                                id!!,
                                email!!,
                                name!!
                        ))
                    }
                }

            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Timber.e(e.message)
            Timber.e(e.message)
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager?.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    internal fun loginWithFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
    }


    internal fun loginWithGoogle(){
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
}