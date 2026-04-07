package com.vivalnk.sdk.demo.vital.ui.device.aoj;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.FileUtils;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.device.aoj.AOJ_O2Manager;
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
 * @date: 2023/3/7 15:22
 */
public class DeviceMenuAOJ_O2Activity extends ConnectedActivity {

    @BindView(R.id.tvPrinter)
    TextView tvPrinter;
    @BindView(R.id.edtMinSpo2)
    TextView edtMinSpo2;
    @BindView(R.id.edtMaxSpo2)
    TextView edtMaxSpo2;
    @BindView(R.id.edtMinPulseRate)
    TextView edtMinPulseRate;
    @BindView(R.id.edtMaxPulseRate)
    TextView edtMaxPulseRate;

    private AOJ_O2Manager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new AOJ_O2Manager(mDevice);
        getSpO2Alarm();
    }

    public void getSpO2Alarm() {
        manager.readSpO2AlarmValue(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                int minSpo2 = (int) data.get("minSpo2");
                edtMinSpo2.setText(minSpo2 + "");
                int minPulseRate = (int) data.get("minPulseRate");
                edtMinPulseRate.setText(minPulseRate + "");
                int maxPulseRate = (int) data.get("maxPulseRate");
                edtMaxPulseRate.setText(maxPulseRate + "");
            }
        });
    }

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_device_menu_aoj_o2);
    }

    @OnClick(R.id.btnSetSpO2Alarm)
    public void btnSetSpO2Alarm() {
        int minSpo2 = 95;
        int maxSpo2 = 100;
        int minPR = 60;
        int maxPR = 100;
        try {
            minSpo2 = Integer.parseInt(edtMinSpo2.getText().toString());
        } catch (Exception e) {

        }
        try {
            maxSpo2 = Integer.parseInt(edtMaxSpo2.getText().toString());
        } catch (Exception e) {

        }
        try {
            minPR = Integer.parseInt(edtMinPulseRate.getText().toString());
        } catch (Exception e) {

        }
        try {
            maxPR = Integer.parseInt(edtMaxPulseRate.getText().toString());
        } catch (Exception e) {

        }
        manager.setSpO2AlarmValue(minSpo2, minPR, maxPR, defaultCallback);
    }

    @OnClick(R.id.btnGetSpO2Alarm)
    public void btnGetSpO2Alarm() {
        manager.readSpO2AlarmValue(defaultCallback);
    }


    @OnClick(R.id.btnDisconnect)
    public void btnDisconnect() {
        manager.disconnect();
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
                tvPrinter.setText(GSON.toJson(sampleData.data));
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
