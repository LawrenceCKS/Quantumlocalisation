package com.example.navigator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log

class WiFiScanner( wifiManager : WifiManager , wiFiDeviceList : MutableList<ScanResult> ): BroadcastReceiver() {
    var wifiManager     : WifiManager
    var wiFiDeviceList  : MutableList<ScanResult>
    init {
        this.wifiManager    = wifiManager
        this.wiFiDeviceList = wiFiDeviceList
        Log.d("WiFiResult" , "wifiScanReceiver()")
    }
    override fun onReceive(context: Context, intent: Intent) {
        try {
                Log.d("WiFiResult" , "onReceive()")
                val action = intent!!.action
                if( action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION ) {
                    this.wiFiDeviceList!!.clear()
                    this.wifiManager!!.scanResults.sortByDescending { it.level }
                    val results    : List<ScanResult>  = this.wifiManager!!.scanResults
                    for(result in results) {
                        try{
                            if( result.SSID.startsWith("69xXAnonymousXx69") ){
                                this.wiFiDeviceList!!.add( result )
                            }
                        } catch (exception: Exception) {
                            Log.d( "WiFiResult" , exception.toString() )
                        }
                    }
                    for(result in results) {
                        try{
                            if( result.SSID.startsWith("JUb4oa3ahq") ){
                                this.wiFiDeviceList!!.add( result )
                            }
                        } catch (exception: Exception) {
                            Log.d( "WiFiResult" , exception.toString() )
                        }
                    }
                    for(result in results) {
                        try{
                            if( result.SSID.startsWith("SIT-WIFI") ){
                                this.wiFiDeviceList!!.add( result )
                            }
                        } catch (exception: Exception) {
                            Log.d( "WiFiResult" , exception.toString() )
                        }
                    }
                    for(result in results) {
                        try{
                            if( !result.SSID.startsWith("69xXAnonymousXx69") && !result.SSID.startsWith("JUb4oa3ahq") && !result.SSID.startsWith("SIT-WIFI") ){
                                this.wiFiDeviceList!!.add( result )
                            }
                        } catch (exception: Exception) {
                            Log.d( "WiFiResult" , exception.toString() )
                        }
                    }
                }
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }
    }
}