package com.myoptimind.get_express

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

open class BaseActivity: AppCompatActivity() {
    companion object {
        private const val RC_UPDATE = 111
    }

    override fun onResume() {
        super.onResume()
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener {
            if(it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                appUpdateManager.startUpdateFlowForResult(
                    it,
                    AppUpdateType.IMMEDIATE,
                    this,
                    RC_UPDATE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_UPDATE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this,"Please update to latest version before proceeding.", Toast.LENGTH_LONG).show()
                finishAndRemoveTask()
            }else{
                Toast.makeText(this,"Application successfully updated", Toast.LENGTH_LONG).show()
            }
        }
    }
}