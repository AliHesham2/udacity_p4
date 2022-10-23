package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment

object PermissionChecking {
    private var backgroundPermission : Boolean = false
    private val qOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


     fun checkPermissionsAndStartGeofencing(context: Context) {
        if (foregroundAndBackgroundLocationPermissionApproved(context)) {
            checkDeviceLocationSettingsAndStartGeofence(true,context)
        } else {
            requestForegroundAndBackgroundLocationPermissions(context)
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {
        val foregroundLocationApproved = (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))

        val backgroundPermissionApproved =
            if (qOrLater) { PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }

        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions(context: Context) {
        if (foregroundAndBackgroundLocationPermissionApproved(context)) return


        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            qOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                SaveReminderFragment.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> SaveReminderFragment.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        ActivityCompat.requestPermissions(context as RemindersActivity, permissionsArray, resultCode)
    }


    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true,context: Context) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(context)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(context as RemindersActivity, SaveReminderFragment.REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    backgroundPermission = false
                }
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                backgroundPermission = true
            }
        }
    }

    fun setPermission(value:Boolean){ backgroundPermission = value }
    fun getPermission(): Boolean {return backgroundPermission}

}