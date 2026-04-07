package com.vivalnk.sdk.demo.vital.ui.device.aoj;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.FileUtils;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.device.aoj.AOJ_TempManager;
import com.vivalnk.sdk.engineer.test.FileManager;
import com.vivalnk.sdk.utils.DateFormat;
import com.vivalnk.sdk.utils.GSON;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Billows
 * @date: 2023/3/9 14:51
 */
public class DeviceMenuAOJ_TempActivity extends ConnectedActivity {

    private AOJ_TempManager manager;
    @BindView(R.id.tvPrinter)
    TextView tvPrinter;

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_device_menu_aoj_temp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new AOJ_TempManager(mDevice);
    }

    @OnClick(R.id.btnStartMeasure)
    public void btnStartMeasure() {
        manager.startTempMeasuring(defaultCallback);
    }

    @OnClick(R.id.btnSetTempMeasureMode)
    public void btnSetTempMeasureMode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Switch Temperature Mode")
                .setItems(R.array.aoj_temperature_mode, (dialog, which) -> {
                    switch (which) {
                        case 0: //Adult
                            manager.setTempMeasureMode(AOJ_TempManager.Mode.Adult, defaultCallback);
                            break;
                        case 1: //Children
                            manager.setTempMeasureMode(AOJ_TempManager.Mode.Children, defaultCallback);
                            break;
                        case 2: //Ear
                            manager.setTempMeasureMode(AOJ_TempManager.Mode.Ear, defaultCallback);
                            break;
                        case 3: //Material
                            manager.setTempMeasureMode(AOJ_TempManager.Mode.Material, defaultCallback);
                            break;
                    }
                });
        builder.create().show();
    }

    @OnClick(R.id.btnDisconnect)
    public void btnDisconnect() {
        manager.disconnect();
    }

    @OnClick(R.id.btnReadHistoryTempData)
    public void btnReadHistoryTempData() {
        manager.readHistoryTempData(defaultCallback);
    }

    @OnClick(R.id.btnClearData)
    public void btnClearData() {
        manager.deleteAllTempData(defaultCallback);
    }

    @OnClick(R.id.btnQueryTempDeviceInfo)
    public void btnQueryTempDeviceInfo() {
        manager.queryTempDeviceInfo(defaultCallback);
    }

    @OnClick(R.id.btnSyncTempDeviceTime)
    public void btnSyncTempDeviceTime() {
        manager.syncTempDeviceTime(defaultCallback);
    }

    @OnClick(R.id.btnClearLog)
    public void btnClearLog() {
        tvPrinter.setText("");
    }

    private Callback defaultCallback = new Callback() {
        @Override
        public void onStart() {
            showProgressDialog("staring...");
        }

        @Override
        public void onComplete(Map<String, Object> data) {
            dismissProgressDialog();
            showAlertDialog("Result", GSON.toJson(data));
        }

        @Override
        public void onError(int code, String msg) {
            dismissProgressDialog();
            showAlertDialog("Error", "error code = " + code + ", msg = " + msg);
        }
    };

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSampleData(DeviceManager.VitalSampleData sampleData) {
        if (!sampleData.device.equals(mDevice)) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvPrinter.append(GSON.toJson(sampleData.data));
                tvPrinter.append("\r\n");
                tvPrinter.append("\r\n");
            }
        });

        final String date = DateFormatUtils.format(new Date(), DateFormat.sPattern);
        String log = date
                + (sampleData.data == null ? "" : (", " + GSON.toJson(sampleData.data)))
                + "\n";
        try {
            //保存原始数据
            String filePath = FileManager.getFileDataPath(mDevice.getName(), "data.txt");
            Context context = VitalClient.getInstance().getAppContext();
            FileUtils.writeFile(filePath, log);
        } catch (Throwable throwable) {

        }

    }

}
