package com.vivalnk.sdk.demo.vital.ui.device.o2.components

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import java.util.concurrent.ArrayBlockingQueue

class Drawer_WaveForm_SpO2(val waveView: WaveViewRTS) {
    private val datas = ArrayBlockingQueue<WaveViewRTS.PointModel>(300)
    private var RUN = false
    private var offset = 1000L
    private var latestPulsePoint = 0
    private lateinit var thread: Thread
    private var handlerThread: HandlerThread = HandlerThread(this.javaClass.name)
    private var handler: Handler

    init {
        handlerThread.start()

        handler = object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    0 -> {
                        if (msg.obj != null) {
                            val ecg = msg.obj as WaveViewRTS.PointModel
                            datas.put(ecg)
                        }
                    }
                    1 -> {
                        if (msg.obj != null) {
                            val pulse = msg.obj as WaveViewRTS.PointModel
                            datas.put(pulse)
                        }
                    }
                }

                super.handleMessage(msg)
            }
        }

        start()
    }

    private fun start() {
        RUN = true
        thread = Thread(Runnable {
            while (RUN) {
                if (datas.size <= 0) {
                    offset = 1
                } else {
                    offset = 1000L / datas.size
                }

                var point: WaveViewRTS.PointModel? = null
                try {
                    point = datas.take()
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }

                if (point == null) {
                    return@Runnable
                }

                waveView.addEcgPoint(point)

                try {
                    Thread.sleep(offset)
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }
            }
        })
        thread.start()
    }

    fun addWaveForm(data: Map<String, Object>) {
        val pr = data["pr"] as Int
        if (pr <= 0) {
            return
        }
        waveView.setSampleRate(125)
        val waveform = data["waveform"] as IntArray?
        if (waveform != null) {
            waveform!!.forEach {
                var pulsePointValue = it
                if (it == 0x9c || it == 0xf6) {
                    pulsePointValue = latestPulsePoint
                } else {
                    latestPulsePoint = pulsePointValue
                }
                val point = WaveViewRTS.PointModel()
                point.point = (100 - pulsePointValue) / 100f
                handler.sendMessage(Message.obtain(handler, 1, point))
            }
        }
    }

    fun updateAmplitude(amplitude: Int) {
        waveView.updateAmplitude(amplitude)
    }

    fun drawLine(isDraw: Boolean) {
        waveView.drawLine(isDraw)
    }

    fun clear() {
        datas.clear()
        waveView.clear()
        handler.removeMessages(0)
        handler.removeMessages(1)
    }

    fun stop() {
        RUN = false
        thread.interrupt()
        handlerThread.quit()
    }
}