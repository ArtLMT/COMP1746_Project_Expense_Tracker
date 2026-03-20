package com.lmt.expensetracker.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utility object for network-related operations.
 *
 * Provides helper functions to check device connectivity status
 * using the modern [ConnectivityManager] and [NetworkCapabilities] APIs,
 * compatible with Android API 24 through API 36.
 */
object NetworkUtils {

    /**
     * Checks whether the device currently has an active network connection
     * via **Wi-Fi** or **Cellular** data.
     *
     * This implementation uses [ConnectivityManager.getActiveNetwork] and
     * [NetworkCapabilities], which is the recommended approach for API 23+
     * and fully compatible up to API 35/36. The deprecated
     * `getActiveNetworkInfo()` path is intentionally omitted because the
     * project's `minSdk` is 24.
     *
     * @param context The [Context] used to access system services.
     * @return `true` if the device has an active Wi-Fi or Cellular connection,
     *         `false` otherwise.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        val hasSupportedTransport =
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

        // INTERNET + VALIDATED reduces false positives on captive portals.
        val hasValidatedInternet =
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        return hasSupportedTransport && hasValidatedInternet
    }
}
