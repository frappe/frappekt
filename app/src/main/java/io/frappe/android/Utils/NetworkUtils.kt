package io.frappe.android.Utils

/**
 * Created by revant on 7/1/18.
 */
import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build

object NetworkUtils {

    fun isConnected(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun isWifiConnected(context: Context): Boolean {
        return isConnected(context, ConnectivityManager.TYPE_WIFI)
    }

    fun isMobileConnected(context: Context): Boolean {
        return isConnected(context, ConnectivityManager.TYPE_MOBILE)
    }

    private fun isConnected(context: Context, type: Int): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val networkInfo = connMgr.getNetworkInfo(type)
            return networkInfo != null && networkInfo.isConnected
        } else {
            return isConnected(connMgr, type)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun isConnected(connMgr: ConnectivityManager, type: Int): Boolean {
        val networks = connMgr.allNetworks
        var networkInfo: NetworkInfo?
        for (mNetwork in networks) {
            networkInfo = connMgr.getNetworkInfo(mNetwork)
            if (networkInfo != null && networkInfo.type == type && networkInfo.isConnected) {
                return true
            }
        }
        return false
    }
}