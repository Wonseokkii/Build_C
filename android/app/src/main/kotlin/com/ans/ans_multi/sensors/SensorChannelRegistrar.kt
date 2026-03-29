package com.ans.ans_multi.sensors

import android.util.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

/**
 * Registers a SensorPlugin using dynamic names:
 * sensors/<sensor.name>/methods
 * sensors/<sensor.name>/events
 */
fun registerSensorChannels(
    flutterEngine: FlutterEngine,
    sensor: SensorPlugin
) {
    val messenger = flutterEngine.dartExecutor.binaryMessenger

    val methodChannelName = "sensors/${sensor.name}/methods"
    val eventChannelName  = "sensors/${sensor.name}/events"

    Log.d("SensorRegistrar", "Registering channels for ${sensor.name}")
    Log.d("SensorRegistrar", "Method channel = $methodChannelName")
    Log.d("SensorRegistrar", "Event channel =  $eventChannelName")

    // ---------------------------
    // EVENT CHANNEL (Android → Flutter)
    // ---------------------------
    EventChannel(messenger, eventChannelName)
        .setStreamHandler(object : EventChannel.StreamHandler {

            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                Log.d("SensorRegistrar", "[${sensor.name}] EventChannel LISTEN")
                sensor.setEventSink(events)
            }

            override fun onCancel(arguments: Any?) {
                Log.d("SensorRegistrar", "[${sensor.name}] EventChannel CANCEL")
                sensor.setEventSink(null)
            }
        })

    // ---------------------------
    // METHOD CHANNEL (Flutter → Android)
    // ---------------------------
    MethodChannel(messenger, methodChannelName)
        .setMethodCallHandler { call, result ->

            when (call.method) {

                "startScan" -> {
                    sensor.startScan()
                    result.success(true)
                }

                "connect" -> {
                    val id = call.argument<String>("id")
                    if (id != null) {
                        sensor.connect(id)
                        result.success(true)
                    } else {
                        result.error("NO_ID", "Device id is required", null)
                    }
                }

                "startEcg" -> {
                    val id = call.argument<String>("id")
                    if (id != null) {
                        try {
                            sensor.startEcg(id)
                            result.success(true)
                        } catch (e: UnsupportedOperationException) {
                            result.error("UNSUPPORTED", e.message, null)
                        }
                    } else result.error("NO_ID", "Device id missing", null)
                }

                "startAcc" -> {
                    val id = call.argument<String>("id")
                    if (id != null) {
                        try {
                            sensor.startAcc(id)
                            result.success(true)
                        } catch (e: UnsupportedOperationException) {
                            result.error("UNSUPPORTED", e.message, null)
                        }
                    } else result.error("NO_ID", "Device id missing", null)
                }

                else -> result.notImplemented()
            }
        }
}
