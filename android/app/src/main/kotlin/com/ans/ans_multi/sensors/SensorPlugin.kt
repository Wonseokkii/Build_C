package com.ans.ans_multi.sensors

import io.flutter.plugin.common.EventChannel

/**
 * Unified interface for any sensor SDK we integrate (Polar, Garmin, etc).
 * MainActivity only talks to this interface, not to Polar-specific classes.
 */
interface SensorPlugin {

    /** Unique name for the sensor, e.g. "polar" */
    val name: String

    /** Initialize the sensor SDK */
    fun initialize() {}

    /** Set or clear the EventSink that sends events to Flutter */
    fun setEventSink(sink: EventChannel.EventSink?)

    /** Start scanning / discovery for devices */
    fun startScan()

    /** Connect to a specific device by ID (MAC / deviceId etc.) */
    fun connect(id: String)

    /** Start ECG streaming (if supported by this sensor) */
    fun startEcg(id: String) {
        throw UnsupportedOperationException("ECG not supported")
    }

    /** Start accelerometer streaming (if supported) */
    fun startAcc(id: String) {
        throw UnsupportedOperationException("ACC not supported")
    }

    /** Cleanup resources, disposables, shutdown SDK, etc. */
    fun shutdown()
}
