package com.example.navigator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.rtt.WifiRttManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.example.navigator.ui.theme.NavigatorTheme
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Math.pow
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : ComponentActivity() {
    //-----------------------------------------------------------------------
    lateinit var rttManager       : WifiRttManager
    lateinit var wifiManager      : WifiManager
    lateinit var conntManager     : ConnectivityManager
    lateinit var wifiScanReceiver : BroadcastReceiver
    lateinit var runTask          : WiFiRunnable
    private  var locationAlpha    : String = "A"
    private  var locationNum      : String = "1"
    private  var sLocation        : String = "A1"
    private  var location         : MutableState<String> = mutableStateOf(sLocation)
    private var aCnt              : Int = 0
    private var bCnt              : Int = 0
    private var cCnt              : Int = 0
    private var jCnt              : Int = 0
    private val aCount            : MutableState<Int> = mutableStateOf(aCnt)
    private val bCount            : MutableState<Int> = mutableStateOf(bCnt)
    private val cCount            : MutableState<Int> = mutableStateOf(cCnt)
    private val jCount            : MutableState<Int> = mutableStateOf(jCnt)
    //-----------------------------------------------------------------------
    private  val wiFiDeviceList : MutableList<ScanResult>  = mutableStateListOf<ScanResult>()
    private  val intentFilter                              = IntentFilter()
    private  val mHandler                                  = Handler()

    private  val MY_PERMISSIONS_NEARBY_WIFI_DEVICES    = 1
    private  val MY_PERMISSIONS_ACCESS_FINE_LOCATION   = 2
    private  val MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 3
    private  val MY_PERMISSIONS_CHANGE_WIFI_STATE      = 4
    private  val MY_PERMISSIONS_ACCESS_WIFI_STATE      = 5
    private  val MY_PERMISSIONS_INTERNET               = 6
    private  val MY_PERMISSIONS_ACCESS_NETWORK_STATE   = 7
    //-----------------------------------------------------------------------

    private fun checkWiFiAccess(wifiManager : WifiManager){
        try {
            if(!wifiManager!!.isWifiEnabled) {
                wifiManager!!.setWifiEnabled(true)
            }

            if( hasValidInternetConnection() ) {
                Log.d("WiFiResult" , "This device is connected to WiFi!" )
            } else {
                Log.d("WiFiResult" , "This device is not connected to any WiFi!" )
            }

            if( packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT) ) {
                try {
                        if( ActivityCompat.checkSelfPermission( this , android.Manifest.permission.NEARBY_WIFI_DEVICES ) != PackageManager.PERMISSION_GRANTED ) {
                            // requires API level >= 33
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(android.Manifest.permission.NEARBY_WIFI_DEVICES),
                                MY_PERMISSIONS_NEARBY_WIFI_DEVICES
                            )
                        }
                } catch (exception: Exception) {
                    Log.d( "WiFiResult" , exception.toString() )
                }
            }

            if( ActivityCompat.checkSelfPermission( this , android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this , arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),MY_PERMISSIONS_ACCESS_FINE_LOCATION)
            }
            if( ActivityCompat.checkSelfPermission( this , android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this , arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),MY_PERMISSIONS_ACCESS_COARSE_LOCATION)
            }
            if( ActivityCompat.checkSelfPermission( this , android.Manifest.permission.CHANGE_WIFI_STATE ) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this , arrayOf(android.Manifest.permission.CHANGE_WIFI_STATE),MY_PERMISSIONS_CHANGE_WIFI_STATE)
            }
            if( ActivityCompat.checkSelfPermission( this , android.Manifest.permission.ACCESS_WIFI_STATE ) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this , arrayOf(android.Manifest.permission.ACCESS_WIFI_STATE),MY_PERMISSIONS_ACCESS_WIFI_STATE)
            }
            if( ActivityCompat.checkSelfPermission( this , android.Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this , arrayOf(android.Manifest.permission.INTERNET),MY_PERMISSIONS_INTERNET)
            }
            if( ActivityCompat.checkSelfPermission( this , android.Manifest.permission.ACCESS_NETWORK_STATE ) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this , arrayOf(android.Manifest.permission.ACCESS_NETWORK_STATE),MY_PERMISSIONS_ACCESS_NETWORK_STATE)
            }

        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
             if( packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT) ) {
                 rttManager  = getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager
             } else {
                 Log.d("WiFiResult","This Android device does not support FTM/RTT ranging")
             }
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }

        try {
            conntManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }

        try {
                Log.d("WiFiResult" , "onCreate()")
                wifiManager = getSystemService(Context.WIFI_SERVICE)             as WifiManager
                checkWiFiAccess( wifiManager )
                wifiScanReceiver = WiFiScanner( wifiManager , wiFiDeviceList )

                try{
                    runTask = WiFiRunnable( mHandler , wifiManager )
                } catch (exception: Exception) {
                    Log.d( "WiFiResult" , exception.toString() )
                }

                setContent {
                    NavigatorTheme {
                        GUI()
                    }
                }
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        try{
            Log.d("WiFiResult" , "onPostResume()")

            intentFilter.addAction( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION )
            registerReceiver( wifiScanReceiver , intentFilter )

        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }
    }

    override fun onPause(){
        super.onPause()
        try{
            Log.d("WiFiResult" , "onPause()")
            unregisterReceiver(wifiScanReceiver)
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            Log.d("WiFiResult" , "onRequestPermissionsResult()")
            if( grantResults.isNotEmpty() ) {
                if( requestCode == MY_PERMISSIONS_ACCESS_FINE_LOCATION ) {
                    if( grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                        Log.d("WiFiResult" , "onRequestPermissionsResult(success)")
                    } else {
                        Log.d("WiFiResult" , "onRequestPermissionsResult(failure)")
                    }
                }
                if( requestCode == MY_PERMISSIONS_ACCESS_COARSE_LOCATION ) {
                    if( grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                        Log.d("WiFiResult" , "onRequestPermissionsResult(success)")
                    } else {
                        Log.d("WiFiResult" , "onRequestPermissionsResult(failure)")
                    }
                }
                if( requestCode == MY_PERMISSIONS_CHANGE_WIFI_STATE ) {
                    if( grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                        Log.d("WiFiResult" , "onRequestPermissionsResult(success)")
                    } else {
                        Log.d("WiFiResult" , "onRequestPermissionsResult(failure)")
                    }
                }
                if( requestCode == MY_PERMISSIONS_ACCESS_WIFI_STATE ) {
                    if( grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                        Log.d("WiFiResult" , "onRequestPermissionsResult(success)")
                    } else {
                        Log.d("WiFiResult" , "onRequestPermissionsResult(failure)")
                    }
                }
            }
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }
    }

    private fun startScan(){
        try {
                runTask!!.run()
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }
    }

    private fun stopScan(){
        try {
            mHandler!!.removeCallbacks( runTask )
            aCnt = 0
            bCnt = 0
            cCnt = 0
            jCnt = 0
            aCount.value = aCnt
            bCount.value = bCnt
            cCount.value = cCnt
            jCount.value = jCnt
            Toast.makeText( this , "Wifi Scan stopped" , Toast.LENGTH_SHORT ).show()
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
        }
    }

    private fun hasValidInternetConnection() : Boolean {
        try {
             val activeNetwork = conntManager!!.activeNetworkInfo
             val isConnected  = activeNetwork!!.isConnected() ?: return false
             return isConnected
//            val activeNetwork = conntManager!!.activeNetwork
//            val connectivity  = conntManager!!.getNetworkCapabilities(activeNetwork) ?: return false
//            return connectivity.hasTransport( NetworkCapabilities.TRANSPORT_WIFI )
        } catch (exception: Exception) {
            Log.d( "WiFiResult" , exception.toString() )
            return false
        }
    }

    @Composable
    fun GUI(){

        ConstraintLayout( modifier = Modifier
                                    .fillMaxSize()
        ) {
            val (banner,options,display,controls) = createRefs()

            Row( verticalAlignment = Alignment.CenterVertically ,
                 horizontalArrangement = Arrangement.Center ,
                 modifier = Modifier
                     .fillMaxWidth()
                     .fillMaxHeight(0.04F)
                     .background(Color.Yellow)
                     .constrainAs(banner) {
                         top.linkTo(parent.top)
                         bottom.linkTo(options.top)
                     }
            ){
                banner()
            }

            Row( verticalAlignment = Alignment.CenterVertically ,
                 horizontalArrangement = Arrangement.Center ,
                 modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.07F)
                .background(Color.LightGray)  // DarkGray
                .constrainAs(options) {
                    top.linkTo(banner.bottom)
                    bottom.linkTo(display.top)
                }
            ){
                options()
            }

            Column( modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82F)
                .background(Color.DarkGray)
                .constrainAs(display) {
                    top.linkTo(options.bottom)
                    bottom.linkTo(controls.top)
                }
            ){
                recyclerView(wiFiDeviceList)
            }

            Row( verticalAlignment = Alignment.CenterVertically ,
                 horizontalArrangement = Arrangement.Center ,
                 modifier = Modifier
                     .fillMaxWidth()
                     .fillMaxHeight(0.07F)
                     .background(Color.DarkGray)
                     .constrainAs(controls) {
                         top.linkTo(display.bottom)
                         bottom.linkTo(parent.bottom)
                     }
            ){
                controls()
            }
        }
    }

    @Composable()
    fun banner(){
        Text("Kee Boon Hwee's Capstone Project 2024" )
    }

    @Composable
    fun controls(){

        Button( onClick = {

                    startScan()
                } ,
                    content = { Text("Scan Nearby WiFi") } )

        Box( modifier = Modifier.size(10.dp) )

        Button( onClick = {

                    stopScan()
                } ,
                    content = { Text("Stop Scan") } )
    }

    @Composable
    fun options(){

        val alphaOptions = listOf("A","B","C","D","E","F","G","H","I","J","K")
        val numOptions   = listOf("1","2","3","4","5","6")

        val (alphaSelected , setAlpha) = remember {  mutableStateOf( alphaOptions[0])  }
        val (numSelected   , setNum)   = remember {  mutableStateOf(   numOptions[0])  }
        val _location                  = remember {  location  }

        val aCounter = remember { aCount }
        val bCounter = remember { bCount }
        val cCounter = remember { cCount }
        val jCounter = remember { jCount }

        Text( text = _location.value ,
              textAlign = TextAlign.Center,
              modifier = Modifier
                        .background( Color.Green)
                        .width(30.dp)
                        .border( 1.dp , Color.Blue )
        )

        Box( modifier = Modifier.size(8.dp) )
        spinner("Alpha : " , alphaOptions , alphaSelected , setAlpha )
        Box( modifier = Modifier.size(8.dp) )
        spinner("Number : " , numOptions , numSelected , setNum )

        Column() {
            Row( verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center ,
            ) {
                Text( text = aCounter.value.toString() ,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background( colorResource(R.color.pink) )
                        .width(33.dp)
                        .border( 1.dp , Color.Blue )
                )

                Text( text = bCounter.value.toString() ,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background( colorResource(R.color.lightyellow) )
                        .width(33.dp)
                        .border( 1.dp , Color.Blue )
                )
            }

            Row( verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center ,
            ) {
                Text( text = cCounter.value.toString() ,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background( colorResource(R.color.lightgreen) )
                        .width(33.dp)
                        .border( 1.dp , Color.Blue )
                )
                Text( text = jCounter.value.toString() ,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background( colorResource(R.color.lightblue) )
                        .width(33.dp)
                        .border( 1.dp , Color.Blue )
                )
            }
        }
//        Box( modifier = Modifier.size(8.dp) )}

//        Box( modifier = Modifier.size(8.dp) )

    }

    @Composable
    fun spinner( label:String , choices:List<String> , defaultValue:String , setSelected:(selected:String) -> Unit ){

        var spinnerText by remember { mutableStateOf( "" ) }
        var isExpanded by remember { mutableStateOf( false ) }

        Text( text = label )

        Box( modifier = Modifier
                        .border(width=1.dp,color=Color.Black)
        ) {
            Row( verticalAlignment     = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.Center ,
                 modifier = Modifier
                            .clickable {
                                        isExpanded = !isExpanded
                                       }
            ) {
                Text( text = defaultValue ,
                      textAlign = TextAlign.Center ,
                      modifier = Modifier
                                    .width(30.dp)
                                    .align(Alignment.CenterVertically)
                )
                Icon(imageVector = Icons.Filled.ArrowDropDown , contentDescription = "" )
                DropdownMenu(   expanded         =   isExpanded,
                                onDismissRequest = { isExpanded = false }
                ) {
                    choices.forEach { choice ->
                        DropdownMenuItem(
                            text = { Text( text = choice , textAlign = TextAlign.Center ) } ,
                            onClick = {
                                isExpanded = false
                                spinnerText = choice
                                setSelected( spinnerText )
                                if( label == "Alpha : " ){
                                    locationAlpha = choice
                                }
                                if( label == "Number : " ){
                                    locationNum   = choice
                                }
                                sLocation = locationAlpha + locationNum
                                location.value = sLocation
                                Log.d("WiFiResult" ,sLocation + " selected" )
                            })
                    }
                }
            }
        }
    }

    @Composable
    fun recyclerView( wiFiDeviceList : MutableList<ScanResult> ) {
        
        val deviceItems = remember{ wiFiDeviceList }

        LazyColumn( modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 5.dp)
        ) {
            items( items = deviceItems ) {
                RSSI( it )
                var accessPoint : String = ""
                val powerMW = pow( 10.0 , it.level.toDouble() / 10.0 )
                try{
                    accessPoint  = "scanned : " + it.SSID.toString() + " [" + it.BSSID.toString() + "] (" + it.level.toString() + " , " + powerMW.toBigDecimal().toPlainString() + ")"
                } catch (exception: Exception) {
                    try{
                        accessPoint  = "scanned : " + it.SSID.toString() + " (" + it.level.toString() + " , " + powerMW.toBigDecimal().toPlainString() + ")"
                    } catch (exception: Exception) {
                        Log.d( "WiFiResult" , "recycler : " + exception.toString() )
                    }
                }

                //  || it.SSID.toString().startsWith("SIT-WIFI")
                if( it.SSID.toString().startsWith("JUb4oa3ahq") || it.SSID.toString().startsWith("69xXAnonymousXx69")  ) {
                    if(hasValidInternetConnection()) {

                        val jsonObject = JSONObject()
                        jsonObject.put("tile"  , sLocation )
                        jsonObject.put("ssid"  , it.SSID.toString() )
                        jsonObject.put("rss"   , it.level.toString() )
                        jsonObject.put("power" , powerMW.toBigDecimal().toPlainString() )
                        val jsonObjectString = jsonObject.toString()

                        Thread( {
                            Log.d( "WiFiResult" , accessPoint )
                            try {
                                val connection = URL("http://192.168.0.124:8686/api/scan").openConnection() as HttpURLConnection
                                connection.requestMethod = "POST"
                                connection.setRequestProperty("Content-Type","application/json")
                                connection.setRequestProperty("Accept","application/json")
                                connection.connectTimeout = 1000
                                connection.readTimeout    = 1000
                                connection.doInput        = true
                                connection.doOutput       = true

                                try {
                                    val outputStreamWriter = OutputStreamWriter( connection.outputStream )
                                    outputStreamWriter.write( jsonObjectString )
                                    outputStreamWriter.flush()

                                    val responseCode = connection.responseCode
                                    try {
                                        val reader = InputStreamReader( connection.inputStream )
                                        reader.use {
                                                result ->
                                            val response = StringBuilder()
                                            val bufferedReader = BufferedReader(result)
                                            bufferedReader.forEachLine {
                                                response.append( it.trim() )
                                            }
                                            Log.d("WiFiResult","Response : " + response.toString() )
                                        }
                                    } catch (exception: Exception) {
                                        Log.d( "WiFiResult" , exception.toString() )
                                    }
                                } catch (exception: Exception) {
                                    Log.d( "WiFiResult" , "[" + sLocation + "] : " +  exception.toString() )
                                }

                            } catch (exception: Exception) {
                                Log.d( "WiFiResult" , exception.toString() )
                            }

                        }).start()
                    }
                }
            }
        }
    }

    @Composable
    fun RSSI( scanResult : ScanResult ){

        if( scanResult.SSID.startsWith("69xXAnonymousXx69-1") )
        {
            Column( modifier = Modifier
                .background(Color.DarkGray)
                .fillMaxWidth()
                .padding(5.dp)
                .background(colorResource(R.color.pink), shape = RoundedCornerShape(10.dp))
            ) {
                Row {
                    Text( " SSID : " )
                    Text( scanResult.SSID.toString() + " [" + scanResult.BSSID.toString() + "]"  )
                }
                Row {

                    Text( " RSS (dBm) : " )
                    Text( scanResult.level.toString() )
                }
                Row {
                    val powerMW = pow( 10.0 , scanResult.level.toDouble() / 10.0 )
                    Text( " Power (mw) : " )
                    Text(  powerMW.toBigDecimal().toPlainString() )
                }
                aCnt += 1
                aCount.value = aCnt
            }
        } else if( scanResult.SSID.startsWith("69xXAnonymousXx69-2") ) {
            Column( modifier = Modifier
                .background(Color.DarkGray)
                .fillMaxWidth()
                .padding(5.dp)
                .background(colorResource(R.color.lightyellow), shape = RoundedCornerShape(10.dp))
            ) {
                Row {
                    Text( " SSID : " )
                    Text( scanResult.SSID.toString() + " [" + scanResult.BSSID.toString() + "]" )
                }
                Row {
                    Text( " RSS (dBm) : " )
                    Text( scanResult.level.toString() )
                }
                Row {
                    val powerMW = pow( 10.0 , scanResult.level.toDouble() / 10.0 )
                    Text( " Power (mw) : " )
                    Text(  powerMW.toBigDecimal().toPlainString() )
                }
                bCnt += 1
                bCount.value = bCnt
            }
        } else if( scanResult.SSID.startsWith("69xXAnonymousXx69-3") ) {
            Column( modifier = Modifier
                .background(Color.DarkGray)
                .fillMaxWidth()
                .padding(5.dp)
                .background(colorResource(R.color.lightgreen), shape = RoundedCornerShape(10.dp))
            ) {
                Row {
                    Text( " SSID : " )
                    Text( scanResult.SSID.toString() + " [" + scanResult.BSSID.toString() + "]" )
                }
                Row {
                    Text( " RSS (dBm) : " )
                    Text( scanResult.level.toString() )
                }
                Row {
                    val powerMW = pow( 10.0 , scanResult.level.toDouble() / 10.0 )
                    Text( " Power (mw) : " )
                    Text(  powerMW.toBigDecimal().toPlainString() )
                }
                cCnt += 1
                cCount.value = cCnt
            }
        } else if( scanResult.SSID.startsWith("JUb4oa3ahq") ) {
            Column( modifier = Modifier
                .background(Color.DarkGray)
                .fillMaxWidth()
                .padding(5.dp)
                .background(colorResource(R.color.lightblue ), shape = RoundedCornerShape(10.dp))
            ) {
                Row {
                    Text( " SSID : " )
                    Text( scanResult.SSID.toString() + " [" + scanResult.BSSID.toString() + "]" )
                }
                Row {
                    Text( " RSS (dBm) : " )
                    Text( scanResult.level.toString() )
                }
                Row {
                    val powerMW = pow( 10.0 , scanResult.level.toDouble() / 10.0 )
                    Text( " Power (mw) : " )
                    Text(  powerMW.toBigDecimal().toPlainString() )
                }
                jCnt += 1
                jCount.value = jCnt
            }
        } else if( scanResult.SSID.startsWith("SIT-WIFI") ) {
            Column( modifier = Modifier
                .background(Color.DarkGray)
                .fillMaxWidth()
                .padding(5.dp)
                .background(colorResource(R.color.gold ), shape = RoundedCornerShape(10.dp))
            ) {
                Row {
                    Text( " SSID : " )
                    Text( scanResult.SSID.toString() + " [" + scanResult.BSSID.toString() + "]" )
                }
                Row {
                    Text( " RSS (dBm) : " )
                    Text( scanResult.level.toString() )
                }
                Row {
                    val powerMW = pow( 10.0 , scanResult.level.toDouble() / 10.0 )
                    Text( " Power (mw) : " )
                    Text(  powerMW.toBigDecimal().toPlainString() )
                }
//                jCnt += 1
//                jCount.value = jCnt
            }
        } else {
            Column( modifier = Modifier
                .background(Color.DarkGray)
                .fillMaxWidth()
                .padding(5.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(10.dp))
            ) {
                Row {
                    Text( " SSID : " )
                    Text( scanResult.SSID.toString() )
                }
                Row {
                    Text( " RSS (dBm) : " )
                    Text( scanResult.level.toString() )
                }
                Row {
                    val powerMW = pow( 10.0 , scanResult.level.toDouble() / 10.0 )
                    Text( " Power (mw) : " )
                    Text(  powerMW.toBigDecimal().toPlainString() )
                }
            }
        }
    }

    @Preview( showBackground=true )
    @Composable
    fun DefaultPreview(){
        NavigatorTheme{
            GUI()
        }
    }

}
