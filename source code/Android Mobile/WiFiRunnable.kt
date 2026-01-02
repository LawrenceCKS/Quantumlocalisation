package com.example.navigator

import android.net.wifi.WifiManager
import android.os.Handler
import android.util.Log

class WiFiRunnable(mHandler : Handler, wifiManager : WifiManager ) : Runnable {
    var mHandler        : Handler
    var wifiManager     : WifiManager
    init{
        this.mHandler       = mHandler
        this.wifiManager    = wifiManager
        Log.d("WiFiResult" , "WiFiRunnable()")
    }
    override fun run(){
        try {
            val success = wifiManager!!.startScan()
            if (!success) {
                Log.d( "WiFiResult" , "startScan(failure)" )
            }
            else {
                Log.d( "WiFiResult" , "startScan(success)" )
            }
            mHandler.postDelayed( this , 4500 )
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }
    }
}