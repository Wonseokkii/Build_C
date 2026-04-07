package com.vivalnk.sdk.demo.vital.ui.device.bp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.github.gzuliyujiang.wheelpicker.DatimePicker;
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatimePickedListener;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity;
import com.github.gzuliyujiang.wheelpicker.entity.TimeEntity;
import com.github.gzuliyujiang.wheelpicker.widget.DatimeWheelLayout;
import com.tencent.mmkv.MMKV;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.base.bp.ABPMSettingsBean;
import com.vivalnk.sdk.base.bp.OnABPMSettingsListener;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.vital.widget.MyAppCompatCheckBox;
import com.vivalnk.sdk.device.blesig.ABPMBPManager;
import com.vivalnk.sdk.utils.DateFormat;
import com.vivalnk.sdk.utils.GSON;

import java.util.Calendar;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Billows
 * @date: 2023/7/18 15:54
 */
public class DeviceMenuABPMActivity extends ConnectedActivity {

    @BindView(R.id.tvPrinter)
    TextView tvPrinter;
    @BindView(R.id.cbAllowAccDataUpload)
    MyAppCompatCheckBox cbAllowAccDataUpload;
    @BindView(R.id.cbDisplayBP)
    MyAppCompatCheckBox cbDisplayBP;
    @BindView(R.id.llABPAutoSleep)
    LinearLayout llABPAutoSleep;
    @BindView(R.id.llABPAuto)
    LinearLayout llABPAuto;
    @BindView(R.id.cbSleep)
    MyAppCompatCheckBox cbSleep;
    @BindView(R.id.btnAutoStartTime)
    AppCompatButton btnAutoStartTime;
    @BindView(R.id.btnAutoStopTime)
    AppCompatButton btnAutoStopTime;
    @BindView(R.id.btnSubmit)
    AppCompatButton btnSubmit;
    @BindView(R.id.cbAutoStartTime)
    MyAppCompatCheckBox cbAutoStartTime;
    @BindView(R.id.llStartEndTime)
    LinearLayout llStartEndTime;

    @BindView(R.id.btnSectionTime1)
    AppCompatButton btnSectionTime1;
    @BindView(R.id.btnSectionTime2)
    AppCompatButton btnSectionTime2;
    @BindView(R.id.btnSectionTime3)
    AppCompatButton btnSectionTime3;
    @BindView(R.id.btnSectionTime4)
    AppCompatButton btnSectionTime4;
    @BindView(R.id.btnSectionTime5)
    AppCompatButton btnSectionTime5;
    @BindView(R.id.btnSectionTime6)
    AppCompatButton btnSectionTime6;
    @BindView(R.id.btnInterval1)
    AppCompatButton btnInterval1;
    @BindView(R.id.btnInterval2)
    AppCompatButton btnInterval2;
    @BindView(R.id.btnInterval3)
    AppCompatButton btnInterval3;
    @BindView(R.id.btnInterval4)
    AppCompatButton btnInterval4;
    @BindView(R.id.btnInterval5)
    AppCompatButton btnInterval5;
    @BindView(R.id.btnInterval6)
    AppCompatButton btnInterval6;
    @BindView(R.id.btnSleepModeInterval)
    AppCompatButton btnSleepModeInterval;

