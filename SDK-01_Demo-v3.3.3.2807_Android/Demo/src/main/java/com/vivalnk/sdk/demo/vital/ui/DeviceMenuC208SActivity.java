package com.vivalnk.sdk.demo.vital.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.device.c208s.C208SManager;
import com.vivalnk.sdk.utils.GSON;

import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Billows
 * @date: 2022/5/26 19:33
 */
public class DeviceMenuC208SActivity extends ConnectedActivity {

    @BindView(R.id.tvPrinter)
    TextView tvPrinter;

    private C208SManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new C208SManager(mDevice);
    }

    @OnClick(R.id.btnGetBatteryData)
    public void getBatteryData() {
        manager.readBatteryLevel(new Callback() {
            @Override
            public void onStart() {
                showProgressDialog("staring...");
            }

            @Override
            public void onComplete(Map<String, Object> data) {
                dismissProgressDialog();
                showAlertDialog("Result of Read Battery Level", GSON.toJson(data));
                VitalLog.d(TAG, GSON.toJson(data));
            }

            @Override
            public void onError(int code, String msg) {
                dismissProgressDialog();
                showAlertDialog("Result of Read Battery Level", "error code = " + code + ", msg = " + msg);
            }
        });
    }

    @OnClick(R.id.btnGetDeviceInfo)
    public void btnGetDeviceInfo() {
        manager.readDeviceInfo(new Callback() {
            @Override
            public void onStart() {
                showProgressDialog("staring...");
            }

            @Override
            public void onComplete(Map<String, Object> data) {
                dismissProgressDialog();
                showAlertDialog("Result of Device Information", GSON.toJson(data));
                VitalLog.d(TAG, GSON.toJson(data));
            }

            @Override
            public void onError(int code, String msg) {
                dismissProgressDialog();
                showAlertDialog("Result of Device Information", "error code = " + code + ", msg = " + msg);
            }
        });
    }

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_device_c208s);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @OnClick(R.id.btnEnableNotification)
    void enableNotification() {
        manager.enableNotification(new Callback() {
            @Override
            public void onStart() {
                showProgressDialog("enable notification ...");
            }

            @Override
            public void onComplete(Map<String, Object> data) {
                showToast("enable success");
                dismissProgressDialog();
            }

            @Override
            public void onError(int code, String msg) {
                showToast(msg);
                dismissProgressDialog();
            }
        });
    }

    @OnClick(R.id.btnDisableNotification)
    void disableNotification() {
        manager.disableNotification(new Callback() {
            @Override
            public void onStart() {
                showProgressDialog("disable notification ...");
            }

            @Override
            public void onComplete(Map<String, Object> data) {
                showToast("disable success");
                dismissProgressDialog();
            }

            @Override
            public void onError(int code, String msg) {
                showToast(msg);
                dismissProgressDialog();
            }
        });
    }

    @OnClick(R.id.btnDisconnect)
    void clickBtnDisconnect() {
        showProgressDialog("Disconnecting...");
        DeviceManager.getInstance().disconnect(mDevice);
    }

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
    }
}
