package com.rcdriving.tankrtk

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private var tankNetworkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = TankViewModel()

        // Phone's own WiFi radio — used to show real signal strength to
        // the tank's access point (see the polling loop below). RSSI does
        // not require a runtime location permission, just the normal
        // ACCESS_WIFI_STATE manifest permission.
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        setContent {

            // ------------------------------------------------------------
            // GLOBAL APP STATE
            // ------------------------------------------------------------
            val status by viewModel.wifi.status.collectAsState()
            val connected = status == ConnectionStatus.CONNECTED

            var currentTab by remember { mutableStateOf(TopTab.MAIN) }

            // Switch the phone onto "TankAP" on launch, then connect.
            //
            // Android 10+ has WifiNetworkSpecifier — a local, app-scoped
            // WiFi request that doesn't touch the phone's saved network
            // list or its general internet route (other apps keep using
            // whatever network they were on; only this app's traffic goes
            // over TankAP, which is exactly right since TankAP has no
            // real internet to offer). The user gets a one-time system
            // dialog to approve it; Android doesn't allow apps to switch
            // WiFi networks completely invisibly, by design.
            //
            // On Android 7-9 (below API 29) this API doesn't exist, so we
            // fall back to the old behavior: the user has to join TankAP
            // manually in WiFi settings first, same as before.
            LaunchedEffect(Unit) {
                connectToTankAp(viewModel)
            }

            // Poll the phone's WiFi signal strength to "TankAP" and feed it
            // into the signal bars meter. Nothing about signal strength
            // comes from the Arduino — it's purely the phone's own radio.
            LaunchedEffect(Unit) {
                while (true) {
                    @Suppress("DEPRECATION")
                    val rssi = wifiManager.connectionInfo?.rssi
                    if (rssi != null) {
                        @Suppress("DEPRECATION")
                        val level = WifiManager.calculateSignalLevel(rssi, 5)
                        viewModel.updateSignal(level)
                    }
                    delay(2000)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                when (currentTab) {

                    TopTab.MAIN -> TankDriveScreen(
                        viewModel = viewModel,
                        connected = connected,
                        onMain = { currentTab = TopTab.MAIN },
                        onRecord = { currentTab = TopTab.RECORD },
                        onSettings = { currentTab = TopTab.SETTINGS }
                    )

                    TopTab.RECORD -> RecordScreen(
                        viewModel = viewModel,
                        connected = connected,
                        onMain = { currentTab = TopTab.MAIN },
                        onRecord = { currentTab = TopTab.RECORD },
                        onSettings = { currentTab = TopTab.SETTINGS }
                    )

                    TopTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        connected = connected,
                        status = status,
                        onMain = { currentTab = TopTab.MAIN },
                        onRecord = { currentTab = TopTab.RECORD },
                        onSettings = { currentTab = TopTab.SETTINGS },
                        onConnectTankAp = { connectToTankAp(viewModel) },
                        onDisconnectTankAp = { disconnectFromTankAp(viewModel) }
                    )
                }
            }
        }
    }

    // Shared by the launch-time auto-connect and the manual "Connect" button
    // on the Settings screen — same fallback behavior either way: try the
    // app-scoped WifiNetworkSpecifier request on Android 10+, fall back to
    // just connecting the TCP socket over whatever network is already
    // active (e.g. the user already joined TankAP manually, or is on an
    // older Android version that lacks this API).
    private fun connectToTankAp(viewModel: TankViewModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                requestTankNetwork(viewModel)
            } catch (e: Exception) {
                android.util.Log.e("TankRTK", "requestTankNetwork failed", e)
                viewModel.connect()
            }
        } else {
            viewModel.connect()
        }
    }

    // Releases the app's TankAP-specific network binding so the phone's
    // traffic (and this app's own connections) go back over whatever
    // network is normally active — mirrors the cleanup in onDestroy(), but
    // callable on demand from the "Disconnect" button, and also tears down
    // the TCP socket to the tank and stops the motors for safety.
    private fun disconnectFromTankAp(viewModel: TankViewModel) {
        viewModel.disconnect()

        tankNetworkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                android.util.Log.e("TankRTK", "unregisterNetworkCallback failed", e)
            }
        }
        tankNetworkCallback = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                connectivityManager.bindProcessToNetwork(null)
            } catch (e: Exception) {
                android.util.Log.e("TankRTK", "bindProcessToNetwork(null) failed", e)
            }
        }
    }

    // Requests the phone connect to TankAP as an app-scoped, local WiFi
    // network (Android 10+ only — gated by the SDK_INT check at the call
    // site). Once granted, binds this process's traffic to it so the TCP
    // connect to 192.168.4.1 actually goes out over TankAP rather than
    // whatever the phone's default network happens to be.
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestTankNetwork(viewModel: TankViewModel) {
        android.util.Log.d("TankRTK", "requestTankNetwork: building request for SSID=${Config.TANK_AP_SSID}")

        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(Config.TANK_AP_SSID)
            .setWpa2Passphrase(Config.TANK_AP_PASSWORD)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                android.util.Log.d("TankRTK", "TankAP network AVAILABLE: $network")
                try {
                    connectivityManager.bindProcessToNetwork(network)
                    android.util.Log.d("TankRTK", "bindProcessToNetwork succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("TankRTK", "bindProcessToNetwork failed", e)
                }
                viewModel.connect()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                android.util.Log.d("TankRTK", "TankAP network UNAVAILABLE (timed out, declined, or not in range)")
                // User declined the system prompt, or TankAP wasn't in
                // range — fall back to whatever network is already
                // active (e.g. the user already joined TankAP manually).
                viewModel.connect()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                android.util.Log.d("TankRTK", "TankAP network LOST: $network")
            }
        }

        tankNetworkCallback = callback
        android.util.Log.d("TankRTK", "requestTankNetwork: calling connectivityManager.requestNetwork")
        connectivityManager.requestNetwork(request, callback)
        android.util.Log.d("TankRTK", "requestTankNetwork: requestNetwork call returned (result is async)")
    }

    override fun onDestroy() {
        super.onDestroy()
        tankNetworkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectivityManager.bindProcessToNetwork(null)
        }
    }
}
