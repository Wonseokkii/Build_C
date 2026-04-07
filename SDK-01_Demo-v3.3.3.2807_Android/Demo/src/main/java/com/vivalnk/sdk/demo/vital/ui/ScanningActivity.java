package com.vivalnk.sdk.demo.vital.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import com.google.android.material.navigation.NavigationView;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.base.WrapperResultEvent;
import com.vivalnk.sdk.ble.BluetoothScanListener;
import com.vivalnk.sdk.common.ble.scan.ScanOptions;
import com.vivalnk.sdk.common.eventbus.EventBus;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.EventBusHelper;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.base.i18n.ErrorMessageHandler;
import com.vivalnk.sdk.demo.repository.device.ConnectEvent;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.repository.device.ScanEvent;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.vital.base.BaseDeviceActivity;
import com.vivalnk.sdk.demo.vital.ui.adapter.ScanListAdapter;
import com.vivalnk.sdk.demo.vital.ui.adapter.ScanListAdapter.StatusDevice;
import com.vivalnk.sdk.demo.vital.ui.device.aoj.DeviceMenuAOJ_BPActivity;
import com.vivalnk.sdk.demo.vital.ui.device.aoj.DeviceMenuAOJ_O2Activity;
import com.vivalnk.sdk.demo.vital.ui.device.aoj.DeviceMenuAOJ_TempActivity;
import com.vivalnk.sdk.demo.vital.ui.device.bodyscale.DeviceMenuHS2SActivity;
import com.vivalnk.sdk.demo.vital.ui.device.bp.DeviceMenuABPMActivity;
import com.vivalnk.sdk.demo.vital.ui.device.checkme_o2.CheckmeO2Activity;
import com.vivalnk.sdk.demo.vital.ui.device.o2.O2MightySatActivity;
import com.vivalnk.sdk.demo.vital.v200.activity.MainActivity;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.open.evnet.TimezoneSyncEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class ScanningActivity extends BaseDeviceActivity implements
    NavigationView.OnNavigationItemSelectedListener {

  private Handler mHandler;
  private String tag="devicetag";

  public static Intent newIntent(Context context) {
    return new Intent(context, ScanningActivity.class);
  }

  private LinkedHashSet<StatusDevice> deviceLinkedHashSet;
  private List<StatusDevice> deviceArrayList;
  private ScanListAdapter recycleAdapter;
  private Boolean mIsScanning = false;

  @BindView(R.id.rvList)
  RecyclerView rvScanList;

  private BluetoothScanListener scanListener = new BluetoothScanListener() {
    @Override
    public void onStart() {
      runOnUiThread(() -> EventBus.getDefault().post(ScanEvent.onStart()));
    }

    @Override
    public void onDeviceFound(Device device) {
    }

    @Override
    public void onStop() {
      runOnUiThread(() -> EventBus.getDefault().post(ScanEvent.onStop()));
    }

    @Override
    public void onError(int code, String msg) {
      runOnUiThread(() -> EventBus.getDefault().post(ScanEvent.onError(code, msg)));
      Toast.makeText(ScanningActivity.this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
  };

  private Comparator comparator = new Comparator<StatusDevice>() {
    @Override
    public int compare(StatusDevice o1, StatusDevice o2) {
      int rssi1 = o1.device.getRssi();
      int rssi2 = o2.device.getRssi();
      if (o1.connect) rssi1 = Math.abs(rssi1);
      if (o2.connect) rssi2 = Math.abs(rssi2);
      if (o1.connect && o2.connect) return rssi1 - rssi2;
      else return rssi2 - rssi1;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    initView();
    EventBusHelper.getDefault().register(this);
    try {
      if(DeviceManager.getInstance().allowConnectLastConnectedDevice){
        List<Device> devices=VitalClient.getInstance().getWatchDevices();
        if(devices==null || devices.size()==0){
          startScan();
        }else{
          VitalClient.getInstance().connectLastDevice();
        }
      }else{
        startScan();
      }
    } catch (Exception e) {
      startActivity(new Intent(this, WelcomeActivity.class));
      this.finish();
    }
    mHandler = new Handler(Looper.getMainLooper());
  }

  private void startScan() {
    if (!checkBLE()) return;
    if (mIsScanning) return;
    ScanOptions options = new ScanOptions.Builder()
        .setTimeout(30 * 1000)
        .setEnableLog(true)
        .build();
    VitalClient.getInstance().startScan(options, scanListener);
  }

  private void updateList() {
    if (deviceArrayList == null) deviceArrayList = new ArrayList<>();
    deviceArrayList.clear();
    deviceArrayList.addAll(new ArrayList<>(deviceLinkedHashSet));
    Collections.sort(deviceArrayList, comparator);
    recycleAdapter.notifyDataSetChanged();
  }

  private void stopScan() {
    if (mIsScanning == false) return;
    mIsScanning = false;
    setScanText(R.string.start_scan);
    VitalClient.getInstance().stopScan(scanListener);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (recycleAdapter != null) updateList();
  }

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_main);
  }

  @Override
  protected void onDestroy() {
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    stopScan();
    super.onDestroy();
    EventBusHelper.getDefault().unregister(this);
  }

  @Override
  protected void onConnectChanged(ConnectEvent connectEvent) {
    super.onConnectChanged(connectEvent);
    VitalLog.e(tag+" onConnectChanged= " + connectEvent.event);

    if (ConnectEvent.ON_DEVICE_READY.equalsIgnoreCase(connectEvent.event)) {
      updateContent(true, connectEvent.device);
      if (null != mTimezoneSwitchDialog && mTimezoneSwitchDialog.isShowing()) return;
      stopScan();
      navToDeviceActivity(ScanningActivity.this, connectEvent.device);
    } else if (ConnectEvent.ON_DISCONNECTED.equalsIgnoreCase(connectEvent.event)) {
      updateContent(false, connectEvent.device);
      showToast(ErrorMessageHandler.getInstance().getDisconnectedMeesage(connectEvent.device, connectEvent.isForce));
    } else if (ConnectEvent.ON_ERROR.equalsIgnoreCase(connectEvent.event)) {
      updateContent(false, connectEvent.device);
      showToast(ErrorMessageHandler.getInstance().getConnectErrorMeesage(connectEvent.device, connectEvent.code, connectEvent.msg));
    }
  }

  private void updateContent(boolean connect, Device device) {
    StatusDevice target = new StatusDevice(device, connect);
    deviceLinkedHashSet.remove(target);
    deviceLinkedHashSet.add(target);
    updateList();
    recycleAdapter.updateConnectStatus(connect, device);
  }

  private void initView() {
    rvScanList.setLayoutManager(new LinearLayoutManager(this));
    rvScanList.setHasFixedSize(true);
    deviceLinkedHashSet = new LinkedHashSet<>();
    deviceArrayList = new ArrayList<>();
    recycleAdapter = new ScanListAdapter(deviceArrayList,
            (itemView, position, device) -> {
              if (checkBLE() == false) return;
              if (continueOTAProgressIfNeed(device)) return;
              if(deviceArrayList.get(position).connect != DeviceManager.getInstance().isConnected(device)) return;
              
              if (DeviceManager.getInstance().isConnected(device)) {
                stopScan();
                navToDeviceActivity(ScanningActivity.this, device);
              } else {
                // ECG 센서 페어링 시 패스코드 이슈 대응 로그
                Log.i(TAG, "Connecting to: " + device.getName() + " [" + device.getId() + "]");
                DeviceManager.getInstance().connect(device);
              }
            });
    rvScanList.setAdapter(recycleAdapter);

    List<StatusDevice> devices = new ArrayList<>();
    try {
      for (Device device : VitalClient.getInstance().getConnectedDeviceList()) {
        devices.add(new StatusDevice(device, true));
      }
    } catch (Exception e) {
      startActivity(new Intent(this, ConfigActivity.class));
      this.finish();
    }
    deviceLinkedHashSet.addAll(devices);
    updateList();

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();
    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
  }

  public static void navToDeviceActivity(Context context, Device device) {
    Bundle extras = new Bundle();
    extras.putSerializable("device", device);
    DeviceModel model = device.getModel();
    
    Log.d("ScanningActivity", "Navigating to Activity for model: " + model);

    if (model == DeviceModel.Checkme_O2) {
      navTo(context, extras, CheckmeO2Activity.class);
    } else if(model == DeviceModel.BP5S) {
      navTo(context, extras, BP5SActivity.class);
    } else if(model == DeviceModel.ABPM) {
      navTo(context, extras, DeviceMenuABPMActivity.class);
    } else if(model == DeviceModel.AOJ_O2) {
      navTo(context, extras, DeviceMenuAOJ_O2Activity.class);
    } else if(model == DeviceModel.AOJ_BP) {
      navTo(context, extras, DeviceMenuAOJ_BPActivity.class);
    } else if(model == DeviceModel.AOJ_TEMP) {
      navTo(context, extras, DeviceMenuAOJ_TempActivity.class);
    } else if (model == DeviceModel.HS2S) {
      navTo(context, extras, DeviceMenuHS2SActivity.class);
    } else if (model.name().contains("ECG") || model == DeviceModel.VV330) {
      // ECG 기기인 경우 DeviceMenuActivity(통합 메뉴) 또는 전용 ECG 액티비티로 유도
      navTo(context, extras, DeviceMenuActivity.class);
    } else {
      navTo(context, extras, DeviceMenuActivity.class);
    }
  }

  private static final int OTA_RET_CODE = 2019;
  private static final int ACTIVITY_CHOOSE_FILE = 3;
  private Device currentContinueOTADevice;
  private boolean continueOTAProgressIfNeed(Device device) {
    if (DeviceManager.getInstance().isContinueOTADevice(device)) {
      currentContinueOTADevice = device;
      OTAActivity.openFileSelector(this, ACTIVITY_CHOOSE_FILE);
      return true;
    }
    return false;
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == ACTIVITY_CHOOSE_FILE) {
      if (resultCode == RESULT_OK && data != null && currentContinueOTADevice != null) {
        String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
        startActivityForResult(OTAActivity.newIntent(this, currentContinueOTADevice, filePath), OTA_RET_CODE);
      }
    }
  }

  @UiThread
  private void onDeviceFound(Device device) {
    deviceLinkedHashSet.add(new StatusDevice(device));
    updateList();
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
    else super.onBackPressed();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_scan) {
      if (mIsScanning) stopScan();
      else startScan();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setScanText(@StringRes int resID) {
    if (toolbar.getMenu() != null && toolbar.getMenu().size() > 0) {
      MenuItem item = toolbar.getMenu().getItem(0);
      item.setTitle(resID);
    }
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.nav_connected_device) {
      if (checkBLE()) navTo(DeviceConnectedListActivity.class);
    } else if (id == R.id.nav_check) {
      navTo(RuntimeCheckActivity.class);
    } else if(id == R.id.nav_v200) {
      startActivity(new Intent(this, MainActivity.class));
    } else if(id == R.id.nav_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
    }
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override protected void onLocationTurnOff() { super.onLocationTurnOff(); stopScan(); }
  @Override protected void onBluetoothTurnOff() { super.onBluetoothTurnOff(); stopScan(); }

  private AlertDialog mTimezoneSwitchDialog;
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEventECGTimezone(TimezoneSyncEvent event) {
    if (event.type == TimezoneSyncEvent.Type.SYNC_START) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Tips")
            .setMessage("Configuring to your time zone...")
            .setPositiveButton(R.string.ok, null);
      mTimezoneSwitchDialog = builder.show();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onScanEvent(ScanEvent event) {
    if (ScanEvent.ON_STOP.equalsIgnoreCase(event.event) || ScanEvent.ON_ERROR.equalsIgnoreCase(event.event)) {
      mIsScanning = false;
      setScanText(R.string.start_scan);
    }else if(ScanEvent.ON_START.equalsIgnoreCase(event.event)){
      Iterator<StatusDevice> it = deviceLinkedHashSet.iterator();
      while (it.hasNext()) {
        if (!DeviceManager.getInstance().isConnected(it.next().device)) it.remove();
      }
      updateList();
      mIsScanning = true;
      setScanText(R.string.stop_scan);
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onWrapperResultEvent(WrapperResultEvent event){
    onDeviceFound(event.device);
  }
}
