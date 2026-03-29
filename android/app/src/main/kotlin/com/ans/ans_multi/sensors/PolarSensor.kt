package com.ans.ans_multi.sensors

import android.content.Context
import android.util.Log
import com.polar.androidcommunications.api.ble.model.DisInfo
import com.polar.androidcommunications.api.ble.model.gatt.client.ChargeState
import com.polar.androidcommunications.api.ble.model.gatt.client.PowerSourcesState

import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApi.PolarBleSdkFeature
import com.polar.sdk.api.PolarBleApiCallbackProvider
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.*

import io.flutter.plugin.common.EventChannel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.UUID

/**
 * Polar-specific implementation of SensorPlugin.
 * Wraps PolarBleApi and translates events into simple maps for Flutter.
 */
class PolarSensor(
    private val context: Context,
    override val name: String
) : SensorPlugin {

    private lateinit var api: PolarBleApi
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var eventSink: EventChannel.EventSink? = null

    private val disposables = CompositeDisposable()


    override fun initialize() {
        api = PolarBleApiDefaultImpl.defaultImplementation(
            context,
            setOf(
                PolarBleSdkFeature.FEATURE_HR,
                PolarBleSdkFeature.FEATURE_DEVICE_INFO,
                PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING
            )
        )

        api.setApiCallback(object : PolarBleApiCallbackProvider {

            override fun batteryChargingStatusReceived(
                identifier: String,
                chargingStatus: ChargeState
            ) {
                Log.d("Polar", "Battery charging status for $identifier: $chargingStatus")
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d("Polar", "Battery level for $identifier: $level%")
            }

            override fun blePowerStateChanged(powered: Boolean) {
                Log.d("Polar", "BLE Power state changed. Powered: $powered")
            }

            override fun bleSdkFeatureReady(
                identifier: String,
                feature: PolarBleSdkFeature
            ) {
                Log.d("Polar", "SDK Feature ready for $identifier: $feature")

                mainHandler.post {
                    eventSink?.success(
                        mapOf(
                            "sensor" to name,
                            "event" to "featureReady",
                            "id" to identifier,
                            "feature" to "onlineStreaming"
                        )
                    )
                }

            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("Polar", "Connected: ${polarDeviceInfo.deviceId}")

                mainHandler.post {
                    eventSink?.success(
                        mapOf(
                            "sensor" to name,
                            "event" to "deviceConnected",
                            "id" to polarDeviceInfo.deviceId
                        )
                    )
                }
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("Polar", "Connecting to: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("Polar", "Disconnected: ${polarDeviceInfo.deviceId}")

                mainHandler.post {
                    eventSink?.success(
                        mapOf(
                            "sensor" to name,
                            "event" to "deviceDisconnected",
                            "id" to polarDeviceInfo.deviceId
                        )
                    )
                }
            }

            override fun disInformationReceived(
                identifier: String,
                disInfo: DisInfo
            ) {
                Log.d("Polar", "DIS info for $identifier: $disInfo")
            }

            override fun disInformationReceived(
                identifier: String,
                uuid: UUID,
                value: String
            ) {
                Log.d("Polar", "DIS info (uuid) for $identifier → $uuid : $value")
            }

            override fun hrNotificationReceived(
                identifier: String,
                data: PolarHrData.PolarHrSample
            ) {
                Log.d("Polar", "HR ($identifier): ${data.hr}")

                mainHandler.post {
                    eventSink?.success(
                        mapOf(
                            "sensor" to name,
                            "event" to "hr",
                            "id" to identifier,
                            "hr" to data.hr
                        )
                    )
                }
            }

            override fun htsNotificationReceived(
                identifier: String,
                data: PolarHealthThermometerData
            ) {
                Log.d("Polar", "Temperature data from $identifier: $data")
            }

            override fun powerSourcesStateReceived(
                identifier: String,
                powerSourcesState: PowerSourcesState
            ) {
                Log.d("Polar", "Power sources state for $identifier: $powerSourcesState")
            }
        })
    }

    // ========== SensorPlugin implementation ==========

    override fun setEventSink(sink: EventChannel.EventSink?) {
        eventSink = sink
    }

    override fun startScan() {
        val disposable = api.searchForDevice()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { info ->
                    Log.d("Polar", "Found: ${info.deviceId}")

                    mainHandler.post {
                        eventSink?.success(
                            mapOf(
                                "sensor" to name,
                                "event" to "deviceFound",
                                "id" to info.deviceId,
                                "name" to info.name
                            )
                        )
                    }
                },
                { err ->
                    Log.e("Polar", "Scan error: $err")
                }
            )
        disposables.add(disposable)
    }

    override fun connect(id: String) {
        api.connectToDevice(id)
    }

    override fun startEcg(id: String) {
        val disposable = api.requestStreamSettings(id, PolarBleApi.PolarDeviceDataType.ECG)
            .flatMapPublisher { settings ->
                api.startEcgStreaming(id, settings)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ecgData ->
                    ecgData.samples.forEach { sample ->
                        when (sample) {
                            is EcgSample -> {
                                Log.d("Polar", "ECG sample class = ${sample::class}")
                                mainHandler.post {
                                    eventSink?.success(
                                        mapOf(
                                            "sensor" to name,
                                            "event" to "ecg",
                                            "id" to id,
                                            "sample" to sample.voltage
                                        )
                                    )
                                }
                            }

                            is FecgSample -> {
                                // If you want fetal ECG, decide what to send.
                                Log.d("Polar", "ECG sample class = ${sample::class}")
                                mainHandler.post {
                                    eventSink?.success(
                                        mapOf(
                                            "sensor" to name,
                                            "event" to "fecg",
                                            "id" to id,
                                            "ecg" to sample.ecg,
                                            "bioz" to sample.bioz,
                                            "status" to sample.status.toInt()
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                { error ->
                    Log.e("Polar", "ECG error: $error")
                }
            )
        disposables.add(disposable)
    }

    override fun startAcc(id: String) {
        val disposable = api.requestStreamSettings(id, PolarBleApi.PolarDeviceDataType.ACC)
            .flatMapPublisher { settings ->
                api.startAccStreaming(id, settings)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ acc: PolarAccelerometerData ->
                acc.samples.forEach { sample ->
                    // Each ACC sample has X,Y,Z
                    Log.d("Polar", "ACC data x y z subscribed")
                    mainHandler.post {
                        eventSink?.success(
                            mapOf(
                                "sensor" to name,
                                "event" to "acc",
                                "id" to id,
                                "x" to sample.x,
                                "y" to sample.y,
                                "z" to sample.z
                            )
                        )
                    }
                }
            }, { error ->
                Log.e("Polar", "ACC error: $error")
            })
        disposables.add(disposable)
    }

    override fun shutdown() {
        disposables.clear()
        api.shutDown()
    }
}
