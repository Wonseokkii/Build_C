package com.ans.ans_multi

import com.ans.ans_multi.sensors.PolarSensor
import com.ans.ans_multi.sensors.SensorPlugin
import com.ans.ans_multi.sensors.registerSensorChannels

import android.util.Log
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }

    // For now: one sensor plugin. Later: Map<String, SensorPlugin> if you add more.
    private lateinit var polarSensor: SensorPlugin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBlePermissions()
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Example: register a single Polar sensor instance named "polar"
        val polarSensor = PolarSensor(
            context = applicationContext,
            name = "polar1" // you can pass ANY name here ("polar1", "myPolar", etc.)
        ).also {
            it.initialize()
        }

        registerSensorChannels(flutterEngine, polarSensor)

    }

    private fun requestBlePermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions += Manifest.permission.BLUETOOTH_SCAN
            permissions += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            permissions += Manifest.permission.ACCESS_FINE_LOCATION
            permissions += Manifest.permission.ACCESS_COARSE_LOCATION
        }

        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        polarSensor.shutdown()
    }
}
