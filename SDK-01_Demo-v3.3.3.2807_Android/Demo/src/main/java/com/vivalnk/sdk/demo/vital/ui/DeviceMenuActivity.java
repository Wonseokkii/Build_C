package com.vivalnk.sdk.demo.vital.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.OnClick;

import com.google.gson.GsonBuilder;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.tencent.mmkv.MMKV;
import com.vivalnk.sdk.BuildConfig;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.CommandRequest;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.command.base.CommandAllType;
import com.vivalnk.sdk.command.base.CommandType;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.base.custom.BackProgressDialog;
import com.vivalnk.sdk.demo.base.i18n.ErrorMessageHandler;
import com.vivalnk.sdk.demo.base.utils.NotificationUtils;
import com.vivalnk.sdk.demo.base.widget.LogListDialogView;
import com.vivalnk.sdk.demo.core.WfdbUtils;
import com.vivalnk.sdk.demo.repository.database.DatabaseManager;
import com.vivalnk.sdk.demo.repository.database.exception.DataEmptyExeption;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.device.vv330.DataStreamMode;
import com.vivalnk.sdk.device.vv330.VV330Manager;
import com.vivalnk.sdk.device.vv330.VV330Manager.CalibrationCallback;
import com.vivalnk.sdk.engineer.test.FileManager;
import com.vivalnk.sdk.model.BatteryInfo;
import com.vivalnk.sdk.model.BatteryInfo.ChargeStatus;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceInfoUtils;
import com.vivalnk.sdk.model.PatchStatusInfo;
import com.vivalnk.sdk.model.SampleData;
import com.vivalnk.sdk.model.cloud.CloudEvent;
import com.vivalnk.sdk.model.common.DataType.DataKey;
import com.vivalnk.sdk.open.manager.SubjectManager;
import com.vivalnk.sdk.utils.DateFormat;
import com.vivalnk.sdk.utils.GSON;
import com.vivalnk.sdk.utils.MapUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
//import io.reactivex.rxjava3.core.Observable;
//import io.reactivex.rxjava3.core.ObservableEmitter;
//import io.reactivex.rxjava3.core.ObservableOnSubscribe;
//import io.reactivex.rxjava3.core.Observer;
//import io.reactivex.rxjava3.disposables.Disposable;
//import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 设备菜单界面
 *
 * @author jake
 * @Date 2019/3/15
 */
public class DeviceMenuActivity extends ConnectedActivity {

  @BindView(R.id.btnDetail)
  Button mBtnDetail;
  @BindView(R.id.tvStatus)
  TextView mTvStatus;
  //vv310
  @BindView(R.id.btnUploadFlash)
  Button btnUploadFlash;
  @BindView(R.id.btnCancelUpload)
  Button btnCancelUpload;
  @BindView(R.id.btnEngineerModule)
  Button btnEngineerModule;

  private LogListDialogView mDataLogView;
  private LogListDialogView mOperationLogView;

  private NotificationUtils mNotificationUtils;

  String ENGINEER_CLASS = "com.vivalnk.sdk.engineer.ui.EngineerActivity";

  /**
   * @see com.vivalnk.sdk.DeviceStatusListener#onFrameDataStatusChange(Device, long)
   */
  private long mRemainderFrameData;
  private androidx.appcompat.app.AlertDialog mRemainderFrameDataDialog;

