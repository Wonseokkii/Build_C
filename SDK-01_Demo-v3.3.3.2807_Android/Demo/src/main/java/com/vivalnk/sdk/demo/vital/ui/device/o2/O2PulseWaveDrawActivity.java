package com.vivalnk.sdk.demo.vital.ui.device.o2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.vital.ui.device.o2.components.Drawer_WaveForm_SpO2;
import com.vivalnk.sdk.demo.vital.ui.device.o2.components.WaveViewRTS;

import java.util.Map;

import butterknife.BindView;

/**
 * @author: Billows
 * @date: 2023/4/10 11:37
 */
public class O2PulseWaveDrawActivity extends ConnectedActivity {

    @BindView(R.id.pulseView)
    WaveViewRTS mPulseView;
    @BindView(R.id.tvSpo2Value)
    TextView mSpo2Value;
    @BindView(R.id.tvSpo2HeartValue)
    TextView mSpo2HeartValue;
    private Drawer_WaveForm_SpO2 mDrawer;

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_o2_pulse_wave_draw);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDrawer = new Drawer_WaveForm_SpO2(mPulseView);
        mDrawer.drawLine(false);
    }

    @Override
    protected void onDestroy() {
        mDrawer.stop();
        super.onDestroy();
    }

    private void drawWaveView(Map<String, Object> data) {
        final int pr = (int) data.get("pr");
        if (pr <= 0) {
            clearWaveView();
        } else {
            mDrawer.addWaveForm(data);
        }
    }

    private void clearWaveView() {
        mDrawer.clear();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSampleData(DeviceManager.VitalSampleData sampleData) {
        if (!sampleData.device.equals(mDevice)) {
            return;
        }
        Map<String, Object> data = sampleData.data;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int spo2 = (int) data.get("spo2");
                final int pr = (int) data.get("pr");
                if (spo2 <= 1) {
                    mSpo2Value.setText("--");
                    mSpo2HeartValue.setText("--");
                } else {
                    mSpo2Value.setText(spo2 + "");
                    mSpo2HeartValue.setText(pr + "");
                }
                drawWaveView(data);
            }
        });
    }

}