    private ABPMBPManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new ABPMBPManager(mDevice);
        initView();
    }

    private void initView() {
        showProgressDialog();
        cbAutoStartTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                llStartEndTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (!isChecked) {
                    manager.setStartEndTime(-1, -1, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            mStartTime = -1;
                            mEndTime = -1;
                            btnAutoStartTime.setText("Auto Start Time");
                            btnAutoStopTime.setText("Auto Stop Time");
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                            cbAutoStartTime.setChecked(!isChecked, false);
                        }
                    });

                }
            }
        });
        cbAllowAccDataUpload.setChecked(manager.isAllowAccDataUpload());
        cbAllowAccDataUpload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                manager.setAllowAccDataUpload(isChecked);
            }
        });
        cbDisplayBP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                manager.setABPMDisplay(isChecked, new Callback() {
                    @Override
                    public void onComplete(Map<String, Object> data) {

                    }

                    @Override
                    public void onError(int code, String msg) {
                        showToast(msg);
                        cbDisplayBP.setChecked(!isChecked, false);
                    }
                });
            }
        });
        cbSleep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                manager.setSleepMode(isChecked, new Callback() {
                    @Override
                    public void onComplete(Map<String, Object> data) {
                        llABPAutoSleep.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                        llABPAuto.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onError(int code, String msg) {
                        showToast(msg);
                        cbSleep.setChecked(!isChecked, false);
                    }
                });

            }
        });
        manager.setOnSettingsListener(new OnABPMSettingsListener() {
            @Override
            public void onSettingsDataCallback(ABPMSettingsBean data) {
                setSettings(data);
                dismissProgressDialog();
            }
        });
    }

    private void setSettings(ABPMSettingsBean abpmSettingsBean) {
        if (null == abpmSettingsBean)
            return;
        saveSettings(abpmSettingsBean);
        cbDisplayBP.setChecked(abpmSettingsBean.isDisplay, false);
        cbSleep.setChecked(abpmSettingsBean.isSleepSetting, false);
        llABPAutoSleep.setVisibility(abpmSettingsBean.isSleepSetting ? View.VISIBLE : View.GONE);
        llABPAuto.setVisibility(abpmSettingsBean.isSleepSetting ? View.GONE : View.VISIBLE);
        btnSectionTime1.setText(getStartTime(abpmSettingsBean.startTimeSection1));
        btnSectionTime2.setText(getStartTime(abpmSettingsBean.startTimeSection2));
        btnSectionTime3.setText(getStartTime(abpmSettingsBean.startTimeSection3));
        btnSectionTime4.setText(getStartTime(abpmSettingsBean.startTimeSection4));
        btnSectionTime5.setText(getStartTime(abpmSettingsBean.startTimeSection5));
        btnSectionTime6.setText(getStartTime(abpmSettingsBean.startTimeSection6));
        btnInterval1.setText(getInterval(abpmSettingsBean.intervalSection1));
        btnSleepModeInterval.setText(getInterval(abpmSettingsBean.intervalSection1));
        btnInterval2.setText(getInterval(abpmSettingsBean.intervalSection2));
        btnInterval3.setText(getInterval(abpmSettingsBean.intervalSection3));
        btnInterval4.setText(getInterval(abpmSettingsBean.intervalSection4));
        btnInterval5.setText(getInterval(abpmSettingsBean.intervalSection5));
        btnInterval6.setText(getInterval(abpmSettingsBean.intervalSection6));
        if (abpmSettingsBean.startTime > -1) {
            mStartTime = abpmSettingsBean.startTime;
            mEndTime = abpmSettingsBean.endTime;
            btnAutoStartTime.setText(DateFormat.format(abpmSettingsBean.startTime, "yyyy-MM-dd HH:mm"));
            btnAutoStopTime.setText(DateFormat.format(abpmSettingsBean.endTime, "yyyy-MM-dd HH:mm"));
            llStartEndTime.setVisibility(View.VISIBLE);
            cbAutoStartTime.setChecked(true, false);
        } else {
            mStartTime = -1;
            mEndTime = -1;
            btnAutoStartTime.setText("Auto Start Time");
            btnAutoStopTime.setText("Auto Stop Time");
            llStartEndTime.setVisibility(View.GONE);
            cbAutoStartTime.setChecked(false, false);
        }
    }

    private static final String SETTINGS_KEY = "mmkv_abpm_settings";

    private void saveSettings(ABPMSettingsBean abpmSettingsBean) {
        MMKV.defaultMMKV().putString(SETTINGS_KEY, GSON.toJson(abpmSettingsBean));
    }

    private ABPMSettingsBean getSettings() {
        String settings = MMKV.defaultMMKV().getString(SETTINGS_KEY, "");
        if (TextUtils.isEmpty(settings)) {
            return null;
        }
        return GSON.fromJson(settings, ABPMSettingsBean.class);
    }

    private String getStartTime(int value) {
        if (value == -1 || value == 255)
            return "OFF";
        else
            return value + ":00";
    }

    private String getInterval(int value) {
        if (value == 0) {
            return "OFF";
        } else if (value == 1) {
            return "5 min";
        } else if (value == 2) {
            return "10 min";
        } else if (value == 3) {
            return "15 min";
        } else if (value == 4) {
            return "20 min";
        } else if (value == 5) {
            return "30 min";
        } else if (value == 6) {
            return "60 min";
        } else if (value == 7) {
            return "120 min";
        }
        return "OFF";
    }

    private long mStartTime = -1;
    private long mEndTime = -1;

    @OnClick(R.id.btnAutoStartTime)
    public void btnAutoStartTime() {
        DatimePicker picker = new DatimePicker(DeviceMenuABPMActivity.this);
        final DatimeWheelLayout wheelLayout = picker.getWheelLayout();
        picker.setOnDatimePickedListener(new OnDatimePickedListener() {
            @Override
            public void onDatimePicked(int year, int month, int day, int hour, int minute, int second) {
                String text = year + "-" + month + "-" + day + " " + hour + ":" + minute;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                mStartTime = calendar.getTimeInMillis();
                btnAutoStartTime.setText(DateFormat.format(mStartTime, "yyyy-MM-dd HH:mm"));
            }
        });
        wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
        wheelLayout.setTimeMode(TimeMode.HOUR_24_NO_SECOND);
        DatimeEntity start = new DatimeEntity();
        start.setDate(DateEntity.target(1900, 1, 1));
        start.setTime(TimeEntity.target(0, 0, 0));
        DatimeEntity end = new DatimeEntity();
        end.setDate(DateEntity.target(2155, 12, 31));
        end.setTime(TimeEntity.target(23, 59, 59));
        wheelLayout.setRange(start, end);
        wheelLayout.setDefaultValue(DatimeEntity.now());
        wheelLayout.setDateLabel("", "", "");
        wheelLayout.setTimeLabel("", "", "");
        picker.show();
    }

    @OnClick(R.id.btnAutoStopTime)
    public void btnAutoStopTime() {
        DatimePicker picker = new DatimePicker(DeviceMenuABPMActivity.this);
        final DatimeWheelLayout wheelLayout = picker.getWheelLayout();
        picker.setOnDatimePickedListener(new OnDatimePickedListener() {
            @Override
            public void onDatimePicked(int year, int month, int day, int hour, int minute, int second) {
                String text = year + "-" + month + "-" + day + " " + hour + ":" + minute;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                mEndTime = calendar.getTimeInMillis();
                btnAutoStopTime.setText(DateFormat.format(mEndTime, "yyyy-MM-dd HH:mm"));
            }
        });
        wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
        wheelLayout.setTimeMode(TimeMode.HOUR_24_NO_SECOND);
        DatimeEntity start = new DatimeEntity();
        start.setDate(DateEntity.target(1900, 1, 1));
        start.setTime(TimeEntity.target(0, 0, 0));
        DatimeEntity end = new DatimeEntity();
        end.setDate(DateEntity.target(2155, 12, 31));
        end.setTime(TimeEntity.target(23, 59, 59));
        wheelLayout.setRange(start, end);
        wheelLayout.setDefaultValue(DatimeEntity.now());
        wheelLayout.setDateLabel("", "", "");
        wheelLayout.setTimeLabel("", "", "");
        picker.show();
    }

    @OnClick(R.id.btnSubmit)
    public void btnSubmit() {
        if (mStartTime >= mEndTime) {
            showToast("End time is less than or equal to start time");
            return;
        }
        manager.setStartEndTime(mStartTime, mEndTime, new Callback() {
            @Override
            public void onComplete(Map<String, Object> data) {

            }

            @Override
            public void onError(int code, String msg) {
                showToast(msg);
            }
        });
    }

    @OnClick(R.id.btnSleepModeInterval)
    public void btnSleepModeInterval() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = {"OFF", "5", "10", "15", "20", "30", "60", "120"};
        builder.setTitle("Set Interval")
                .setItems(users, (dialog, which) -> {
                    manager.setABPMInterval(1, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnInterval1.setText(getInterval((Integer) data.get("interval")));
                            btnSleepModeInterval.setText(getInterval((Integer) data.get("interval")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });

                });
        builder.create().show();
    }

    @OnClick(R.id.btnInterval1)
    public void btnInterval1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = {"OFF", "5", "10", "15", "20", "30", "60", "120"};
        builder.setTitle("Set Interval")
                .setItems(users, (dialog, which) -> {
                    manager.setABPMInterval(1, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnInterval1.setText(getInterval((Integer) data.get("interval")));
                            btnSleepModeInterval.setText(getInterval((Integer) data.get("interval")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });

                });
        builder.create().show();
    }

    @OnClick(R.id.btnInterval2)
    public void btnInterval2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = {"OFF", "5", "10", "15", "20", "30", "60", "120"};
        builder.setTitle("Set Interval")
                .setItems(users, (dialog, which) -> {
                    manager.setABPMInterval(2, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnInterval2.setText(getInterval((Integer) data.get("interval")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnInterval3)
    public void btnInterval3() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = {"OFF", "5", "10", "15", "20", "30", "60", "120"};
        builder.setTitle("Set Interval")
                .setItems(users, (dialog, which) -> {
                    manager.setABPMInterval(3, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnInterval3.setText(getInterval((Integer) data.get("interval")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnInterval4)
    public void btnInterval4() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = {"OFF", "5", "10", "15", "20", "30", "60", "120"};
        builder.setTitle("Set Interval")
                .setItems(users, (dialog, which) -> {
                    manager.setABPMInterval(4, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnInterval4.setText(getInterval((Integer) data.get("interval")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnInterval5)
    public void btnInterval5() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = {"OFF", "5", "10", "15", "20", "30", "60", "120"};
        builder.setTitle("Set Interval")
                .setItems(users, (dialog, which) -> {
                    manager.setABPMInterval(5, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnInterval5.setText(getInterval((Integer) data.get("interval")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnInterval6)
    public void btnInterval6() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = {"OFF", "5", "10", "15", "20", "30", "60", "120"};
        builder.setTitle("Set Interval")
                .setItems(users, (dialog, which) -> {
                    manager.setABPMInterval(6, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnInterval6.setText(getInterval((Integer) data.get("interval")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnSectionTime1)
    public void btnSectionTime1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = new String[25];
        users[0] = "OFF";
        for (int i = 0; i < 24; i++) {
            users[i + 1] = i + ":00";
        }
        builder.setTitle("Set Start Time")
                .setItems(users, (dialog, which) -> {
                    if (which == 0) {
                        which = 255;
                    } else {
                        which = which - 1;
                    }
                    manager.setABPMStartTime(1, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnSectionTime1.setText(getStartTime((Integer) data.get("time")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnSectionTime2)
    public void btnSectionTime2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = new String[25];
        users[0] = "OFF";
        for (int i = 0; i < 24; i++) {
            users[i + 1] = i + ":00";
        }
        builder.setTitle("Set Start Time")
                .setItems(users, (dialog, which) -> {
                    if (which == 0) {
                        which = 255;
                    } else {
                        which = which - 1;
                    }
                    manager.setABPMStartTime(2, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnSectionTime2.setText(getStartTime((Integer) data.get("time")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnSectionTime3)
    public void btnSectionTime3() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = new String[25];
        users[0] = "OFF";
        for (int i = 0; i < 24; i++) {
            users[i + 1] = i + ":00";
        }
        builder.setTitle("Set Start Time")
                .setItems(users, (dialog, which) -> {
                    if (which == 0) {
                        which = 255;
                    } else {
                        which = which - 1;
                    }
                    manager.setABPMStartTime(3, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnSectionTime3.setText(getStartTime((Integer) data.get("time")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnSectionTime4)
    public void btnSectionTime4() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = new String[25];
        users[0] = "OFF";
        for (int i = 0; i < 24; i++) {
            users[i + 1] = i + ":00";
        }
        builder.setTitle("Set Start Time")
                .setItems(users, (dialog, which) -> {
                    if (which == 0) {
                        which = 255;
                    } else {
                        which = which - 1;
                    }
                    manager.setABPMStartTime(4, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnSectionTime4.setText(getStartTime((Integer) data.get("time")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnSectionTime5)
    public void btnSectionTime5() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = new String[25];
        users[0] = "OFF";
        for (int i = 0; i < 24; i++) {
            users[i + 1] = i + ":00";
        }
        builder.setTitle("Set Start Time")
                .setItems(users, (dialog, which) -> {
                    if (which == 0) {
                        which = 255;
                    } else {
                        which = which - 1;
                    }
                    manager.setABPMStartTime(5, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnSectionTime5.setText(getStartTime((Integer) data.get("time")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @OnClick(R.id.btnSectionTime6)
    public void btnSectionTime6() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] users = new String[25];
        users[0] = "OFF";
        for (int i = 0; i < 24; i++) {
            users[i + 1] = i + ":00";
        }
        builder.setTitle("Set Start Time")
                .setItems(users, (dialog, which) -> {
                    if (which == 0) {
                        which = 255;
                    } else {
                        which = which - 1;
                    }
                    manager.setABPMStartTime(6, which, new Callback() {
                        @Override
                        public void onComplete(Map<String, Object> data) {
                            btnSectionTime6.setText(getStartTime((Integer) data.get("time")));
                        }

                        @Override
                        public void onError(int code, String msg) {
                            showToast(msg);
                        }
                    });
                });
        builder.create().show();
    }

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.activity_device_master_abpm);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
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

    @OnClick(R.id.btnDisconnect)
    void clickBtnDisconnect() {
        showProgressDialog("Disconnecting...");
        DeviceManager.getInstance().disconnect(mDevice);
    }
}