  @Subscribe
  public void onDataUpdate(SampleData data) {
    if (!data.getDeviceID().equals(mDevice.getId())) {
      return;
    }
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (mDataLogView == null) {
          return;
        }
        mDataLogView.updateLog(data.toSimpleString());

      }
    });
  }



  @Subscribe
  public void onBatteryEvent(DeviceManager.BatteryData batteryData) {
    if (!batteryData.device.equals(mDevice)) {
      return;
    }

    mTvStatus.setText(batteryData.batteryInfo.getNotifyStr());

    if (batteryData.batteryInfo.isLowBattery() && batteryData.batteryInfo.getStatus() == ChargeStatus.NOT_INCHARGING) {
      mNotificationUtils.sendNotification(mDevice.getName(), getString(R.string.low_battery_warning));
      return;
    }

    if (batteryData.batteryInfo.getStatus() == ChargeStatus.INCHARGING_ABNORMAL) {
      mNotificationUtils.sendNotification(mDevice.getName(), getString(R.string.battery_info_status_charger_connected_abnormal));
      return;
    }

    if (batteryData.batteryInfo.getStatus() == ChargeStatus.INCHARGING_STOPPED) {
      mNotificationUtils.sendNotification(mDevice.getName(), getString(R.string.battery_info_status_charger_connected_stopped));
      return;
    }

    if (batteryData.batteryInfo.getStatus() == ChargeStatus.INCHARGING_NOT_STARTED) {
      mNotificationUtils.sendNotification(mDevice.getName(), getString(R.string.battery_info_status_charger_connected_not_started));
      return;
    }

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
   // 设置常亮
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    mNotificationUtils = new NotificationUtils(this.getApplicationContext());
    initView();
    mRemainderFrameData = 0;
  }

  private void initView() {
    if (DeviceInfoUtils.isVV310(mDevice)) {
      btnUploadFlash.setVisibility(View.VISIBLE);
      btnCancelUpload.setVisibility(View.VISIBLE);
    } else {
      btnUploadFlash.setVisibility(View.GONE);
      btnCancelUpload.setVisibility(View.GONE);
    }

    mDataLogView = new LogListDialogView();
    mOperationLogView = new LogListDialogView();

    mDataLogView.create(this);
    mOperationLogView.create(this);

    initEngineerModule();
  }

  private void initEngineerModule() {
    try {
      Class.forName(ENGINEER_CLASS);
      btnEngineerModule.setVisibility(View.VISIBLE);
    } catch (ClassNotFoundException e) {
      btnEngineerModule.setVisibility(View.GONE);
    }
  }

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_device_detail);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
//    取消常亮
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    mNotificationUtils = null;
    if (calibrationProgressDialog != null) {
      calibrationProgressDialog.dismiss();
    }
  }

  @OnClick(R.id.btnDetail)
  void clickBtnDetail() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.title_todo)
        .setItems(R.array.log_details, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            //DataLog
            if (which == 0) {
              mDataLogView.show();
              //Operation Log
            } else if (which == 1) {
              mOperationLogView.show();
            }
          }
        });
    AlertDialog dialog = builder.create();
    dialog.show();
  }

  //int count = 0;
  //private void requestRTTReadSN() {
  //  count = 0;
  //  CommandRequest readSnFromPatch = new CommandRequest.Builder().setType(CommandAllType.readSnFromPatch).build();
  //  execute(readSnFromPatch, new DefaultCallback(){
  //    @Override
  //    public void onComplete(Map<String, Object> data) {
  //      super.onComplete(data);
  //      count++;
  //      if (count < 100) {
  //        execute(readSnFromPatch, this);
  //      }else {
  //        count = 0;
  //        requestRTTReadInfo();
  //      }
  //    }
  //  });
  //}
  //
  //private void requestRTTReadInfo() {
  //  CommandRequest readUserInfoFromFlash = new CommandRequest.Builder().setType(CommandAllType.readUserInfoFromFlash).build();
  //  execute(readUserInfoFromFlash, new DefaultCallback(){
  //    @Override
  //    public void onComplete(Map<String, Object> data) {
  //      super.onComplete(data);
  //      count++;
  //      if (count < 100) {
  //        execute(readUserInfoFromFlash, this);
  //      }else {
  //        count = 0;
  //        requestRTTReadDeviceInfo();
  //      }
  //    }
  //  });
  //}
  //private void requestRTTReadDeviceInfo() {
  //  CommandRequest readDeviceInfo = new CommandRequest.Builder().setType(CommandAllType.readDeviceInfo).build();
  //  execute(readDeviceInfo, new DefaultCallback(){
  //    @Override
  //    public void onComplete(Map<String, Object> data) {
  //      super.onComplete(data);
  //      count++;
  //      if (count < 100) {
  //        execute(readDeviceInfo, this);
  //      }else {
  //        count = 0;
  //      }
  //    }
  //  });
  //}

  @OnClick(R.id.btnDisconnect)
  void clickBtnDisconnect() {
    showProgressDialog("Disconnecting...");
    DeviceManager.getInstance().disconnect(mDevice);
  }

  @OnClick(R.id.btnReadPatchVersion)
  public void clickReadPatchVersion(Button view) {
    execute(CommandType.readPatchVersion, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        String hwVersion = (String) data.get("hwVersion");
        String fwVersion = (String) data.get("fwVersion");
        showAlertDialog("Patch Version", getString(R.string.device_read_patch_version, hwVersion, fwVersion));
      }
    });
  }

  @OnClick(R.id.btnReadDeviceInfo)
  public void clickReadDeviceInfo(Button view) {
    execute(CommandType.readDeviceInfo, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        Integer productTypeInt = MapUtils.parserMapValue(data, "productType", Integer.class, 0);
        String productType = "0(VV330)";
        if (productTypeInt == 0) {
          productType = "0(VV330)";
        } else if(productTypeInt == 1) {
          productType = "1(VV350)";
        }
        String magnification = (String) data.get("magnification");
        String samplingFrequency = (String) data.get("ecgSamplingFrequency");
        String model = (String) data.get("model");
        String encryption = (String) data.get("encryption");
        String manufacturer = (String) data.get("manufacturer");
        String info = (String) data.get("info");
        String TroyHR = (String) data.get("hasHR");
        showAlertDialog("Device Information", getString(R.string.device_read_device_info, productType, magnification, samplingFrequency, model,
            encryption, manufacturer, info));
      }
    });
  }

  @OnClick(R.id.btnShowQueueSize)
  public void clickShowQueueSize(Button view) {
      String message = getString(R.string.str_unprocessed_amount, String.valueOf(mRemainderFrameData));
      if (mRemainderFrameDataDialog == null) {
          mRemainderFrameDataDialog = showAlertDialogSync("Unprocessed Data Amount", message);
      } else {
          mRemainderFrameDataDialog.setMessage(message);
          if (!mRemainderFrameDataDialog.isShowing()){
              mRemainderFrameDataDialog.show();
          }
      }
  }

  @OnClick(R.id.btnReadSn)
  public void clickReadSN(Button view) {
    execute(CommandType.readSnFromPatch);
  }

  @OnClick(R.id.btnQueryFlash)
  public void clickQueryFlashCount(Button view) {
    execute(CommandType.checkFlashDataStatus, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        long number = (long) data.get("number"); //bytes
        if (data.containsKey("totalNumber") && data.containsKey("seconds")) {
          long totalNumber = (long) data.get("totalNumber"); //bytes
          //unit seconds
          long seconds = (long) data.get("seconds");
          showAlertDialog("Flash Info", getString(R.string.flash_info_new, String.valueOf(totalNumber), String.valueOf(number), String.valueOf(seconds)));
        } else {
          showAlertDialog("Flash Info", getString(R.string.flash_info_old, String.valueOf(number)));
        }
      }
    });
  }

  @OnClick(R.id.btnCheckPatchStatus)
  public void clickCheckPatchStatus(Button view) {
    execute(CommandType.checkPatchStatus, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        PatchStatusInfo patchStatusInfo = (PatchStatusInfo) data.get("data");
        try {
          InfoDialog.newInstance(mDevice, patchStatusInfo).show(getSupportFragmentManager(), InfoDialog.TAG);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  @OnClick(R.id.btnSwitchDataMode)
  public void clickSwitchDataMode(Button view) {

    Callback callback = new Callback() {
      @Override
      public void onStart() {
        showProgressDialog("start switch Mode");
      }

      @Override
      public void onComplete(Map<String, Object> data) {
        dismissProgressDialog();
        showToast("switch mode successful");
        DataStreamMode mode = DeviceManager.getInstance().getVV330Manager(mDevice).getDataStreamMode();
        DeviceManager.putDataStreamMode(mDevice, mode);
      }

      @Override
      public void onError(int code, String msg) {
        dismissProgressDialog();
        showToast("switch mode error: code = " + code + ", msg = " + msg);
      }
    };
    if (BuildConfig.sdkChannel.equals("sdk01")) {
      showSwitchDataStreamModeDialog(callback);
    } else {
      showSwitchDataStreamMode01ADialog(callback);
    }


  }

  private void showSwitchDataStreamMode01ADialog(Callback callback) {
    VV330Manager vv330Manager = DeviceManager.getInstance().getVV330Manager(mDevice);

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Switch Data Stream Mode")
            .setItems(R.array.stream_mode_01a, (dialog, which) -> {
             if (which == 0) {
               vv330Manager.switchToLiveMode(mDevice, callback);
             } else if (which == 1) {
               vv330Manager.switchToFullDualMode(mDevice, callback);
             }
            });
    builder.create().show();
  }

  private void showSwitchDataStreamModeDialog(Callback callback) {
    VV330Manager vv330Manager = DeviceManager.getInstance().getVV330Manager(mDevice);

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Switch Data Stream Mode")
            .setItems(R.array.stream_mode, (dialog, which) -> {
              switch (which) {
                case 0: //NoneMode
                  vv330Manager.switchToNoneMode(mDevice, callback);
                  break;
                case 1: //DualMode
                  vv330Manager.switchToDualMode(mDevice, callback);
                  break;
                case 2: //LiveMode
                  vv330Manager.switchToLiveMode(mDevice, callback);
                  break;
                case 3: //FullDualMode
                  vv330Manager.switchToFullDualMode(mDevice, callback);
                  break;
                case 4: //RTSMode
                  vv330Manager.switchToRTSMode(mDevice, callback);
                  break;
              }
            });
    builder.create().show();
  }

  BackProgressDialog calibrationProgressDialog = null;
  @OnClick(R.id.btnStartActivityCalibration)
  public void click_btnStartActivityCalibration() {
    VV330Manager vv330Manager = DeviceManager.getInstance().getVV330Manager(mDevice);
    vv330Manager.startActivityCalibration(new CalibrationCallback() {
      @Override
      public void onStart() {
        if (calibrationProgressDialog != null) {
          calibrationProgressDialog.dismiss();
        }
        calibrationProgressDialog = createProgressDialog(100, "on Activity calibration...", true, true);
        calibrationProgressDialog.show();
      }

      @Override
      public void onProgress(int progress) {
        if (calibrationProgressDialog != null) {
          calibrationProgressDialog.setProgress(progress);
        }
      }

      @Override
      public void onFailure(int code, String msg) {
        if (code == CALIBRATION_IN_PROCESS) {
          if (calibrationProgressDialog != null) {
            calibrationProgressDialog.show();
          }
          return;
        }
        if (calibrationProgressDialog != null) {
          calibrationProgressDialog.dismiss();
        }
        showAlertDialog("Activity Calibration Result", "Calibration failure, coe = " + code + ", reason = " + msg);
      }

      @Override
      public void onCancel() {
        if (calibrationProgressDialog != null) {
          calibrationProgressDialog.dismiss();
        }
        showToast("user cancel the calibration process");
      }

      @Override
      public void onSuccess(Map<String, Object> data) {
        if (calibrationProgressDialog != null) {
          calibrationProgressDialog.dismiss();
        }
        showAlertDialog("Activity Calibration Result", "Calibration Success!!!");
      }
    });
  }

  @OnClick(R.id.btnCancelActivityCalibration)
  public void click_btnCancelActivityCalibration() {
    VV330Manager vv330Manager = DeviceManager.getInstance().getVV330Manager(mDevice);
    vv330Manager.cancelCurrentActivityCalibration();
  }

  CalibrationCallback postureCalibrationCallback = new CalibrationCallback() {
    @Override
    public void onStart() {
      if (calibrationProgressDialog != null) {
        calibrationProgressDialog.dismiss();
      }
      calibrationProgressDialog = createProgressDialog(100, "on Posture calibration...", true, true);
      calibrationProgressDialog.show();
    }

    @Override
    public void onProgress(int progress) {
      if (calibrationProgressDialog != null) {
        calibrationProgressDialog.setProgress(progress);
      }
    }

    @Override
    public void onFailure(int code, String msg) {
      if (code == CALIBRATION_IN_PROCESS) {
        if (calibrationProgressDialog != null) {
          calibrationProgressDialog.show();
        }
        return;
      }

      if (calibrationProgressDialog != null) {
        calibrationProgressDialog.dismiss();
      }
      showAlertDialog("Posture Calibration Result", "Calibration failure, coe = " + code + ", reason = " + msg);
    }

    @Override
    public void onCancel() {
      if (calibrationProgressDialog != null) {
        calibrationProgressDialog.dismiss();
      }
      showToast("user cancel the calibration process");
    }

    @Override
    public void onSuccess(Map<String, Object> data) {
      if (calibrationProgressDialog != null) {
        calibrationProgressDialog.dismiss();
      }
      showAlertDialog("Posture Calibration Result", "Calibration Success!!!");
    }
  };
  @OnClick(R.id.btnStartPostureCalibration_Sitting)
  public void click_btnStartPostureCalibration_Sitting() {
    VV330Manager vv330Manager = DeviceManager.getInstance().getVV330Manager(mDevice);
    vv330Manager.startCalibrateSitting(postureCalibrationCallback);
  }

  @OnClick(R.id.btnStartPostureCalibration_Lying)
  public void click_btnStartPostureCalibration_Lying() {
    VV330Manager vv330Manager = DeviceManager.getInstance().getVV330Manager(mDevice);
    vv330Manager.startCalibrateLying(postureCalibrationCallback);
  }

  @OnClick(R.id.btnCancelPostureCalibration)
  public void click_btnCancelPostureCalibration() {
    VV330Manager vv330Manager = DeviceManager.getInstance().getVV330Manager(mDevice);
    vv330Manager.cancelCurrentPostureCalibration();
  }

  @OnClick(R.id.btnEnableLeadOffAcc)
  public void clickEnableLeadOffAcc(Button view) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Enable/Disable Lead off Acc Sampling")
        .setItems(R.array.enable_disable, (dialog, which) -> {
          boolean enable = which == 0;
          CommandRequest leadOffAccASampling = getCommandRequest(CommandAllType.leadOffAccSampling,
              10 * 1000, PatchStatusInfo.Key.enable, enable);
          execute(leadOffAccASampling);
        });
    builder.create().show();
  }

  @OnClick(R.id.btnAccSamplingSwitch)
  public void btnAccSamplingSwitch(Button view) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Enable/Disable Acc Sampling")
        .setItems(R.array.enable_disable, (dialog, which) -> {
          boolean enable = which == 0;
          CommandRequest leadOffAccASampling = getCommandRequest(CommandAllType.accSamplingSwitch,
              10 * 1000, PatchStatusInfo.Key.enable, enable);
          execute(leadOffAccASampling);
        });
    builder.create().show();
  }

  @OnClick(R.id.btnUploadFlash)
  public void clickUploadFlash(Button view) {
    CommandRequest uploadFlashRequest = getCommandRequest(CommandType.uploadFlash, 10 * 1000);
    execute(uploadFlashRequest);
  }

  @OnClick(R.id.btnCancelUpload)
  public void clickCancelUpload(Button view) {
    execute(CommandType.cancelUploadFlash);
  }

  @OnClick(R.id.btnEraseFlash)
  public void clickEraseFlash(Button view) {
    execute(CommandType.eraseFlash);
  }

  @OnClick(R.id.btnStartSampling)
  public void clickStartSampling(Button view) {
    execute(CommandType.startSampling);
  }

  @OnClick(R.id.btnStopSampling)
  public void clickStopSampling(Button view) {
    execute(CommandType.stopSampling);
  }

  @OnClick(R.id.btnShutDown)
  public void clickShutdown(Button view) {
    execute(CommandType.shutdown, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        showProgressDialog("Shutdown...");
      }
    });
  }

  @OnClick(R.id.btnSelfTest)
  public void clickSelfTest(Button view) {
    CommandRequest selfTestRequest = getCommandRequest(CommandType.selfTest, 10000);
    execute(selfTestRequest, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        BatteryInfo batteryInfo = (BatteryInfo) data.get("batteryInfo");
        InfoDialog.newInstance(mDevice, batteryInfo)
            .show(getSupportFragmentManager(), InfoDialog.TAG);
      }

//      @Override
//      public void onError(int code, String msg) {
//        if (code == 4102){
//          CommandRequest restartDevice = new CommandRequest.Builder()
//                  .setTimeout(3000)
//                  .setType(CommandAllType.writeTestParameters)
//                  .addParam(WriteTestParametersCore.KEY_testMode, 4)
//                  .addParam(WriteTestParametersCore.KEY_beginTime, -1)
//                  .build();
//          execute(restartDevice, null, true, true, true);
//        }
//      }
    });
  }

  @OnClick(R.id.btnSetPatchClock)
  public void clickSetPatchClock(Button view) {
    execute(CommandType.setPatchClock);
  }

  @OnClick(R.id.btnReadPatchClock)
  public void clickReadPatchClock(Button view) {
    execute(CommandType.readPatchClock, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm.ss");
        String timeStr = sdf.format(new Date((Long) data.get("time")));
        showAlertDialog("Patch Current Clock", "currentTime: " + timeStr);
      }
    });
  }

  @OnClick(R.id.btnReadUserInfo)
  public void clickReadlUserInfo(Button view) {
    execute(CommandType.readUserInfoFromFlash);
  }

  @OnClick(R.id.btnEraseUserInfo)
  public void clickEraseUserInfo(Button view) {
    execute(CommandType.eraseUserInfoFromFlash);
  }

  @OnClick(R.id.btnSetUserInfo)
  public void clickSetUserInfo(Button view) {
    final EditText et = new EditText(this);
    et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
    AlertDialog mUserInfoDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.input_text_hint)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setView(et)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            String input = et.getText().toString();
            if (TextUtils.isEmpty(input)) {
              showToast(R.string.input_text_empty);
            } else {
              CommandRequest setUserInfoRequest = getCommandRequest(CommandType.setUserInfoToFlash,
                  3000, "info", input);
              execute(setUserInfoRequest);
            }
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
    mUserInfoDialog.setCanceledOnTouchOutside(false);
  }

  @OnClick(R.id.btnGraphics)
  void clickBtnGraphics() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.title_todo)
        .setItems(R.array.data_graphics, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            //RTS
            if (which == 0) {
              navToConnectedActivity(mDevice, MotionGraphicActivity.class);
              //History
            } else if (which == 1) {
//              navToConnectedActivity(mDevice, HistoryActivity.class);
              navToConnectedActivity(mDevice, CustomEcgViewActivity.class);
            } else if (which == 2) {
              navToConnectedActivity(mDevice, CustomEcgViewActivity.class);
            }
          }
        });
    AlertDialog dialog = builder.create();
    dialog.show();
  }

  @OnClick(R.id.btnClearDatabase)
  public void clickClearDatabase() {
    DatabaseManager.getInstance().getDataDAO().deleteAll();
    showToast("delete all sample data success!");
  }

  SubjectManager subjectManager = new SubjectManager();
  @OnClick(R.id.btnRegisterSubject)
  public void btnRegisterSubject() {
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

            subjectManager.register(projectId, subjectId, new Callback() {
                  @Override
                  public void onStart() {
                    showProgressDialog("register subject...");
                  }

                  @Override
                  public void onComplete(Map<String, Object> data) {
                    showToast("register success:" + GSON.toJson(data));
                    dismissProgressDialog();
                  }

                  @Override
                  public void onError(int code, String msg) {
                    showToast("register error: " + code + ", " + msg);
                    dismissProgressDialog();
                  }
                });

          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  @OnClick(R.id.btnBindDevice)
  public void btnBindDevice() {
    View view = LayoutInflater.from(this).inflate(R.layout.subject_id_layout, null);
    final EditText edtProjectId = (EditText) view.findViewById(R.id.edtProjectId);
    final EditText edtSubjectId = (EditText) view.findViewById(R.id.edtSubjectId);
    new AlertDialog.Builder(this).setTitle("Bind Device")
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
            subjectManager.bindDevice(mDevice.getName(), projectId, subjectId, System.currentTimeMillis());

          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  @OnClick(R.id.btnPostUserEvent)
  public void btnPostUserEvent() {
    View view = LayoutInflater.from(this).inflate(R.layout.subject_id_layout, null);
    final EditText edtProjectId = (EditText) view.findViewById(R.id.edtProjectId);
    final EditText edtSubjectId = (EditText) view.findViewById(R.id.edtSubjectId);
    new AlertDialog.Builder(this).setTitle("Post an User Event")
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

            CloudEvent cloudEvent = CloudEvent.userEvent(mDevice.getName(),
                System.currentTimeMillis() - 120 * 1000,
                System.currentTimeMillis(),
                "an user event")
                .setSubjectId(subjectId);

            subjectManager.eventUpload(projectId, cloudEvent);

          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  @OnClick(R.id.btnExportMIT16)
  public void clickExportMIT16() {
    Observable.create(new ObservableOnSubscribe<Object>() {
      @Override
      public void subscribe(ObservableEmitter<Object> emitter) throws Exception {

        String timeStr = (System.currentTimeMillis() / 1000) + "";
        String name = mDevice.getSn().replace('/', '_');
        String heaFile = FileManager.getFileDataPath(mDevice.getSn(), name + "_" + timeStr);
        String dataFile = FileManager.getFileDataPath(mDevice.getSn(), name + "_" + timeStr + ".dat");

        List<SampleData>
            data = DatabaseManager.getInstance().getDataDAO().queryAllOrderByTimeASC(mDevice.getId());

        if (data.size() <= 0) {
          emitter.onError(new DataEmptyExeption("empty database"));
          return;
        }

        SampleData firstdata = data.get(0);

        WfdbUtils.initFile(dataFile, heaFile);

        WfdbUtils.initSignalInfo(
            firstdata.getECG().length,
            16,
            "sample data",
            "mV",
            DeviceInfoUtils.getMagnification(mDevice),
            16,
            0);

        WfdbUtils.open();

        String time = DateFormat.format(firstdata.getTime(), "HH:mm:ss yyyy/MM/dd");
        WfdbUtils.setBaseTime(time);

        SampleData preData = data.get(0);
        WfdbUtils.doSample(preData.getECG());

        for (int i = 1; i < data.size(); i++) {

          long deltaTime = data.get(i).time - preData.getTime();
          if (deltaTime >= 2000) {
            //there are data missing, should fill by default zero value
            long delta = deltaTime / 1000 - 1;
            for (int j = 0; j < delta; j++) {
              int[] deltaEcg = new int[preData.getECG().length];
              Arrays.fill(deltaEcg, 8700);
              WfdbUtils.doSample(deltaEcg);
            }
          }

          SampleData dataI = data.get(i);
          int[] ecg = dataI.getECG();
          WfdbUtils.doSample(ecg);
          preData = dataI;
        }

        WfdbUtils.newHeader();

        WfdbUtils.close();

        emitter.onNext(new Object());
        emitter.onComplete();
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Object>() {
          @Override
          public void onSubscribe(Disposable d) {
            showProgressDialog("processing...");
          }

          @Override
          public void onNext(Object o) {

          }

          @Override
          public void onError(Throwable e) {
            showToast(e.getMessage());
            dismissProgressDialog();
          }

          @Override
          public void onComplete() {
            showToast("process complete, please see the data file");
            dismissProgressDialog();
          }
        });
  }


  private static final int OTA_RET_CODE = 2019;
  private static final int ACTIVITY_CHOOSE_FILE = 3;
  @OnClick(R.id.btnOTA)
  public void clickOTA() {
    OTAActivity.openFileSelector(DeviceMenuActivity.this, ACTIVITY_CHOOSE_FILE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == ACTIVITY_CHOOSE_FILE) {
      if (resultCode != RESULT_OK || data == null) {
        isOTAing = false;
      } else {
        String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
        startActivityForResult(OTAActivity.newIntent(this, mDevice, filePath), OTA_RET_CODE);
        isOTAing = true;
      }
    } else if (requestCode == OTA_RET_CODE) {
      isOTAing = false;
      if (resultCode != Activity.RESULT_OK) {
        showToast("OTA failed");
      }
      finish();
    } else {
      finish();
    }
  }

  @OnClick(R.id.btnEngineerModule)
  public void openEngineerModule() {
    try {
      // look for engineer Activity
      Intent engineerActivity = new Intent(this, Class.forName(ENGINEER_CLASS));
      engineerActivity.putExtra("device", mDevice);
      startActivity(engineerActivity);
    } catch (final Exception e) {
      showToast(R.string.error_no_support_engineer_module);
    }
  }

  @OnClick(R.id.btnAnimalHrAlgoEnable)
  void clickBtnAnimalHrAlgoEnable() {
//    VV330Manager vv330Manager = DeviceManager.getInstance().getVV330Manager(mDevice);
//
//    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//    builder.setTitle("Enable/Disable Animal HR Algo. current: " + vv330Manager.isAnimalHrAlgoEnable(mDevice))
//        .setItems(R.array.enable_disable, (dialog, which) -> {
//          boolean enable = which == 0;
//          vv330Manager.setAnimalHrAlgoEnable(mDevice, enable);
//          if(enable){
//            if (enable == vv330Manager.isAnimalHrAlgoEnable(mDevice)) {
//              showToast(R.string.str_animal_hr_algo_enable_success);
//            } else {
//              showToast(R.string.str_animal_hr_algo_enable_failed);
//            }
//          } else{
//            if (enable == vv330Manager.isAnimalHrAlgoEnable(mDevice)) {
//              showToast(R.string.str_animal_hr_algo_disable_success);
//            } else {
//              showToast(R.string.str_animal_hr_algo_disable_failed);
//            }
//          }
//        });
//    builder.create().show();
  }

  public void execute(final int requestType, final Callback callback) {
    CommandRequest request = getCommandRequest(requestType, 3000);
    execute(request, callback);
  }

  public void execute(final CommandRequest request, final Callback callback) {
    super.execute(request, new Callback() {
      @Override
      public void onStart() {
        if (null != callback) {
          callback.onStart();
        }
      }

      @Override
      public void onComplete(Map<String, Object> data) {
        if (null != callback) {
          callback.onComplete(data);
        }
      }

      @Override
      public void onError(int code, String msg) {
        if (null != callback) {
          callback.onError(code, msg);
        }
      }
    });
  }

  public void execute(CommandRequest request, Callback callback, boolean showProgressDialog, boolean showSuccessDialog, boolean showErrorDialog) {
    DeviceManager.getInstance().execute(mDevice, request, new Callback() {
      @Override
      public void onStart() {
        String log = request.getTypeName() + ": onStart";
        if (mOperationLogView != null) {
          mOperationLogView.updateLog(log);
        }
        if (showProgressDialog) {
          showProgressDialog(ErrorMessageHandler.getInstance().getOnStartMessage(mDevice.getModel(), request.getType()));
        }
        if (null != callback) {
          callback.onStart();
        }
      }

      @Override
      public void onComplete(Map<String, Object> data) {
        dismissProgressDialog();
        String log = request.getTypeName() + ": " + (data != null ? "onComplete: data = " + new GsonBuilder().disableHtmlEscaping().create().toJson(data) : "onComplete");
        if (mOperationLogView != null) {
          mOperationLogView.updateLog(log);
        }
        if (showSuccessDialog) {
          showAlertDialog(request.getTypeName(), ErrorMessageHandler.getInstance().getOnCompleteMessage(mDevice.getModel(), request.getType(), data));
        }
        if (null != callback) {
          callback.onComplete(data);
        }
      }

      @Override
      public void onError(int code, String msg) {
        dismissProgressDialog();
        String log = request.getTypeName() + ": " + "onError: code = " + code + ", msg = " + msg;
        if (mOperationLogView != null) {
          mOperationLogView.updateLog(log);
        }
        if (showErrorDialog) {
          showAlertDialog(request.getTypeName(), ErrorMessageHandler.getInstance().getOnErrorMesage(mDevice.getModel(), request.getType(), code, msg));
        }
        if (null != callback) {
          callback.onError(code, msg);
        }
      }
    });
  }
}
