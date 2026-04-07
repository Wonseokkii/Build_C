package com.vivalnk.sdk.demo.repository.device;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.tencent.mmkv.MMKV;
import com.vivalnk.sdk.BuildConfig;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.CommandRequest;
import com.vivalnk.sdk.DataReceiveListener;
import com.vivalnk.sdk.DefaultCallback;
import com.vivalnk.sdk.DeviceStatusListener;
import com.vivalnk.sdk.SampleDataReceiveListener;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.VitalClient.Builder.Key;
import com.vivalnk.sdk.ble.BluetoothConnectListener;
import com.vivalnk.sdk.common.ble.connect.BleConnectOptions;
import com.vivalnk.sdk.common.ble.exception.BleCode;
import com.vivalnk.sdk.common.ble.utils.BluetoothUtils;
import com.vivalnk.sdk.common.eventbus.EventBus;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.demo.repository.database.DatabaseManager;
import com.vivalnk.sdk.demo.repository.database.VitalDevice;
import com.vivalnk.sdk.device.bp5s.BP5SManager;
import com.vivalnk.sdk.device.checkmeo2.CheckmeO2Manager;
import com.vivalnk.sdk.device.vv330.DataStreamMode;
import com.vivalnk.sdk.device.vv330.VV330Manager;
import com.vivalnk.sdk.engineer.test.LogerManager;
import com.vivalnk.sdk.model.BatteryInfo;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.model.SampleData;
import com.vivalnk.sdk.model.common.DataType;
import com.vivalnk.sdk.open.VivaLINKMMKV;
import com.vivalnk.sdk.open.manager.SubjectManager;
import com.vivalnk.sdk.utils.GSON;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.SingleScheduler;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class DeviceManager {

  public static final String TAG = "DeviceManager";
  private Context mContext;
  public static final String projectId = "VivaLNK_SDKTest";
  public static final String subjectId = "testAndroid";

  private BP5SManager mBP5SMananger;
  private SubjectManager subjectManager;
  private Subject<Runnable> subject = PublishSubject.<Runnable>create().toSerialized();
  public VitalClient.Builder builder;

  public boolean allowConnectLastConnectedDevice=true;
  public boolean autoConnectIfDisconnectDevice=true;
  public boolean allowCheckme_02ReadFile=true;
  public float checkme_o2RealtimeDataInterval=4;
  public boolean allowAnimalHrAlgo=false;
  public boolean allowUploadDataToCloud=false;
  public boolean allowForceClockSyncOnceConnected=true;
  private boolean isAllowWriteToFile = true;

  private DataReceiveListener dataReceiveListener;
  private DeviceStatusListener deviceStatusListener;

  // Inner Classes for Events
  public static class VitalSampleData { public Device device; public Map<String, Object> data; }
  public static class BatteryData { public Device device; public BatteryInfo batteryInfo; }
  public static class RssiData { public Device device; public Integer rssi; }
  public static class LeadStatusData { public Device device; public boolean leadOn; }

  private static class SingletonHolder {
    private static final DeviceManager INSTANCE = new DeviceManager();
  }

  private DeviceManager() {}

  public static DeviceManager getInstance() { return SingletonHolder.INSTANCE; }

  public BP5SManager getBP5SMananger() { return mBP5SMananger; }

  public void setDataReceiveListener(DataReceiveListener dataReceiveListener) {
    this.dataReceiveListener = dataReceiveListener;
  }

  public void setDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
    this.deviceStatusListener = deviceStatusListener;
  }

  public void init(Application application) {
    mContext = application.getApplicationContext();
    DatabaseManager.getInstance().init(mContext);

    subject
        .subscribeOn(new SingleScheduler())
        .observeOn(new SingleScheduler())
        .subscribe(new Observer<Runnable>() {
          @Override public void onSubscribe(Disposable d) {}
          @Override public void onNext(Runnable runnable) { runnable.run(); }
          @Override public void onError(Throwable e) { VitalLog.e(e); }
          @Override public void onComplete() {}
        });

    builder = new VitalClient.Builder();
    builder.setConnectResumeListener(myConnectListener);
    builder.allowSaveDataToDB(allowUploadDataToCloud);
    builder.allowUploadDataToCloud(allowUploadDataToCloud);
    builder.setAutoStartSampling(true);
    builder.grantNetworkInfo(true);
    builder.grantLocationInfo(true);
    builder.allowForceClockSyncOnceConnected(allowForceClockSyncOnceConnected);
    builder.putExtra(Key.projectId, MMKV.defaultMMKV().getString(Key.projectId, projectId));
    builder.putExtra(Key.subjectId, MMKV.defaultMMKV().getString(Key.subjectId, subjectId));

    VitalClient.getInstance().allowWriteToFile(isAllowWriteToFile);
    VitalClient.getInstance().init(mContext, builder);
    if (isAllowWriteToFile) VitalClient.getInstance().openLog();
    else VitalClient.getInstance().closeLog();

    subjectManager = new SubjectManager();
    if (allowUploadDataToCloud) {
      subjectManager.register(MMKV.defaultMMKV().getString(Key.projectId,projectId), MMKV.defaultMMKV().getString(Key.subjectId,subjectId), new DefaultCallback());
    }

    mBP5SMananger = new BP5SManager(application);
    try {
      InputStream is = mContext.getAssets().open(BuildConfig.sdkChannel.equals("sdk01") ? "com_vivalnk_sdk_vSDK_demo_android.pem" : "com_vivalnk_sdk_vSDK_demo01a_android.pem");
      mBP5SMananger.init(new DefaultCallback(), is);
    } catch (IOException e) { e.printStackTrace(); }

    LogerManager.getInstance().init();
  }

  public void setDataToCloudEnable(boolean enable) { VitalClient.getInstance().setDataToCloudEnable(enable); }
  public void clearLog() { VitalClient.getInstance().clearLog(); }

  public void connect(final Device device) { connect(device, 6); }

  public void connect(final Device device, int retry) {
    if (null == device || TextUtils.isEmpty(device.getId())) return;
    if (BluetoothUtils.isDeviceConnected(mContext, device.getId())) return;

    BleConnectOptions.Builder optionsBuilder = new BleConnectOptions.Builder()
        .setConnectRetry(retry)
        .setConnectTimeout(15 * 1000)
        .setAutoConnect(autoConnectIfDisconnectDevice);

    // ECG 기기 PIN 설정 (사용자 요청에 따라 260708 사용)
    if (isVivaLNKEcgDevice(device.getName())) {
        Log.i(TAG, "Attempting connection with ECG PIN: 260708");
        optionsBuilder.addParam("pin", "260708");
    }

    VitalClient.getInstance().connect(device, optionsBuilder.build(), myConnectListener);
  }

  public void disconnect(Device device) { VitalClient.getInstance().disconnect(device); }
  public void disconnectAll() { VitalClient.getInstance().disconnectAll(); }
  public void disconnectAll(boolean quietly) { VitalClient.getInstance().disconnectAll(quietly); }
  public boolean isConnected(Device device) { 
    if (device == null || TextUtils.isEmpty(device.getId())) return false;
    return BluetoothUtils.isDeviceConnected(mContext, device.getId()); 
  }

  public int checkBle() { return VitalClient.getInstance().checkBle(); }
  public void enableBle() { VitalClient.getInstance().enableBle(); }
  public void disableBle() { VitalClient.getInstance().disableBle(); }

  public boolean isContinueOTADevice(Device device) {
    if (device == null || TextUtils.isEmpty(device.getName())) return false;
    return "DfuTarg_First_step".equalsIgnoreCase(device.getName())
        || "DfuTarg_Second_step".equalsIgnoreCase(device.getName())
        || "O2 Updater".equalsIgnoreCase(device.getName());
  }

  public void execute(Device device, CommandRequest command, Callback callback) {
    if (null == device || TextUtils.isEmpty(device.getId())) throw new NullPointerException("device is null");
    if (callback == null) callback = new DefaultCallback();
    VitalClient.getInstance().execute(device, command, callback);
  }

  public void runOnUiThread(Runnable runnable) { mMainHandler.post(runnable); }

  private boolean isVivaLNKEcgDevice(String name) {
    if (TextUtils.isEmpty(name)) return false;
    return name.startsWith("ECGRec_") || name.startsWith("VitalScout_");
  }

  private BluetoothConnectListener myConnectListener = new MyConnectListener();
  Handler mMainHandler = new Handler(Looper.getMainLooper());

  private class MyConnectListener implements BluetoothConnectListener {
    @Override public boolean onResume(Device device) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onResume(device))); return false; }
    @Override public void onStartScan(Device device) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onStartScan(device))); }
    @Override public void onStopScan(Device device) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onStopScan(device))); }
    @Override public void onStart(Device device) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onStart(device))); }
    @Override public void onConnecting(Device device) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onConnecting(device))); }
    @Override public void onConnected(Device device) {
      if(device.getModel() == DeviceModel.Checkme_O2){
        CheckmeO2Manager m = new CheckmeO2Manager(device);
        m.setRealtimeDataInterval((long)(checkme_o2RealtimeDataInterval*1000));
        m.setWhetherAllowRegularReadHistoryData(allowCheckme_02ReadFile);
      }
      runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onConnected(device)));
    }
    @Override public void onServiceReady(Device device) {
      if (BuildConfig.sdkChannel.equals("sdk01")) VitalClient.getInstance().registerDataReceiver(device, dataReceiveListener);
      else VitalClient.getInstance().registerDeviceStatusListener(device, deviceStatusListener);
      runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onServiceReady(device)));
    }
    @Override public void onEnableNotify(Device device) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onEnableNotify(device))); }
    @Override public void onDeviceReady(Device device) {
      DatabaseManager.getInstance().getDeviceDAO().insert(new VitalDevice(device));
      runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onDeviceReady(device)));
      DataStreamMode mode = getDataStreamMode(device);
      if (mode != DataStreamMode.None) {
        VV330Manager m = getVV330Manager(device);
        if (m != null) m.switchToMode(mode, new DefaultCallback());
      }
    }
    @Override public void onTryRescanning(Device device) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onTryRescanning(device))); }
    @Override public void onTryReconnect(Device device) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onTryReconnect(device))); }
    @Override public void onRetryConnect(Device device, int t, int c, long to) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onRetryConnect(device))); }
    @Override public void onDisConnecting(Device device, boolean f) { runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onDisConnecting(device, f))); }
    @Override public void onDisconnected(Device device, boolean f) {
      DatabaseManager.getInstance().getDeviceDAO().delete(device.getId());
      runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onDisconnected(device, f)));
      removeVV330Manager(device);
    }
    @Override public void onError(Device device, int code, String msg) {
      runOnUiThread(() -> EventBus.getDefault().post(ConnectEvent.onError(device, code, msg)));
      removeVV330Manager(device);
    }
  }

  Map<String, VV330Manager> vv330ManagerMap = new HashMap<>();
  public VV330Manager getVV330Manager(Device device) {
    if (isVivaLNKEcgDevice(device.getName())) {
      VV330Manager m = vv330ManagerMap.get(device.getId());
      if (m == null) { m = new VV330Manager(device); m.setAnimalHrAlgoEnable(allowAnimalHrAlgo); vv330ManagerMap.put(device.getId(), m); }
      return m;
    }
    return null;
  }
  public void removeVV330Manager(Device device) { vv330ManagerMap.remove(device.getId()); }
  public static void putDataStreamMode(Device device, DataStreamMode mode) { VivaLINKMMKV.defaultMMKV().putInt(device.getId() + "_demo_dataStreamMode", mode.ordinal()); }
  public static DataStreamMode getDataStreamMode(Device device) { return DataStreamMode.forNumber(VivaLINKMMKV.defaultMMKV().getInt(device.getId() + "_demo_dataStreamMode", DataStreamMode.None.getNumber())); }

  public void allowWriteToFile(boolean isAllowWriteToFile){ this.isAllowWriteToFile = isAllowWriteToFile; }
}
