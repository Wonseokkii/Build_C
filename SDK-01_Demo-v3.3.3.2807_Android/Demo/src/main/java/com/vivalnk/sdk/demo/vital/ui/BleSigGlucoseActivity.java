package com.vivalnk.sdk.demo.vital.ui;

import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.base.custom.DialogActivity;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.CallbackUtils;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.device.sig.glucose.BleSIG_GlucoseManager;
import com.vivalnk.sdk.utils.GSON;
import java.util.Map;

public class BleSigGlucoseActivity extends ConnectedActivity {

  private static final String TAG = "BleSigGlucoseActivity";

  BleSIG_GlucoseManager manager;

  @BindView(R.id.bthGetAllRecord)
  Button bthGetLastRecord;

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_device_gls);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    manager = new BleSIG_GlucoseManager(mDevice);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void showAlertDialog(String title, String msg) {
    DialogActivity.showDialogActivity(this, title, msg);
  }

  @OnClick(R.id.btnDisconnect)
  void clickBtnDisconnect() {
    if (DeviceManager.getInstance().isConnected(mDevice)) {
      showProgressDialog("Disconnecting...");
      DeviceManager.getInstance().disconnect(mDevice);
    } else {
      finish();
    }
  }

  @OnClick(R.id.bthGetAllRecord)
  public void bthGetAllRecord() {
    CallbackUtils.getInstance().getAllRecords(this, manager, TAG);
  }

  @OnClick(R.id.bthGetFirstRecord)
  public void bthGetFirstRecord() {
    CallbackUtils.getInstance().getFirstRecord(this, manager, TAG);
  }

  @OnClick(R.id.bthGetLastRecord)
  public void bthGetLastRecord() {
    CallbackUtils.getInstance().getLastRecord(this, manager, TAG);
  }

  @OnClick(R.id.bthDeleteAllRecord)
  public void bthDeleteAllRecord() {
    manager.deleteAllRecords(new Callback() {
      @Override
      public void onStart() {
        showProgressDialog("staring...");
      }

      @Override
      public void onComplete(Map<String, Object> data) {
        dismissProgressDialog();
        showAlertDialog("Result of Delete All Records", GSON.toJson(data));
        VitalLog.d(TAG, GSON.toJson(data));
      }

      @Override
      public void onError(int code, String msg) {
        dismissProgressDialog();
        showAlertDialog("Result of Delete All Records", "error code = " + code + ", msg = " + msg);
      }
    });
  }


  @OnClick(R.id.bthReadBatteryLevel)
  public void bthReadBatteryLevel() {
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

  @OnClick(R.id.bthReadCurrentTime)
  public void bthReadCurrentTime() {
    manager.readCurrentTime(new Callback() {
      @Override
      public void onStart() {
        showProgressDialog("staring...");
      }

      @Override
      public void onComplete(Map<String, Object> data) {
        dismissProgressDialog();
        showAlertDialog("Result of Read Current Time", GSON.toJson(data));
        VitalLog.d(TAG, GSON.toJson(data));
      }

      @Override
      public void onError(int code, String msg) {
        dismissProgressDialog();
        showAlertDialog("Result of Read Current Time", "error code = " + code + ", msg = " + msg);
      }
    });
  }

  @OnClick(R.id.bthReadDeviceInfo)
  public void bthReadDeviceInfo() {
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

}
