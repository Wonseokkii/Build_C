package com.vivalnk.sdk.demo.vital.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.mmkv.MMKV;
import com.vivalnk.sdk.BuildConfig;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.demo.base.app.BaseToolbarActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.MyDataReceiveListener;
import com.vivalnk.sdk.demo.vital.MyDeviceStatusListener;
import com.vivalnk.sdk.demo.vital.R;

public class ConfigActivity extends BaseToolbarActivity {

    private boolean isFirst=true;

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_config);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        if (BuildConfig.sdkChannel.equals("sdk01")) {
            DeviceManager.getInstance().setDataReceiveListener(new MyDataReceiveListener());
        } else {
            DeviceManager.getInstance().setDeviceStatusListener(new MyDeviceStatusListener());
        }
    }

    private void initView() {
        actionBar.setTitle(getString(R.string.config_title));
        toolbar.setNavigationOnClickListener((v) -> finish());
    }

    private void doShowToast(String msg){
        if(isFirst){
            return;
        }
        showToast(msg);
    }

    private void setListener(){
        CheckBox radio_auto_connect_last=findViewById(R.id.radio_auto_connect_last);
        radio_auto_connect_last.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DeviceManager.getInstance().allowConnectLastConnectedDevice=isChecked;
            doShowToast("isChecked="+isChecked);
        });
        radio_auto_connect_last.setChecked(true);

        CheckBox radio_auto_connect=findViewById(R.id.radio_auto_connect);
        radio_auto_connect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DeviceManager.getInstance().autoConnectIfDisconnectDevice=isChecked;
            doShowToast("isChecked="+isChecked);
        });
        radio_auto_connect.setChecked(true);


        CheckBox radio_log_output=findViewById(R.id.radio_log_output);
        radio_log_output.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DeviceManager.getInstance().allowWriteToFile(isChecked);
            doShowToast("isChecked="+isChecked);
        });
        radio_log_output.setChecked(true);

        CheckBox radio_ecg_sync_clock=findViewById(R.id.radio_ecg_sync_clock);
        radio_ecg_sync_clock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DeviceManager.getInstance().allowForceClockSyncOnceConnected=isChecked;
            doShowToast("isChecked="+isChecked);
        });
        radio_ecg_sync_clock.setChecked(true);


        CheckBox radio_blooddev_read_file=findViewById(R.id.radio_blooddev_read_file);
        radio_blooddev_read_file.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DeviceManager.getInstance().allowCheckme_02ReadFile=isChecked;
            doShowToast("isChecked="+isChecked);
        });
        radio_blooddev_read_file.setChecked(true);

        TextView tv_data_upload = findViewById(R.id.tv_data_upload);
        CheckBox radio_data_upload=findViewById(R.id.radio_data_upload);
        if (BuildConfig.sdkChannel.equals("sdk01")) {
            radio_data_upload.setOnCheckedChangeListener((buttonView, isChecked) -> {
                DeviceManager.getInstance().allowUploadDataToCloud=isChecked;
                doShowToast("isChecked="+isChecked);
                if (isChecked) {
                    showRegisterSubjectDialog();
                }
            });
            radio_data_upload.setChecked(false);
        } else {
            DeviceManager.getInstance().allowUploadDataToCloud = true;
            radio_data_upload.setEnabled(false);
            radio_data_upload.setChecked(true);
            tv_data_upload.setText(getResources().getString(R.string.config_data_upload_msg));
            showRegisterSubjectDialog();
        }

        CheckBox radio_heart_rate=findViewById(R.id.radio_heart_rate);
        radio_heart_rate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DeviceManager.getInstance().allowAnimalHrAlgo=isChecked;
            doShowToast("isChecked="+isChecked);
        });
        radio_heart_rate.setChecked(false);

        EditText et_blood_rtsupload_rate=findViewById(R.id.et_blood_rtsupload_rate);
        et_blood_rtsupload_rate.setText("4");

        findViewById(R.id.bt_continue).setOnClickListener(v -> {
            float interval=4;
            try {
                interval=Float.valueOf(et_blood_rtsupload_rate.getText().toString().trim());
            }catch (Exception e){
                e.printStackTrace();
            }

            DeviceManager.getInstance().checkme_o2RealtimeDataInterval=interval;

            DeviceManager.getInstance().init(this.getApplication());

            startActivity(ScanningActivity.newIntent(ConfigActivity.this));
            finish();
        });

        isFirst=false;

//        Button button=findViewById(R.id.bt_tannotation);

//        button.setOnClickListener(v->{
//            if("旧数据库".equals(button.getText())){
//                button.setText("新数据库");
//                ConstantTest.isUseNewDao=true;
//            }else{
//                button.setText("旧数据库");
//                ConstantTest.isUseNewDao=false;
//            }
//        });
    }

    /**
     * 修改projectId和subjectId dialog
     */
    private void showRegisterSubjectDialog() {
        if (MMKV.defaultMMKV().getString(VitalClient.Builder.Key.projectId, "").isEmpty()) {
            MMKV.defaultMMKV().putString(VitalClient.Builder.Key.projectId, "VivaLNK_SDKTest");
        }
        if (MMKV.defaultMMKV().getString(VitalClient.Builder.Key.subjectId, "").isEmpty()) {
            MMKV.defaultMMKV().putString(VitalClient.Builder.Key.subjectId, "testAndroid");
        }
        View view = LayoutInflater.from(this).inflate(R.layout.subject_id_layout, null);
        final EditText edtProjectId = (EditText) view.findViewById(R.id.edtProjectId);
        final EditText edtSubjectId = (EditText) view.findViewById(R.id.edtSubjectId);
        new AlertDialog.Builder(this).setTitle("Register Subject")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String projectId = edtProjectId.getText().toString();
                        String subjectId = edtSubjectId.getText().toString();

                        if (TextUtils.isEmpty(projectId)) {
                            projectId = DeviceManager.projectId;
                        }

                        if (TextUtils.isEmpty(subjectId)) {
                            subjectId = DeviceManager.subjectId;
                        }
                        MMKV.defaultMMKV().putString(VitalClient.Builder.Key.projectId, projectId);
                        MMKV.defaultMMKV().putString(VitalClient.Builder.Key.subjectId, subjectId);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }


}