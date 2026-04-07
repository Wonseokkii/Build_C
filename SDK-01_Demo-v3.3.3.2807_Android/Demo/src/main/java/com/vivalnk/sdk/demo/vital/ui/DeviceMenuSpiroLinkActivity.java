package com.vivalnk.sdk.demo.vital.ui;

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
import com.vivalnk.sdk.device.aoj.AOJ_BPManager;
import com.vivalnk.sdk.device.spirolink.SpiroLinkManager;
import com.vivalnk.sdk.engineer.test.FileManager;
import com.vivalnk.sdk.utils.DateFormat;
import com.vivalnk.sdk.utils.GSON;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class DeviceMenuSpiroLinkActivity extends ConnectedActivity {
    private static final String TAG = "SpiroLinkActivity";

    private SpiroLinkManager manager;

    @BindView(R.id.tvPrinter)
    TextView tvPrinter;

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_device_spirolink);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new SpiroLinkManager(mDevice);
    }

    @OnClick(R.id.btnDisconnect)
    void clickBtnDisconnect() {
        showProgressDialog("Disconnecting...");
        DeviceManager.getInstance().disconnect(mDevice);
    }

    @OnClick(R.id.btnStartMeasure)
    void clickBtnStartMeasure() {
        manager.startSPMeasuring(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                Callback.super.onComplete(data);
                showToast("Start Measure OK!");
            }
        });
    }

    @OnClick(R.id.btnStopMeasure)
    void clickBtnStopMeasure() {
        manager.stopSPMeasuring(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                Callback.super.onComplete(data);
                showToast("Stop Measure OK!");
            }
        });
    }

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
                + (sampleData.data == null ? "" : (", " +  GSON.toJson(sampleData.data)))
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
