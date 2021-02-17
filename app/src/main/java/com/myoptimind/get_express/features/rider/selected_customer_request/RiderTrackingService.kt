package com.myoptimind.get_express.features.rider.selected_customer_request

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.myoptimind.get_express.MainActivity
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.api.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RiderTrackingService: LifecycleService() {


    private var isFirstRun = true
    private var isServiceKilled = false
    private var sendCoordinates = false
    private var currentLocation: Location? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    @Inject
    lateinit var customerRequestRepository: CustomerRequestRepository

    @Inject
    lateinit var appSharedPref: AppSharedPref

    var cartId: String? = null



    companion object {
        private const val EXTRA_SEND_COORDINATES = "extra_send_coordinates"

        const val ACTION_START_OR_RESUME_SERVICE = "start_or_resume_service"
        const val ACTION_PAUSE_SERVICE           = "pause_service"
        const val ACTION_STOP_SERVICE            = "stop_service"
        const val ACTION_SHOW_CUSTOMER_REQUEST   = "show_customer_request"


        const val NOTIFICATION_CHANNEL_ID        = "tracking_channel"
        const val NOTIFICATION_CHANNEL_NAME      = "Rider Tracking Channel"
        const val NOTIFICATION_ID                = 118

        const val EXTRA_CART_ID                  = "extra_cart_id"

        val latLong = MutableLiveData<LatLng?>()

        fun createIntent(context: Context, action: String, cartId: String?, sendCoordinates: Boolean): Intent  {
            return Intent(context, RiderTrackingService::class.java).also {
                it.action = action
                it.putExtra(EXTRA_CART_ID,cartId)
                it.putExtra(EXTRA_SEND_COORDINATES,sendCoordinates)
                context.startService(it)
            }
        }

    }



    private var sendLocationJob: Job? = null
    private fun sendLocation(cartId: String, lat: Double, longi: Double) = GlobalScope.launch(IO){
        customerRequestRepository.sendRiderCurrentLocation(cartId,lat,longi).collect {
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



    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            super.onLocationResult(locationResult)
            if(isServiceKilled.not()){
                if (locationResult?.lastLocation != null) {
                    currentLocation = locationResult.lastLocation.also {
                        val latLng = LatLng(it.latitude,it.longitude)
                        latLong.postValue(latLng)

                        if(cartId != null && (sendLocationJob == null || sendLocationJob!!.isCompleted) && sendCoordinates){
                            Timber.d("cart id $cartId")
                            sendLocationJob?.cancel()
                            sendLocationJob = sendLocation(cartId!!,it.latitude,it.longitude)
//                        sendLocationJob?.start()
                        }
                    }
                    Timber.d("location: lat: ${currentLocation?.latitude} long: ${currentLocation?.longitude}")
                } else {
                    Timber.d("Location information isn't available.")
                }
            }

        }
    }

    private fun initGPS () {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(10)
            fastestInterval = TimeUnit.SECONDS.toMillis(5)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
/*            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ), REQUEST_PERMISSION_COARSE_LOCATION)*/
            return
        }
/*        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            if(!checkPermissionsAndroidP()){
                return
            }
        }*/
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
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
                        if(it.extras?.get(EXTRA_CART_ID) != null){
                            cartId = it.extras?.get(EXTRA_CART_ID) as String
                        }else{
                            cartId = null
                        }
                        sendCoordinates = it.extras?.get(EXTRA_SEND_COORDINATES) as Boolean
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
            .setContentText("Tracking your location..")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
        initGPS()

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
        sendLocationJob?.cancel()

        Timber.d("on destroy called.")
    }

}