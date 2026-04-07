package com.vivalnk.sdk.demo.vital.ui.device.checkme_o2;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import butterknife.BindView;
import butterknife.OnClick;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.vivalnk.sdk.BuildConfig;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.CommandRequest;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.command.checkmeo2.CheckmeO2Constants;
import com.vivalnk.sdk.command.checkmeo2.base.CommandType;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.FileUtils;
import com.vivalnk.sdk.common.utils.PermissionHelper;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.base.i18n.ErrorMessageHandler;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.vital.ui.BP5SActivity;
import com.vivalnk.sdk.demo.vital.ui.BleSigGlucoseActivity;
import com.vivalnk.sdk.demo.vital.ui.DeviceMenuActivity;
import com.vivalnk.sdk.demo.vital.ui.DeviceMenuC208SActivity;
import com.vivalnk.sdk.demo.vital.ui.OTAActivity;
import com.vivalnk.sdk.demo.vital.ui.device.aoj.DeviceMenuAOJ_BPActivity;
import com.vivalnk.sdk.demo.vital.ui.device.aoj.DeviceMenuAOJ_O2Activity;
import com.vivalnk.sdk.demo.vital.ui.device.aoj.DeviceMenuAOJ_TempActivity;
import com.vivalnk.sdk.demo.vital.ui.device.o2.O2PulseWaveDrawActivity;
import com.vivalnk.sdk.engineer.test.FileManager;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.model.O2File;
import com.vivalnk.sdk.utils.DateFormat;
import com.vivalnk.sdk.utils.GSON;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CheckmeO2Activity extends ConnectedActivity {

  private static final String TAG = "CheckmeO2Activity";

  @BindView(R.id.tvPrinter)
  TextView tvPrinter;
  @BindView(R.id.btnOTA)
  TextView btnOTA;
  @BindView(R.id.btnGetHistoryData)
  AppCompatButton btnGetHistoryData;
  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_device_checkmeo2);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (mDevice.getName().startsWith("O2 ")) {
      btnOTA.setVisibility(View.VISIBLE);
    } else {
      btnOTA.setVisibility(View.GONE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
//    getMenuInflater().inflate(R.menu.settings, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.device_settings) {

      Bundle extras = new Bundle();
      extras.putSerializable("device", mDevice);
      navTo(this, extras, CheckmeO2SettingsActivity.class);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onSampleData(DeviceManager.VitalSampleData sampleData) {

    if (!sampleData.device.equals(mDevice)) {
      return;
    }

    Map<String, Object> data = sampleData.data;
    final int spo2 = (int) data.get("spo2");
    final int pr = (int) data.get("pr");
    final Float pi = (Float) data.get("pi");
    final int battery = (int) data.get("battery");
    final int[] waveform = (int[]) data.get("waveform");
    final int chargerStatus = (int) data.get("chargerStatus");

    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm.ss");

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        updateView(spo2, pr, pi, battery, chargerStatus);
      }
    });

    final String date = DateFormatUtils.format(new Date(), DateFormat.sPattern);
    String log = date
            + (sampleData.data == null ? "" : (", " + GSON.toJson(sampleData.data)))
            + "\n";
    try {
      //保存原始数据
      String filePath = FileManager.getFileDataPath(mDevice.getName(), "data.txt");
      Context context = VitalClient.getInstance().getAppContext();
      FileUtils.writeFile(filePath, log);
    } catch (Throwable throwable) {

    }
  }

  @OnClick(R.id.btnSetSleepTime)
  void clickBtnSetSleepTime(){
    //设置超时自动关机时间
    View view = LayoutInflater.from(this).inflate(R.layout.oxithr_id_layout, null);
    final EditText sleetTimeEdt = (EditText) view.findViewById(R.id.edtOxithr);
    sleetTimeEdt.setHint("please input num such as 30");
    new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.str_set_parameters_sleepTime))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setView(view)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                int value = 30;
                try{
                  value = Integer.parseInt(sleetTimeEdt.getText().toString());
                  VitalLog.d("SleepTime="+value);
                } catch (Exception e){

                }
                CommandRequest request = new CommandRequest.Builder()
                        .setType(CommandType.setParameters)
                        .addParam(CheckmeO2Constants.ParamsKeys.SetSleepTime, value)
                        .addParam(CheckmeO2Constants.ParamsKeys.SetTIME, System.currentTimeMillis())
                        .build();
                execute(request, new Callback() {
                  @Override
                  public void onComplete(Map<String, Object> data) {
                      if(data!=null){
                        for (Map.Entry<String,Object> entry:data.entrySet()){
                          VitalLog.d("SleepTime result="+entry.getKey()+":"+entry.getValue());
                        }
                      }else{
                        VitalLog.d("SleepTime result=null");
                      }
                  }

                  @Override
                  public void onError(int code, String msg) {
                    VitalLog.d("SleepTime result error:code="+code+" msg="+msg);
                  }
                });
              }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
  }



  @OnClick(R.id.btnDisconnect)
  void clickBtnDisconnect() {
    showProgressDialog("Disconnecting...");
    DeviceManager.getInstance().disconnect(mDevice);
  }

  @OnClick(R.id.btnGetRTWaveFormData)
  public void clickGetRTWaveFormData() {
   /* CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.getRealTimeWaveform)
        .build();
    execute(request, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        String filePath = FileManager.getFileDataPath(mDevice.getSn(), "getRealTimeWaveform.txt");
        Context context = VitalClient.getInstance().getAppContext();
        if(!PermissionHelper.hasPermission(context, permission.WRITE_EXTERNAL_STORAGE)) {
          showToast("we have no permission to write external file");
          return;
        }
        FileUtils.writeFile(filePath, GSON.toJson(data));
      }
    });*/
  }

  @OnClick(R.id.btnGetHistoryData)
  public void clickGetHistoryData() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.getHistoryData)
        .build();
    execute(request, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        if (data == null) {
          showAlertDialog("Get History Data", "Empty");
          return;
        }
        List<O2File> fileList = (List<O2File>) data.get("data");
        Set<String> set = new HashSet<>();
        for (int i = 0; i < fileList.size(); i++) {
          set.add(fileList.get(i).fileName);
        }
        showAlertDialog("Get History Data", GSON.toJson(set));
        String filePath = FileManager.getFileDataPath(mDevice.getSn(), "getHistoryData.txt");
        Context context = VitalClient.getInstance().getAppContext();
//        if(!PermissionHelper.hasPermission(context, permission.WRITE_EXTERNAL_STORAGE)) {
//          showToast("we have no permission to write external file");
//          return;
//        }
        FileUtils.writeFile(filePath, GSON.toJson(fileList));
      }
    });
  }

  @OnClick(R.id.btnGetDeviceInfo)
  public void getDeviceInfo() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.getDeviceInfo)
        .build();
    execute(request, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        showAlertDialog("Get Device Info", GSON.toJson(data));
      }
    });
  }

  @OnClick(R.id.btnPingDevice)
  public void pingDevice() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.pingDevice)
        .build();
    execute(request, new Callback() { });
  }

  @OnClick(R.id.btnSetParameters)
  public void setParameters() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.setParameters)
        .addParam(CheckmeO2Constants.ParamsKeys.SetTIME, System.currentTimeMillis())
        .build();
    execute(request);
  }

  @OnClick(R.id.btnSetParametersOxiThr)
  public void setParametersOxiThr() {
    //震动阈值
    View view = LayoutInflater.from(this).inflate(R.layout.oxithr_id_layout, null);
    final EditText edtOxithr = (EditText) view.findViewById(R.id.edtOxithr);
    new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.str_set_parameters_oxiThr))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setView(view)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                int value = 90;
                try{
                  value = Integer.parseInt(edtOxithr.getText().toString());
                } catch (Exception e){

                }
                CommandRequest request = new CommandRequest.Builder()
                        .setType(CommandType.setParameters)
                        .addParam(CheckmeO2Constants.ParamsKeys.SetOxiThr, value)
                        .addParam(CheckmeO2Constants.ParamsKeys.SetTIME, System.currentTimeMillis())
                        .build();
                execute(request);

              }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();

  }

  @OnClick(R.id.btnSetParametersMotor)
  public void setParametersMotor() {
    View view = LayoutInflater.from(this).inflate(R.layout.motor_id_layout, null);
    final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
    final RadioButton radioButton0 = (RadioButton) view.findViewById(R.id.radioButton0);
    final RadioButton radioButton20 = (RadioButton) view.findViewById(R.id.radioButton20);
    final RadioButton radioButton40 = (RadioButton) view.findViewById(R.id.radioButton40);
    final RadioButton radioButton80 = (RadioButton) view.findViewById(R.id.radioButton80);
    final RadioButton radioButton100 = (RadioButton) view.findViewById(R.id.radioButton100);
    new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.str_set_parameters_motor))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setView(view)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                int value = 0;
                if (radioGroup.getCheckedRadioButtonId() == radioButton0.getId()) {
                  value = 0;
                } else if (radioGroup.getCheckedRadioButtonId() == radioButton20.getId()) {
                  value = 20;
                } else if (radioGroup.getCheckedRadioButtonId() == radioButton40.getId()) {
                  value = 40;
                } else if (radioGroup.getCheckedRadioButtonId() == radioButton80.getId()) {
                  value = 80;
                } else if (radioGroup.getCheckedRadioButtonId() == radioButton100.getId()) {
                  value = 100;
                }
                VitalLog.e("value="+ value);
                CommandRequest request = new CommandRequest.Builder()
                      .setType(CommandType.setParameters)
                      .addParam(CheckmeO2Constants.ParamsKeys.SetMotor, value)
                      .addParam(CheckmeO2Constants.ParamsKeys.SetTIME, System.currentTimeMillis())
                      .build();
                execute(request);
              }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();

  }

  @OnClick(R.id.btnGetRTData)
  public void getRTData() {
    /*CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.getRealTimeData)
        .build();
    execute(request, new Callback() {
      @Override
      public void onComplete(Map<String, Object> data) {
        final int spo2 = (int) data.get("spo2");
        final int pr = (int) data.get("pr");
        final Float pi = (Float) data.get("pi");
        final int battery = (int) data.get("battery");
        final int[] waveform = (int[]) data.get("waveform");
        final int chargerStatus = (int) data.get("chargerStatus");
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            updateView(spo2, pr, pi, battery, chargerStatus);
          }
        });
      }
    });*/
  }

  @OnClick(R.id.btnFactoryReset)
  public void factoryReset() {
    CommandRequest request = new CommandRequest.Builder()
        .setType(CommandType.factoryReset)
        .build();
    execute(request);
  }

  @OnClick(R.id.btnDrawWave)
  public void drawWave() {
    Bundle extras = new Bundle();
    extras.putSerializable("device", mDevice);
    navTo(this, extras, O2PulseWaveDrawActivity.class);
  }

  private static final int OTA_RET_CODE = 2019;
  private static final int ACTIVITY_CHOOSE_FILE = 3;
  @OnClick(R.id.btnOTA)
  public void clickOTA() {
    openFileSelector();
  }

  private void openFileSelector() {
    Intent intent = new Intent(this, FilePickerActivity.class);
    intent.putExtra(FilePickerActivity.ARG_FILTER, Pattern.compile("CheckO2_app_vivalnk_(\\d+\\.\\d\\.\\d(\\.\\d+)*)*\\.zip"));
    startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
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

  private void updateView(int spo2, int pr, Float pi, int battery, int chargerStatus) {
    String text = new StringBuffer()
        .append("SpO2 = " + spo2).append("\n")
        .append("PI = " + pi).append("\n")
        .append("Pulse Rate = " + pr).append("\n")
        .append("Battery = " + battery).append("\n")
        .append("chargerStatus = " + getChargingStateDescription(chargerStatus)).append("\n")
        .toString();
    tvPrinter.setText(text);
  }

  private String getChargingStateDescription(int state) {
    String ret = "";
    switch (state) {
      case 0:
        ret = "No Charging";
        break;
      case 1:
        ret = "Charging";
        break;
      case 2:
        ret = "Charging Complete";
        break;
      case 3:
        ret = "Low Battery";
        break;
    }
    return ret;
  }

}
