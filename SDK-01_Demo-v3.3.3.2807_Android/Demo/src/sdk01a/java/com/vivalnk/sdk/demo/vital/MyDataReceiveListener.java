package com.vivalnk.sdk.demo.vital;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.vivalnk.sdk.DataReceiveListener;
import com.vivalnk.sdk.common.eventbus.EventBus;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.demo.repository.database.DatabaseManager;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.device.vv330.DataStreamMode;
import com.vivalnk.sdk.model.BatteryInfo;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.model.SampleData;
import com.vivalnk.sdk.model.common.DataType;

import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.SingleScheduler;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class MyDataReceiveListener implements DataReceiveListener {

    public static final String TAG = "DeviceManager";
    private Subject<Runnable> subject = PublishSubject.<Runnable>create().toSerialized();
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public MyDataReceiveListener() {
        subject
                //event source will be execute on this thread
                .subscribeOn(new SingleScheduler())
                //observer's callback onNext/onError/onComplete will be execute in this thread
                .observeOn(new SingleScheduler())
                .subscribe(new Observer<Runnable>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Runnable runnable) {
                        runnable.run();
                    }

                    @Override
                    public void onError(Throwable e) {
                        VitalLog.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

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

    private void handleSampleData(final Device device, final Map<String, Object> data) {
        SampleData sampleData = (SampleData) data.get("data");

        if (sampleData == null) {
            return;
        }

        //save to database
        DataStreamMode mode = sampleData.getData(DataType.DataKey.dataStreamMode);
        if (mode != null && mode == DataStreamMode.FullDualMode) {
            if (sampleData.isFlash()) {
                //under FullDualMode, we just store the flash data, and only show the RTS data waveform.
                DatabaseManager.getInstance().insert(new SampleData(sampleData));
            }
        } else {
            DatabaseManager.getInstance().insert(new SampleData(sampleData));
        }

        runOnUiThread(() -> EventBus.getDefault().post(sampleData));

    }

}
