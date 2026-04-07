package com.vivalnk.sdk.demo.vital.v200.activity;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.vivalnk.model.ChargerInfo;
import com.vivalnk.sdk.common.utils.PermissionHelper;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.demo.base.app.BaseApplication;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.vital.v200.ble.BleConstants;
import com.vivalnk.sdk.demo.vital.v200.ble.BleData;
import com.vivalnk.sdk.demo.vital.v200.ble.BleManager;
import com.vivalnk.sdk.demo.vital.v200.utils.GPSUtils;
import com.vivalnk.sdk.utils.GSON;
import com.vivalnk.vdireader.VDIType.CHECKBLE_STATUS_TYPE;
import com.vivalnk.vdireader.VDIType.TEMPERATURE_STATUS;
import com.vivalnk.vdireaderimpl.CommonFunction;
import com.vivalnk.vdireaderimpl.VDIBleService;
import com.vivalnk.vdiutility.viLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "###MainActivity";
    private static final String LOCAL_PATH = Environment.getExternalStorageDirectory()
            .getPath() + "/vivalnk/v200";

    private TextView mTvPairingRSSI;
    private TextView mTvResult;

    private AlertDialog mLogDialog;
    private ArrayList<String> mListLog;
    private ArrayAdapter<String> mLogAdapter;

    private File outputFile;

    private boolean isBackground = false;
    private BleManager mBleManager;
    private int mPairingRSSI = -110;

    private boolean isTemperatureUpdate = false;

    private LoadDataHandler mLoadDataHandler;

    private Timer mCheckDataReceiveTimer;
    private TimerTask mCheckDataReceiveTimerTask;

    private int mForegroundReceiveCounts = 0;
    private int mBackgroundReceiveCounts = 0;
    private long mLatestTemperatureUpdateTime;
    private int mForegroundLostSamples = 0;
    private int mBackGroundLostSamples = 0;
    private int mForeGroundSamples = 0;
    private int mBackGroundSamples = 0;

    private long mBackgroundStartTime;
    private String mLatestTemperatureString = "";

    private VDIBleService mService;
    private Intent serviceIntent;
    private boolean mServiceBound = false;

    private String mPairedDevice = "";
    private int mPairedDeviceNumber = 0;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((VDIBleService.LocalBinder) service).getService();
            mService.background();
            mService.setNotification(createNotification());

            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    private Notification createNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("VV200_Demo")
                .setContentText("Background running")
                .setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel("com.vivalnk.fever.service", "VV200 Service", NotificationManager.IMPORTANCE_DEFAULT));
            builder = builder.setChannelId("com.vivalnk.fever.service");
        }

        return builder.build();
    }

    protected void onStart() {
        super.onStart();
        if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                serviceIntent = new Intent(this, VDIBleService.class);
                startService(serviceIntent);
                bindService(serviceIntent, mConnection, 0);
            }

        }
    }

    private void handleCacheResult(String cacheResult) {
        String[] result = cacheResult.split(";");
        mListLog.add(result[1]);
        mLogAdapter.notifyDataSetChanged();
        outPrintLog(result[0], result[1]);
    }

    private static class LoadDataHandler extends Handler {
        private WeakReference<MainActivity> mTarget;

        public LoadDataHandler(MainActivity activity) {
            mTarget = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mTarget.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            switch (msg.what) {
                case BleConstants.MESSAGE_CACHE_REAL_UPDATED:
                case BleConstants.MESSAGE_CACHE_HISTORY_UPDATED:
                    activity.handleCacheResult((String) msg.obj);
                    break;
                case BleConstants.MESSAGE_TEMPERATURE_UPDATE:
                    String result = activity.getDisplayTemperatureResultString((BleData) msg.obj);
                    viLog.i(TAG, "MESSAGE_TEMPERATURE_UPDATE");
                    activity.updateResultText();
                    activity.mLogAdapter.notifyDataSetChanged();
                    if (activity.mPairedDeviceNumber == 1) {
                        activity.mTvResult.setText(result);
                    }
                    break;
                case BleConstants.MESSAGE_CHARGER_INFO_UPDATED:
                    ChargerInfo chargerInfo = (ChargerInfo) msg.obj;
                    if (chargerInfo.toString().contains("low")) {
                        Toast.makeText(activity, "charger low battery!", Toast.LENGTH_LONG).show();
                    }
                    activity.mListLog.add(activity.sdf.format(new Date()) + ", " + chargerInfo.toString() + "\n");
                    activity.mLogAdapter.notifyDataSetChanged();
                    activity.outPrintLog(chargerInfo.getSN(), activity.sdf.format(new Date()) + ", " +
                            chargerInfo.toString() + "\n");
                    activity.updateResultText();
                    break;
                case BleConstants.MESSAGE_TEMPERATURE_MISSED:
                    activity.onTemperatureMissed((String) msg.obj);
                    activity.updateResultText();
                    break;
                case BleConstants.MESSAGE_DEVIE_LOST:
                    activity.onDeviceLost((String) msg.obj);
                    activity.updateResultText();
                    break;
                case BleConstants.MESSAGE_TEMPERATURE_ABNORAML:
                    Toast.makeText(activity, "abnormal low temperature!", Toast.LENGTH_LONG).show();
                    activity.mListLog.add((String) msg.obj);
                    activity.mLogAdapter.notifyDataSetChanged();
                    activity.updateResultText();
                    break;
                case AppConstants.MESSAGE_SCREEN_ON:
                    viLog.d(TAG, "Message    :MESSAGE_SCREEN_ON()");
                    if (activity.mPairedDeviceNumber != 1) {
                        return;
                    }
                    activity.isBackground = false;
                    if (activity.isTemperatureUpdate) {
                        if (activity.mBackgroundStartTime == 0) {
                            activity.mBackgroundStartTime = System.currentTimeMillis();
                            return;
                        }
                        long currentTimeM = System.currentTimeMillis();
                        long samples = (currentTimeM - activity.mBackgroundStartTime) / AppConstants.DATA_RECEIVE_CHECK_PERIOD;
                        activity.mBackGroundSamples += samples;
                        if ((currentTimeM - activity.mLatestTemperatureUpdateTime) > AppConstants.DATA_RECEIVE_CHECK_DELAY) {
                            long lostSamples = (currentTimeM - activity.mLatestTemperatureUpdateTime) / AppConstants.DATA_RECEIVE_CHECK_PERIOD;
                            activity.mBackGroundLostSamples += lostSamples;
                            activity.mLatestTemperatureUpdateTime = System.currentTimeMillis();
                        }
                        activity.updateUI();
                        activity.startDataReceiveCheckTimer();
                    }
                    break;
                case AppConstants.MESSAGE_SCREEN_OFF:
                    viLog.d(TAG, "Message    :MESSAGE_SCREEN_OFF()");
                    if (activity.mPairedDeviceNumber != 1) {
                        return;
                    }
                    activity.isBackground = true;
                    if (activity.isTemperatureUpdate) {
                        activity.mBackgroundStartTime = System.currentTimeMillis();
                        activity.stopDataReceiveCheckTimer();
                    }
                    break;
                case AppConstants.MESSAGE_UPDATE_UI:
                    activity.updateUI();
                    break;
                case BleConstants.MESSAGE_PHONE_BLUETOOTH_OFF:
                    Toast.makeText(activity, "phone bluetooth off", Toast.LENGTH_LONG).show();
                    break;
                case BleConstants.MESSAGE_PHONE_LOCATION_OFF:
                    Toast.makeText(activity, "phone location off", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }

    public void updateResultText() {
        mPairedDeviceNumber = mBleManager.getPDListLength();
        if (mPairedDeviceNumber == 1) {
            mPairedDevice = mBleManager.iteratePDList().get(0);
            mTvResult.setText("deviceId " + mPairedDevice);
            startDataReceiveCheckTimer();
        } else {
            mTvResult.setText("Paired device: " + GSON.toJson(mBleManager.iteratePDList()));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v200_main);

        mLoadDataHandler = new LoadDataHandler(this);
        mTvResult = (TextView) findViewById(R.id.tvResult);
        mBleManager = BleManager.getInstance(BaseApplication.getApplication());
        checkPermission();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        findViewById(R.id.tvHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVersion();
            }
        });
        findViewById(R.id.tvPair).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
                isTemperatureUpdate = false;
                mPairedDeviceNumber = 0;
                mLatestTemperatureString = "";
                if (mPairedDeviceNumber == 1) {
                    stopDataReceiveCheckTimer();
                }
                mBleManager.getBleReader().stopTemperatureUpdate();
                mBleManager.getBleReader().purgePDList();
                mBleManager.getBleReader().setPairingRssi(mPairingRSSI);

                startActivityForResult(new Intent(MainActivity.this, DeviceListActivity.class),
                        AppConstants.REQUEST_CODE_OPEN_SCAN_DEVICE_UI);
            }
        });
        mPairedDeviceNumber = mBleManager.getPDListLength();
        if(mBleManager.getPDListLength() > 0){
            isTemperatureUpdate = true;
            mBleManager.getBleReader().startTemperatureUpdate();
            mLatestTemperatureUpdateTime = System.currentTimeMillis();

            if (mPairedDeviceNumber == 1) {
                mPairedDevice = mBleManager.iteratePDList().get(0);
                mTvResult.setText("deviceId " + mPairedDevice);
                startDataReceiveCheckTimer();
            } else {
                mTvResult.setText("Paired device: " + GSON.toJson(mBleManager.iteratePDList()));
            }

            mListLog.add(mBleManager.iteratePDList().get(0) + "  added to collect data:");
            mLogAdapter.notifyDataSetChanged();
        }
        mBleManager.setHandler(mLoadDataHandler);
    }

    protected Toolbar toolbar;

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.finish();
                }
            });
        }
    }


    private void initView() {
        initPairingSeekBarFeature();
        initLogFileSave();
        initLogButton();
        registerScreenActionReceiver();
        initToolbar();
    }

    private void init() {

        if (!GPSUtils.isGPSEnable(getApplicationContext())) {
            Toast.makeText(MainActivity.this, "location is not enabled! ", Toast.LENGTH_LONG).show();
        }
        CHECKBLE_STATUS_TYPE checkResult = mBleManager.getBleReader().checkBle();
        if (checkResult == CHECKBLE_STATUS_TYPE.SYSTEM_BLE_NOT_ENABLED) {
            Toast.makeText(MainActivity.this, "BLE is not enabled now, please enable BLE in the setting page", Toast.LENGTH_LONG).show();
        } else if (checkResult == CHECKBLE_STATUS_TYPE.SYSTEM_LOCATION_NOT_ENABLED) {
            if (Build.VERSION.SDK_INT < VERSION_CODES.S) {
                Toast.makeText(MainActivity.this, "You need to allow location update permission for this app to enable BLE scanning", Toast.LENGTH_LONG).show();
            }
        } else if (checkResult == CHECKBLE_STATUS_TYPE.SYSTEM_NOT_SUPPORT_BLE) {
            Toast.makeText(MainActivity.this, "BLE is not available", Toast.LENGTH_LONG).show();
        }

    }


    //request location and write permissions at rum time
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
            init();
            initView();
            return;
        } else {
            List<String> mListPermissions = new ArrayList<String>();
            mListPermissions.add(permission.MANAGE_EXTERNAL_STORAGE);
            mListPermissions.add(permission.BLUETOOTH_SCAN);
            mListPermissions.add(permission.BLUETOOTH_CONNECT);
//			mListPermissions.add(permission.POST_NOTIFICATIONS);
            if (Build.VERSION.SDK_INT < VERSION_CODES.S) {
                mListPermissions.add(permission.ACCESS_COARSE_LOCATION);
                if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
                    mListPermissions.add(permission.ACCESS_FINE_LOCATION);
                }
            }
            boolean needRequestPermission = false;
            for (int i = 0; i < mListPermissions.size(); i++) {
                if (checkSelfPermission(mListPermissions.get(i)) != PackageManager.PERMISSION_GRANTED) {
                    needRequestPermission = true;
                }
            }

            if (mListPermissions != null && mListPermissions.size() > 0 && needRequestPermission) {
                realCheckPermission(mListPermissions);

            } else {
                init();
                initView();
            }
        }
    }


    //Clicking twice the back button to exit activity
    @Override
    public void onBackPressed() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm to exit app？")
                .setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void registerScreenActionReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);

        registerReceiver(mReceiver, filter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            // Do your action here
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                isBackground = true;
                mLoadDataHandler.sendEmptyMessage(AppConstants.MESSAGE_SCREEN_OFF);
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                isBackground = false;
                mLoadDataHandler.sendEmptyMessage(AppConstants.MESSAGE_SCREEN_ON);
            }
        }

    };

    private void initPairingSeekBarFeature() {
        SeekBar seekBarPairingRSSI = (SeekBar) findViewById(R.id.sbPairRssi);
        mTvPairingRSSI = (TextView) findViewById(R.id.tvPairRssi);
        mTvPairingRSSI.setText("Rssi: " + mPairingRSSI);
        seekBarPairingRSSI.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float nCurrent = seekBar.getProgress();
                float nMax = seekBar.getMax();
                mPairingRSSI = (int) (0 - 110 * (nMax - nCurrent) / nMax);
                mTvPairingRSSI.setText("Rssi: " + mPairingRSSI);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void initLogFileSave() {

        String path = Environment.getExternalStorageDirectory()
                .getPath() + File.separator + "vivalnk";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        path = LOCAL_PATH;
        dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void initLogButton() {
        Button btnLog = (Button) findViewById(R.id.btnLog);
        mListLog = new ArrayList<String>();
        mLogAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,
                mListLog);
        ListView lvLog = new ListView(MainActivity.this);
        lvLog.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lvLog.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        lvLog.setAdapter(mLogAdapter);


        mLogDialog = new AlertDialog.Builder(MainActivity.this)
                .setView(lvLog)
                .setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }

                }).create();


        mLogDialog.setCanceledOnTouchOutside(false);

        btnLog.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLogDialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.REQUEST_CODE_OPEN_SCAN_DEVICE_UI &&
                resultCode == AppConstants.RESULT_CODE_OK && data != null) {

            String pairDevice = data.getStringExtra("BLE_DEVICE");
            if (pairDevice.length() > 0) {
                isTemperatureUpdate = true;
                mPairedDeviceNumber = pairDevice.split(",").length;
                mBleManager.getBleReader().startTemperatureUpdate();
                mLatestTemperatureUpdateTime = System.currentTimeMillis();

                if (mPairedDeviceNumber == 1) {
                    mPairedDevice = pairDevice;
                    mTvResult.setText("deviceId " + mPairedDevice);
                    startDataReceiveCheckTimer();
                } else {
                    mTvResult.setText("Paired device: " + pairDevice);
                }

                mListLog.add(pairDevice + "  added to collect data:");
                mLogAdapter.notifyDataSetChanged();
            }

            mBleManager.setHandler(mLoadDataHandler);
        }
    }

    private void showVersion() {
	     /*View layoutVersion = LayoutInflater.from(this).inflate(R.layout.version, null);
	     ListView lvVersion = (ListView) layoutVersion.findViewById(R.id.lvVersion);
	     ArrayList<String> mlistVersion = new ArrayList<String>();
		 try {
			 PackageManager pm = getPackageManager();
		     PackageInfo pinfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
		     String versionName = pinfo.versionName;
	         mlistVersion.add("Copyright (c) 2020,Vivalnk,Inc." + "\n" +  "Version " + versionName);
		 } catch (NameNotFoundException e) {
			    e.printStackTrace();
		 }
	     ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.version_list_item, mlistVersion);
	     lvVersion.setAdapter(adapter);
		 AlertDialog adlgVersion = new AlertDialog.Builder(MainActivity.this)
	           //    .setView(layoutVersion)
	     .setPositiveButton("OK", new OnClickListener() {
	          public void onClick(DialogInterface dialog, int which) {
	          }
	     }).create();

	     adlgVersion.setView(layoutVersion,0,0,0,0);
	     adlgVersion.show();*/
    }


    @Override
    protected void onResume() {
        viLog.d(TAG, "onResume()");
        if (mBleManager.getBleReader() != null) {
            if (isTemperatureUpdate)
                mBleManager.getBleReader().resume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        viLog.d(TAG, "onPause()");
//        if (mBleManager.getBleReader() != null) {
//            if (isTemperatureUpdate)
//                mBleManager.getBleReader().suspend();
//        }

        super.onPause();

    }

    @Override
    protected void onStop() {
        viLog.d(TAG, "onStop()");
        if (mServiceBound) {
            mService.foreground();
            mService.foreground();
            unbindService(mConnection);
            mServiceBound = false;
        }

        super.onStop();
    }


    protected void onDestroy() {
        viLog.d(TAG, "onDestroy()");
        if (serviceIntent != null) {
            stopService(serviceIntent);
        }
        if (mPairedDeviceNumber == 1) {
            stopDataReceiveCheckTimer();
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

       /* if (mBleManager != null) {
            mBleManager.getBleReader().stopTemperatureUpdate();
            mBleManager.getBleReader().purgePDList();
            mBleManager.getBleReader().stopDeviceDiscovery();
            mBleManager.getBleReader().destroy();
            mBleManager.destroy();
        }*/
        if (mLogDialog != null)
            mLogDialog.dismiss();

        super.onDestroy();
    }

    private void outPrintLog(String deviceId, String log) {
       /* try {
            File file = new File(LOCAL_PATH + "/" + deviceId + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fis = new FileOutputStream(file, true);

            fis.write(log.getBytes());
            String newLine = System.getProperty("line.separator");
            fis.write(newLine.getBytes());
            fis.close();
            fis = null;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void onDeviceLost(String deviceId) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentTime = formatter.format(new Date());
        viLog.i(TAG, currentTime + ", onDeviceLost " + deviceId);

        mListLog.add(currentTime + " ,onDeviceLost " + deviceId);
        mLogAdapter.notifyDataSetChanged();

    }

    private String getDisplayTemperatureResultString(BleData data) {
        String deviceId = data.getDeviceId();
        int batteryPercent = data.getBatteryPercent();
        float rawTemperature = data.getRawTemperature();
        long currentTimeM = System.currentTimeMillis();
        float displayTemperature = data.getDisplayTemperature();

        TEMPERATURE_STATUS status = data.getTemperatureStatus();
        String statusString = "";
        if (status == TEMPERATURE_STATUS.NORMAL) {
            statusString = "temperatureNormal";
        } else if (status == TEMPERATURE_STATUS.WARMUP) {
            statusString = "temperatureWarmUp";
        }
        if ((currentTimeM - mLatestTemperatureUpdateTime) > AppConstants.DATA_RECEIVE_CHECK_DELAY) {
            long lostSamples = (currentTimeM - mLatestTemperatureUpdateTime) / AppConstants.DATA_RECEIVE_CHECK_PERIOD;
            if (isBackground) {
                mBackGroundLostSamples += lostSamples;
            } else {
                mForegroundLostSamples += lostSamples;
            }


        }
        mLatestTemperatureUpdateTime = System.currentTimeMillis();

        if (isBackground) {
            mBackgroundReceiveCounts++;
        } else {
            mForegroundReceiveCounts++;
        }

        StringBuilder result = new StringBuilder("deviceId");
        result.append(" ").append(deviceId);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentTime = formatter.format(new Date());
        String mode = "";
        if (isBackground) {
            mode = "BG: ";
        } else {
            mode = "FG: ";
        }
        float rawWithoutOffset = rawTemperature - CommonFunction.getFloatPreferenceValue(
                deviceId + CommonFunction.PREFERENCE_OFFEST_PREFIX,
                CommonFunction.DEFAULT_OFFEST);
        String logDescription = mode + deviceId + ", " + currentTime + ", rawWithoutOffset temperature " + rawWithoutOffset +
                ", raw temperature " + rawTemperature + ", display temperature " + displayTemperature +
                ", " + statusString + ", battery " + batteryPercent + "%" + ", FW " + data.getFW() +
                ", mac " + data.getMac() + ", rssi " + data.getRSSI();


        viLog.d(TAG, "onTemperatureUpdated  " + logDescription);
        mListLog.add(logDescription);
        mLogAdapter.notifyDataSetChanged();
        outPrintLog(deviceId, logDescription);

        result.append("  ").append(displayTemperature).append("  ").append(batteryPercent).append("%").append("\r\n");

        mLatestTemperatureString = result.toString();
        if (mPairedDeviceNumber == 1) {
            result.append(getDataReceiveString());
        }
        return result.toString();
    }

    private void onTemperatureMissed(String deviceId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentTime = formatter.format(new Date());
        String log = "Temperature missed " + deviceId + "  , " + currentTime + ".";
        mListLog.add(log);
        mLogAdapter.notifyDataSetChanged();
        outPrintLog(deviceId, log);
    }


    private float castOneDecimalFloat(float f) {
        int decimal = 1;
        BigDecimal bd = new BigDecimal(f);
        bd = bd.setScale(decimal, RoundingMode.FLOOR);
        viLog.d(TAG, "" + bd.floatValue());
        return bd.floatValue();
    }

    private void reset() {
        mTvResult.setText("");
        mListLog.clear();
        mLogAdapter.notifyDataSetChanged();
        mPairedDevice = "";
        mForegroundReceiveCounts = 0;
        mBackgroundReceiveCounts = 0;
        mForegroundLostSamples = 0;
        mBackGroundLostSamples = 0;
        mForeGroundSamples = 0;
        mBackGroundSamples = 0;
    }


    private void startDataReceiveCheckTimer() {
        mCheckDataReceiveTimer = new Timer();
        mCheckDataReceiveTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isBackground)
                    return;
                mForeGroundSamples++;
                updateLostSamples();
                mLoadDataHandler.sendEmptyMessage(AppConstants.MESSAGE_UPDATE_UI);
            }
        };
        mCheckDataReceiveTimer.scheduleAtFixedRate(mCheckDataReceiveTimerTask,
                AppConstants.DATA_RECEIVE_CHECK_DELAY, AppConstants.DATA_RECEIVE_CHECK_PERIOD);
    }

    private void stopDataReceiveCheckTimer() {
        if (mCheckDataReceiveTimer != null) {
            mCheckDataReceiveTimer.cancel();
            mCheckDataReceiveTimer.purge();
            mCheckDataReceiveTimer = null;
        }

        if (mCheckDataReceiveTimerTask != null) {
            mCheckDataReceiveTimerTask.cancel();
            mCheckDataReceiveTimerTask = null;
        }
    }

    private void updateUI() {
        StringBuilder result = new StringBuilder(mLatestTemperatureString);
        if (mPairedDeviceNumber == 1) {
            result.append(getDataReceiveString());
        }
        if (mLatestTemperatureString != "") {
            mTvResult.setText(result.toString());
        }
    }

    private void updateLostSamples() {
        long currentTimeM = System.currentTimeMillis();

        if ((currentTimeM - mLatestTemperatureUpdateTime) > AppConstants.DATA_RECEIVE_CHECK_DELAY) {
            long lostSamples = (currentTimeM - mLatestTemperatureUpdateTime) / AppConstants.DATA_RECEIVE_CHECK_PERIOD;
            if (isBackground) {
                mBackGroundLostSamples += lostSamples;
            } else {
                mForegroundLostSamples += lostSamples;
            }

            mLatestTemperatureUpdateTime = System.currentTimeMillis();
        }
    }

    private String getDataReceiveString() {
        StringBuilder result = new StringBuilder("");

        float foreDataReceivedRate;
        if (mForeGroundSamples != 0) {
            foreDataReceivedRate = (mForeGroundSamples - mForegroundLostSamples) * 1f / (mForeGroundSamples * 1f) * 100;
            if (foreDataReceivedRate < 0) {
                foreDataReceivedRate = 0;
            }
        } else {
            if (mForegroundReceiveCounts > 0) {
                foreDataReceivedRate = 100f;
            } else
                foreDataReceivedRate = -1f;
        }


        foreDataReceivedRate = castOneDecimalFloat(foreDataReceivedRate);

        float backDataReceivedRate;
        if (mBackGroundSamples != 0) {
            backDataReceivedRate = (mBackGroundSamples - mBackGroundLostSamples) * 1f / (mBackGroundSamples * 1f) * 100f;
            if (backDataReceivedRate < 0) {
                backDataReceivedRate = 0f;
            }
        } else {
            if (mBackgroundReceiveCounts > 0) {
                backDataReceivedRate = 100f;
            } else
                backDataReceivedRate = -1f;
        }


        backDataReceivedRate = castOneDecimalFloat(backDataReceivedRate);

        result.append("FG:").append(mForeGroundSamples - mForegroundLostSamples).append("/").append(mForeGroundSamples).append(" (");

        result.append(mForegroundReceiveCounts).append(") ").append((foreDataReceivedRate == -1) ? "NA" : foreDataReceivedRate).append("%, BG:");

        result.append(mBackGroundSamples - mBackGroundLostSamples).append("/").append(mBackGroundSamples).append(" (");

        result.append(mBackgroundReceiveCounts).append(") ").append((backDataReceivedRate == -1) ? "NA" : backDataReceivedRate).append("%");


        return result.toString();

    }

    private void realCheckPermission(List<String> data) {
        XXPermissions.with(this)
                .permission(data.toArray(new String[data.size()]))
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
                            if (
                                    !PermissionHelper.hasPermission(MainActivity.this, permission.BLUETOOTH_SCAN)
                                            || !PermissionHelper.hasPermission(MainActivity.this, permission.BLUETOOTH_CONNECT)
                            ) {
                                Toast.makeText(MainActivity.this,"You must grant the blue permissions under Android 13!",Toast.LENGTH_LONG).show();
                                finish();
                                return;
                            }
                        }
                        if (Build.VERSION.SDK_INT < VERSION_CODES.Q) {
                            if (!PermissionHelper.hasPermission(MainActivity.this, permission.ACCESS_COARSE_LOCATION)) {
                                Toast.makeText(MainActivity.this,"You must grant the location permission under Android 10!",Toast.LENGTH_LONG).show();
                                finish();
                                return;
                            }
                        }

                        // 29 <= api < 31
                        if (Build.VERSION.SDK_INT < VERSION_CODES.S) {
                            if (
                                    !PermissionHelper.hasPermission(MainActivity.this, permission.ACCESS_COARSE_LOCATION)
                                            || !PermissionHelper.hasPermission(MainActivity.this, permission.ACCESS_FINE_LOCATION)
                            ) {
                                Toast.makeText(MainActivity.this,"You must grant the location permissions under Android 12!",Toast.LENGTH_LONG).show();
                                finish();
                                return;
                            }
                        }

                        //api >= 31
                        if (Build.VERSION.SDK_INT >= 31) {
                            if (
                                    !PermissionHelper.hasPermission(MainActivity.this, permission.BLUETOOTH_CONNECT)
                                            || !PermissionHelper.hasPermission(MainActivity.this, permission.BLUETOOTH_SCAN)
                            ) {
                                Toast.makeText(MainActivity.this,"You must grant the bluetooth permissions on Android 12!",Toast.LENGTH_LONG).show();
                                finish();
                                return;
                            }
                        }

                        init();
                        initView();
                    }


                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            Toast.makeText(MainActivity.this, "被永久拒绝授权，请手动授予App相关权限", Toast.LENGTH_LONG).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                        } else {
                            Toast.makeText(MainActivity.this, "App相关权限授权失败", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}
