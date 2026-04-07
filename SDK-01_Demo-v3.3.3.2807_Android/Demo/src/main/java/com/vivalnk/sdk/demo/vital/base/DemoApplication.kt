package com.vivalnk.sdk.demo.vital.base

import android.content.Context
import android.os.Process
import leakcanary.LeakCanary.showLeakDisplayActivityLauncherIcon
import com.vivalnk.sdk.demo.base.app.BaseApplication
import androidx.multidex.MultiDex
import com.vivalnk.sdk.demo.base.utils.SPUtils
import leakcanary.LeakCanary
import com.vivalnk.sdk.demo.repository.device.DeviceManager
import com.vivalnk.sdk.common.utils.log.VitalLog
import android.text.TextUtils
import com.tencent.mmkv.MMKV
import com.vivalnk.sdk.demo.vital.BuildConfig
import com.vivalnk.sdk.utils.ProcessUtils
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

/**
 * Created by JakeMo on 18-1-30.
 */
class DemoApplication : BaseApplication() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        SPUtils.getInstance()
        val openLeakCanary = BuildConfig.openLeakCanary && isLeanCanaryOpen
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = openLeakCanary)
        showLeakDisplayActivityLauncherIcon(openLeakCanary)
        MMKV.initialize(this)
//        if (ProcessUtils.isMainProcess(applicationContext)) {
//            DeviceManager.getInstance().init(this)
//        }

        initBugly(this.applicationContext)
    }

    companion object {
        private const val APPID = "9b29392c19"

        //init engineering module
        private val isLeanCanaryOpen: Boolean
            private get() {
                //init engineering module
                try {
                    val engineeringClient =
                        Class.forName("com.vivalnk.sdk.engineer.EngineeringClient")
                    val getInstance = engineeringClient.getDeclaredMethod("getInstance")
                    val client = getInstance.invoke(null)
                    val isLeakCanaryOpen = engineeringClient.getDeclaredMethod("isLeakCanaryOpen")
                    return isLeakCanaryOpen.invoke(client) as Boolean
                } catch (e: Throwable) {
                    if (com.vivalnk.sdk.BuildConfig.DEBUG) {
                        VitalLog.i(e)
                    }
                }
                return false
            }

        fun initBugly(context: Context) {
//            val strategy = UserStrategy(context)
//            strategy.appVersion = BuildConfig.VERSION_NAME
//            strategy.appPackageName = context.packageName
//            // 获取当前包名
//            val packageName = context.packageName
//            // 获取当前进程名
//            val processName = getProcessName(Process.myPid())
//            strategy.isUploadProcess = processName == null || processName == packageName
//            strategy.setCrashHandleCallback(object : CrashHandleCallback() {
//                @Synchronized
//                override fun onCrashHandleStart(
//                    i: Int, s: String, s1: String,
//                    s2: String
//                ): Map<String, String> {
//                    return VivalnkCrashHandler.getInstance().collectDeviceInfosMap()
//                }
//            })

//            //设置测试设备
//            CrashReport.setIsDevelopmentDevice(context, true)
//            CrashReport.putUserData(context, "appPackageName", context.packageName)
//            CrashReport.putUserData(context, "appVersionName", BuildConfig.VERSION_NAME)
//            CrashReport.putUserData(context, "appVersionCode", "" + BuildConfig.VERSION_CODE)
//            CrashReport.putUserData(
//                context,
//                "sdkVersionName",
//                com.vivalnk.sdk.BuildConfig.VERSION_NAME
//            )
//            CrashReport.putUserData(
//                context,
//                "sdkVersionCode",
//                "" + com.vivalnk.sdk.BuildConfig.VERSION_CODE
//            )
//            CrashReport.initCrashReport(context, APPID, true, strategy)
        }

        private fun getProcessName(pid: Int): String? {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
                var processName = reader.readLine()
                if (!TextUtils.isEmpty(processName)) {
                    processName = processName.trim { it <= ' ' }
                }
                return processName
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (exception: IOException) {
                    exception.printStackTrace()
                }
            }
            return null
        }
    }
}