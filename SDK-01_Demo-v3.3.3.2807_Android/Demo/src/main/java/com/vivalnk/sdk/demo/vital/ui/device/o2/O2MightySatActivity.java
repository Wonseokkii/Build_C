package com.vivalnk.sdk.demo.vital.ui.device.o2;

import android.os.Bundle;
import android.widget.TextView;

import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.device.mightysat.MightySatManager;
import com.vivalnk.sdk.utils.GSON;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Billows
 * @date: 2023/5/19 17:23
 */
public class O2MightySatActivity  extends ConnectedActivity {

    @BindView(R.id.tvPrinter)
    TextView tvPrinter;

    private MightySatManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new MightySatManager(mDevice);
    }

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_o2_mightysat);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
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
