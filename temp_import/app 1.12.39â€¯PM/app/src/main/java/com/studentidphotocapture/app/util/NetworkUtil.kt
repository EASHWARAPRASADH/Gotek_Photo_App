package com.studentidphotocapture.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtil {
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    fun schedulePhotoUpload(context: Context) {
        if (isNetworkAvailable(context)) {
            // Trigger upload worker
            android.content.Intent(context, com.studentidphotocapture.app.workmanager.PhotoUploadService::class.java).also {
                context.startService(it)
            }
        }
    }
}
