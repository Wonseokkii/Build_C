package com.vivalnk.sdk.demo.vital;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.vivalnk.sdk.DeviceStatusListener;
import com.vivalnk.sdk.common.eventbus.EventBus;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.model.BatteryInfo;
import com.vivalnk.sdk.model.Device;

import java.util.Map;

public class MyDeviceStatusListener implements DeviceStatusListener {

    public static final String TAG = "DeviceManager";
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onBatteryChange(Device device, Map<String, Object> data) {
        //for VV330
        if (data.get("data") != null && data.get("data") instanceof BatteryInfo) {
            BatteryInfo batteryInfo = (BatteryInfo) data.get("data");

            DeviceManager.BatteryData batteryData = new DeviceManager.BatteryData();
            batteryData.device = device;
            batteryData.batteryInfo = batteryInfo;

            runOnUiThread(() -> EventBus.getDefault().post(batteryData));
        }
    }

    @Override
    public void onDeviceInfoUpdate(Device device, Map<String, Object> data) {
        //for SpO2, data has field like bellow
        /**
         * {
         *     "Region": "CE",
         *     "Model": "1641",
         *     "HardwareVer": "AA",
         *     "SoftwareVer": "4.7.0",
         *     "BootloaderVer": "0.1.0.0",
         *     "FileVer": "3",
         *     "SPCPVer": "1.4",
         *     "SN": "1912217640",
         *     "CurTIME": "2020-07-27,11:08:24",
         *     "CurBAT": "30%",
         *     "CurBatState": "0",
         *     "CurOxiThr": "90",
         *     "CurMotor": "50",
         *     "CurPedtar": "99999",
         *     "CurMode": "1",
         *     "BranchCode": "21010000",
         *     "FileList": "20200723114655,20200723170412,20200724083753,"
         * }
         */

        //TODO for VV330

    }

    @Override
    public void onLeadStatusChange(Device device, boolean isLeadOn) {
        DeviceManager.LeadStatusData leadStatusData = new DeviceManager.LeadStatusData();
        leadStatusData.device = device;
        leadStatusData.leadOn = isLeadOn;

        runOnUiThread(() -> EventBus.getDefault().post(leadStatusData));
    }

    @Override
    public void onFlashStatusChange(Device device, int remainderFlashBlock) {
        Log.d(TAG, "remainder Flash Block num = " + remainderFlashBlock);
    }

    @Override
    public void onFlashUploadFinish(Device device) {
        Log.d(TAG, device.getName() + " flash upload finish");
    }

    public void runOnUiThread(Runnable runnable) {
        mMainHandler.post(runnable);
    }

}
