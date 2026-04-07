package com.vivalnk.sdk.demo.vital.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.vivalnk.sdk.demo.base.app.BaseToolbarActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;

import butterknife.OnClick;

/**
 * @author: Billows
 * @date: 2023/11/16 13:22
 */
public class SettingsActivity extends BaseToolbarActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_settings);
    }

    @OnClick(R.id.btnClearLog)
    void clickBtnDetail() {
        DeviceManager.getInstance().clearLog();
    }
}
