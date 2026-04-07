package com.vivalnk.sdk.demo.vital.ui.device.bodyscale;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.base.ihealth.VViHealthCmdInstance;
import com.vivalnk.sdk.base.ihealth.hs2s.VVScaleMeasure;
import com.vivalnk.sdk.base.ihealth.hs2s.WeightScaleUnit;
import com.vivalnk.sdk.base.ihealth.hs2s.HealthUser;
import com.vivalnk.sdk.base.ihealth.iHealthCommandType;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.FileUtils;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.engineer.test.FileManager;
import com.vivalnk.sdk.utils.DateFormat;
import com.vivalnk.sdk.utils.GSON;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Billows
 * @date: 2023/6/25 9:11
 */
public class DeviceMenuHS2SActivity extends ConnectedActivity {

    @BindView(R.id.tvPrinter)
    TextView tvPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VVScaleMeasure.getInstance().setNeedNotifyToUpload(mDevice, false);
    }

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_device_menu_hs2s);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    @OnClick(R.id.btnClearLog)
    void clickBtnClearLog() {
        tvPrinter.setText("");
    }

    @OnClick(R.id.btnGetDeviceInfo)
    void clickBtnGetDeviceInfo() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.read_scale_deviceInfo);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }

    @OnClick(R.id.btnReadBattery)
    void clickBtnReadBattery() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.read_scale_battery);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }

    @OnClick(R.id.btnSetUnitKG)
    void clickBtnSetUnitKG() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.set_scale_weightUnit);
        cmdInstance.setScaleUnit(WeightScaleUnit.WSUnit_Kg);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }

    @OnClick(R.id.btnSetUnitLB)
    void clickBtnSetUnitLB() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.set_scale_weightUnit);
        cmdInstance.setScaleUnit(WeightScaleUnit.WSUnit_LB);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }

    @OnClick(R.id.btnSetUnitST)
    void clickBtnSetUnitST() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.set_scale_weightUnit);
        cmdInstance.setScaleUnit(WeightScaleUnit.WSUnit_ST);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }

    @OnClick(R.id.btnMeasureScaleWeight)
    void clickBtnMeasureScaleWeight() {
        selectUser(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                if (null == data) {
                    showToast("Device has no users");
                } else {
                    VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.measure_scale_weight);
                    cmdInstance.user = (HealthUser) data.get("data");
                    VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
                }
            }
        });
    }

    @OnClick(R.id.btnDeleteUser)
    void clickBtnDeleteUser() {
        selectUser(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                if (null == data) {
                    showToast("Device has no users");
                } else {
                    VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.delete_scale_user);
                    cmdInstance.setUser((HealthUser) data.get("data"));
                    VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
                }
            }
        });
    }

    @OnClick(R.id.btnUpdateUser)
    void clickBtnUpdateUser() {
        selectUser(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                if (null == data) {
                    showToast("Device has no users");
                } else {
                    showUserDialog((HealthUser) data.get("data"));
                }
            }
        });
    }

    private void showUserDialog(HealthUser userBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceMenuHS2SActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ihealth_hs2s_create_user, null);
        EditText userIdEditText = view.findViewById(R.id.etUserId);
        EditText etWeightEditText = view.findViewById(R.id.etWeight);
        EditText etGenderEditText = view.findViewById(R.id.etGender);
        EditText etImpedanceEditText = view.findViewById(R.id.etImpedance);
        EditText etHeightEditText = view.findViewById(R.id.etHeight);
        EditText etBodybuildingEditText = view.findViewById(R.id.etBodybuilding);
        EditText etAgeEditText = view.findViewById(R.id.etAge);
        if (userBean != null) {
            userIdEditText.setText(userBean.userId);
            etWeightEditText.setText(userBean.weight + "");
            etGenderEditText.setText(userBean.sex + "");
            etImpedanceEditText.setText(userBean.impedanceMark + "");
            etHeightEditText.setText(userBean.height + "");
            etBodybuildingEditText.setText(userBean.fitness + "");
            etAgeEditText.setText(userBean.age + "");
        }
        builder.setView(view)
                .setTitle(userBean == null ? "Create User" : "Update User")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String userId = userIdEditText.getText().toString();
                        if (TextUtils.isEmpty(userId)) {
                            showToast("user id is empty");
                            return;
                        }
                        float weight = -1;
                        try {
                            weight = Float.parseFloat(etWeightEditText.getText().toString());
                        } catch (Exception e) {

                        }
                        if (weight == -1) {
                            showToast("weight is empty or wrong");
                            return;
                        }
                        int gender = -1;
                        try {
                            gender = Integer.parseInt(etGenderEditText.getText().toString());
                        } catch (Exception e) {

                        }
                        if (gender == -1) {
                            showToast("gender is empty or wrong");
                            return;
                        }
                        int impedance = -1;
                        try {
                            impedance = Integer.parseInt(etImpedanceEditText.getText().toString());
                        } catch (Exception e) {

                        }
                        if (impedance == -1) {
                            showToast("impedance is empty or wrong");
                            return;
                        }
                        int height = -1;
                        try {
                            height = Integer.parseInt(etHeightEditText.getText().toString());
                        } catch (Exception e) {

                        }
                        if (height == -1) {
                            showToast("height is empty or wrong");
                            return;
                        }
                        int bodybuilding = -1;
                        try {
                            bodybuilding = Integer.parseInt(etBodybuildingEditText.getText().toString());
                        } catch (Exception e) {

                        }
                        if (bodybuilding == -1) {
                            showToast("fitness is empty or wrong");
                            return;
                        }
                        int age = -1;
                        try {
                            age = Integer.parseInt(etAgeEditText.getText().toString());
                        } catch (Exception e) {

                        }
                        if (age == -1) {
                            showToast("age is empty or wrong");
                            return;
                        }
                        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.update_scale_userInfo);
                        HealthUser userBean = new HealthUser(userId);
                        userBean.age = age;
                        userBean.impedanceMark = impedance;
                        userBean.sex = gender;
                        userBean.createTS = System.currentTimeMillis();
                        userBean.fitness = bodybuilding;
                        userBean.height = height;
                        userBean.weight = weight;
                        cmdInstance.setUser(userBean);
                        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @OnClick(R.id.btnCreateUser)
    void clickBtnCreateUser() {
        showUserDialog(null);
    }

    @OnClick(R.id.btnGetUserInfo)
    void clickBtnGetUserInfo() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.read_scale_userInfo);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }

    @OnClick(R.id.btnReadHistoryCount)
    void clickBtnReadHistoryCount() {
        selectUser(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                if (null == data) {
                    showToast("Device has no users");
                } else {
                    VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.read_scale_historyDataCount);
                    cmdInstance.user = (HealthUser) data.get("data");
                    VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
                }
            }
        });
    }

    @OnClick(R.id.btnReadHistoryData)
    void clickBtnReadHistoryData() {
        selectUser(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                if (null == data) {
                    showToast("Device has no users");
                } else {
                    VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.read_scale_historyData);
                    cmdInstance.setUser((HealthUser) data.get("data"));
                    VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
                }
            }
        });
    }

    @OnClick(R.id.btnDeleteHistoryData)
    void clickBtnDeleteHistoryData() {
        selectUser(new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                if (null == data) {
                    showToast("Device has no users");
                } else {
                    VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.delete_scale_historyData);
                    cmdInstance.user = (HealthUser) data.get("data");
                    VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
                }
            }
        });
    }

   /* @OnClick(R.id.btnReadAnonymousHistoryDataCount)
    void clickBtnReadAnonymousHistoryDataCount() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.read_scale_anonymousHistoryDataCount);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }

    @OnClick(R.id.btnReadAnonymousHistoryData)
    void clickBtnReadAnonymousHistoryData() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.read_scale_anonymousHistoryData);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }

    @OnClick(R.id.btnDeleteAnonymousHistoryData)
    void clickBtnDeleteAnonymousHistoryData() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.delete_scale_anonymousHistoryData);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }*/

    @OnClick(R.id.btnResetDevice)
    void clickBtnResetDevice() {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.reset_scale_device);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, defaultCallback);
    }

    @OnClick(R.id.btnDisconnect)
    void clickBtnDisconnect() {
        showProgressDialog("Disconnecting...");
        DeviceManager.getInstance().disconnect(mDevice);
    }

    private void selectUser(Callback callback) {
        VViHealthCmdInstance cmdInstance = new VViHealthCmdInstance(mDevice, iHealthCommandType.read_scale_userInfo);
        VVScaleMeasure.getInstance().sendiHealthScaleCommand(cmdInstance, new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {
                List<HealthUser> list = (List<HealthUser>) data.get("userInfo");
                if (list == null || list.size() == 0) {
                    callback.onComplete(null);
                    return;
                }

                Map<String, Object> map = new HashMap<>();
                if (list.size() > 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceMenuHS2SActivity.this);
                    String[] users = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        users[i] = list.get(i).userId;
                    }
                    builder.setTitle("Select User")
                            .setItems(users, (dialog, which) -> {
                                map.put("data", list.get(which));
                                callback.onComplete(map);
                            });
                    builder.create().show();
                } else {
                    map.put("data", list.get(0));
                    callback.onComplete(map);
                }
            }
        });
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
                tvPrinter.append("\n\n");

                final String date = DateFormatUtils.format(new Date(), DateFormat.sPattern);
                String log = date
                        + (sampleData.data == null ? "" : (", " + GSON.toJson(sampleData.data)))
                        + "\n";
                try {
                    //保存原始数据
                    String filePath = FileManager.getFileDataPath(mDevice.getName(), "data.txt");
                    FileUtils.writeFile(filePath, log);
                } catch (Throwable throwable) {

                }

            }
        });
    }
}
