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
import com.vivalnk.sdk.device.aoj.AOJ_BPManager;
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
 * @date: 2023/3/7 19:38
 */
public class DeviceMenuAOJ_BPActivity extends ConnectedActivity {
    private AOJ_BPManager manager;
    @BindView(R.id.tvPrinter)
    TextView tvPrinter;

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_device_menu_aoj_bp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new AOJ_BPManager(mDevice);
    }

    @OnClick(R.id.btnStartMeasure)
    public void btnStartMeasure() {
        manager.startBPMeasuring(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                tvPrinter.append("callback: " + GSON.toJson(data));
                tvPrinter.append("\r\n");
            }
        });
    }

    @OnClick(R.id.btnStopMeasure)
    public void btnStopMeasure() {
        manager.stopBPMeasuring(defaultCallback);
    }

    @OnClick(R.id.btnGetSN)
    public void btnGetSN() {
    }

    @OnClick(R.id.btnQueryBPDeviceStatus)
    public void btnQueryBPDeviceStatus() {
        manager.queryBPDeviceStatus(defaultCallback);
    }

    @OnClick(R.id.btnTimeSync)
    public void btnTimeSync() {
        manager.syncBPDeviceTime(defaultCallback);
    }

    @OnClick(R.id.btnDeleteBPHistoryData)
    public void btnDeleteBPHistoryData() {
        manager.deleteBPHistoryData(defaultCallback);
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        String[] users = {"All", "User1", "User2"};
//        builder.setTitle("Remove User Data")
//                .setItems(users, (dialog, which) -> {
//                    switch (which) {
//                        case 0: //all
//                            manager.deleteBPHistoryData(AOJ_BPManager.User.All, defaultCallback);
//                            break;
//                        case 1: //user1
//                            manager.deleteBPHistoryData(AOJ_BPManager.User.User1, defaultCallback);
//                            break;
//                        case 2: //user2
//                            manager.deleteBPHistoryData(AOJ_BPManager.User.User2, defaultCallback);
//                            break;
//                    }
//                });
//        builder.create().show();
    }

    @OnClick(R.id.btnSyncBPHistoryData)
    public void btnSyncBPHistoryData() {
        manager.syncBPHistoryData(defaultCallback);
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        String[] users = {"All", "User1", "User2"};
//        builder.setTitle("Sync Data")
//                .setItems(users, (dialog, which) -> {
//                    switch (which) {
//                        case 0: //all
//                            manager.syncBPHistoryData(AOJ_BPManager.User.All, defaultCallback);
//                            break;
//                        case 1: //user1
//                            manager.syncBPHistoryData(AOJ_BPManager.User.User1, defaultCallback);
//                            break;
//                        case 2: //user2
//                            manager.syncBPHistoryData(AOJ_BPManager.User.User2, defaultCallback);
//                            break;
//                    }
//                });
//        builder.create().show();
    }

    @OnClick(R.id.btnDisconnect)
    public void btnDisconnect() {
        manager.disconnect();
    }

    @OnClick(R.id.btnSwitchUser)
    public void btnSwitchUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = {"User1", "User2"};
        builder.setTitle("Switch User")
                .setItems(users, (dialog, which) -> {
                    switch (which) {
                        case 0: //user1
                            manager.switchBPDeviceUser(1, defaultCallback);
                            break;
                        case 1: //user2
                            manager.switchBPDeviceUser(2, defaultCallback);
                            break;
                    }
                });
        builder.create().show();
    }

    @OnClick(R.id.btnVoiceControl)
    public void btnVoiceControl() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = {"Enable", "Disable"};
        builder.setTitle("Voice Control")
                .setItems(users, (dialog, which) -> {
                    switch (which) {
//                        case 0: //Enable
//                            manager.voiceControl(true, defaultCallback);
//                            break;
//                        case 1: //Disable
//                            manager.voiceControl(false, defaultCallback);
//                            break;
                    }
                });
        builder.create().show();
    }

    @OnClick(R.id.btnClearLog)
    public void btnClearLog() {
        tvPrinter.setText("");
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

}
