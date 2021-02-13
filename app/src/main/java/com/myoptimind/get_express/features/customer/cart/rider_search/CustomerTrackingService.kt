package com.myoptimind.get_express.features.customer.cart.rider_search

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.cart.data.GetCartInfoResponse
import com.myoptimind.get_express.features.customer.food_grocery.StoresRepository
import com.myoptimind.get_express.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CustomerTrackingService: LifecycleService() {

    private var isFirstRun = true
    private var isServiceKilled = false
    private var currentLocation: Location? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    @Inject
    lateinit var storesRepository: StoresRepository

    lateinit var cartId: String



    companion object {
        const val ACTION_START_OR_RESUME_SERVICE = "start_or_resume_service"
        const val ACTION_PAUSE_SERVICE           = "pause_service"
        const val ACTION_STOP_SERVICE            = "stop_service"
        const val ACTION_SHOW_CUSTOMER_REQUEST   = "show_customer_request"


        const val NOTIFICATION_CHANNEL_ID        = "tracking_channel_customer"
        const val NOTIFICATION_CHANNEL_NAME      = "Customer Tracking Channel"
        const val NOTIFICATION_ID                = 130

        const val EXTRA_CART_ID                  = "extra_cart_id"

        val cart = MutableLiveData<GetCartInfoResponse>()

        fun createIntent(context: Context, action: String, cartId: String): Intent  {
            return Intent(context, CustomerTrackingService::class.java).also {
                it.action = action
                it.putExtra(EXTRA_CART_ID,cartId)
                context.startService(it)
            }
        }

    }



    private var getCartInformationJob: Job? = null
    private fun getCartInformation(cartId: String) = GlobalScope.launch(IO){
        storesRepository.getCartInformation(cartId).collect {
            when(it){
                is Result.Progress ->{
                    if(it.isLoading){
                        Timber.d("Sending rider location...")
                    }
                }
                is Result.Success -> {
                    if(it.data != null){
                        Timber.d("result:${it.data.meta.message}")
                    }
                }
                is Result.Error -> {
                    Timber.d("result:${it.metaResponse.message}")
                }
                is Result.HttpError -> {
                    Timber.d("result:${it.error.message}")
                }
            }
        }
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun){
                        Timber.d("started service")
                        startForegroundService()
                        isFirstRun = false
                        isServiceKilled = false
                        cartId = it.extras?.get(EXTRA_CART_ID) as String
                    }else{
                        Timber.d("resuming service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("paused service")
                }
                ACTION_STOP_SERVICE -> {
                    this.stopService(intent)
                    this.stopForeground(true)
                    this.stopSelf()
                    isFirstRun = true
                    isServiceKilled = true
                    Timber.d("stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle("Get Express")
                .setContentText("Finding a driver")
                .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).also {
                it.action = ACTION_SHOW_CUSTOMER_REQUEST
            },
            FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        getCartInformationJob?.cancel()

        Timber.d("on destroy called.")
    }

}