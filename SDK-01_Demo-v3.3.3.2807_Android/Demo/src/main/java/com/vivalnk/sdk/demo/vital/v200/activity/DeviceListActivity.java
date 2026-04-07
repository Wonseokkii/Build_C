package com.vivalnk.sdk.demo.vital.v200.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.vital.v200.ble.BleConstants;
import com.vivalnk.sdk.demo.vital.v200.ble.BleDevice;
import com.vivalnk.sdk.demo.vital.v200.ble.BleManager;
import com.vivalnk.vdireaderimpl.CommonFunction;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class DeviceListActivity extends AppCompatActivity {

    private static final String TAG = "###DeviceListActivity";
    private static final int SCAN_PERIOD = 35 * 1000;

    private static final int MESSAGE_FLAG_SCAN_BLUETOOTH_TIMEOUT = 1;
    private ListView mLViewFoundDevice;
    private ArrayList<String> mlistFoundDevice;
    private MultipleAdapter mAdapter;
    private ProgressBar mPbcenter;


    private BleManager mBleManager;
    private LoadDataHandler mLoadDataHandler;

    private String mPairDeviceId = "";
    private AlertDialog mPasswordDialog;

    private static class LoadDataHandler extends Handler {
        private WeakReference<DeviceListActivity> mTarget;

        public LoadDataHandler(DeviceListActivity activity) {
            mTarget = new WeakReference<DeviceListActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            DeviceListActivity activity = mTarget.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            switch (msg.what) {
                case BleConstants.MESSAGE_DEVICE_FOUND:
                    BleDevice device = (BleDevice) msg.obj;
                    activity.mlistFoundDevice.add(device.getDeviceId() + ", " + device.getDeviceType());
                    activity.mAdapter.notifyDataSetChanged();

                    break;
                case MESSAGE_FLAG_SCAN_BLUETOOTH_TIMEOUT:
                    activity.mBleManager.getBleReader().stopDeviceDiscovery();
                    activity.mPbcenter.setVisibility(View.INVISIBLE);
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


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v200_device_list);
        initToolbar();
        mLViewFoundDevice = (ListView) findViewById(R.id.lvFoundDevicelist);
        mlistFoundDevice = new ArrayList<String>();
        mAdapter = new MultipleAdapter(DeviceListActivity.this,
                android.R.layout.simple_list_item_multiple_choice, mlistFoundDevice);
        mLViewFoundDevice.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mLViewFoundDevice.setAdapter(mAdapter);

        mPbcenter = (ProgressBar) findViewById(R.id.pgbCenter);
        mBleManager = BleManager.getInstance(this);
        mLoadDataHandler = new LoadDataHandler(this);
        mBleManager.setHandler(mLoadDataHandler);

        mBleManager.getBleReader().startDeviceDiscovery();
        mPbcenter.setVisibility(View.VISIBLE);
        mLoadDataHandler.sendEmptyMessageDelayed(MESSAGE_FLAG_SCAN_BLUETOOTH_TIMEOUT, SCAN_PERIOD);
        findViewById(R.id.tvRepair).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionRepair();
            }
        });
        findViewById(R.id.tvDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionDone();
            }
        });
    }

    protected void onDestroy() {
        if (mBleManager != null) {
            if (mBleManager.getBleReader() != null) {
                mBleManager.getBleReader().stopDeviceDiscovery();
            }
        }

        mAdapter.clear();
        mAdapter = null;
        super.onDestroy();
    }

    protected Toolbar toolbar;

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getResources().getString(R.string.main_menu_scaning));
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionDone();
                }
            });
        }
    }

    private void actionDone() {
        mPbcenter.setVisibility(View.INVISIBLE);
        mBleManager.getBleReader().stopDeviceDiscovery();
        mPairDeviceId = "";

        long[] position = mLViewFoundDevice.getCheckedItemIds();
        if (position.length > 0) {
            String deviceId = mlistFoundDevice.get((int) (position[0])).split(",")[0];
            String type = mlistFoundDevice.get((int) (position[0])).split(",")[1];


            if (deviceId.length() > 0) {
                showPasswordDialog(deviceId, type, 0, position.length, position);
            }

        } else {
            Intent intent = new Intent();
            intent.putExtra("BLE_DEVICE", mPairDeviceId);
            setResult(AppConstants.RESULT_CODE_OK, intent);
            finish();
        }

    }

    private void showPasswordDialog(final String deviceId, final String type, final int index, final int length,
                                    final long[] position) {

        if (!type.contains("ENCRYPTED")) {
            mBleManager.getBleReader().addPDList(deviceId);
            if (mPairDeviceId.length() == 0)
                mPairDeviceId = deviceId;
            else
                mPairDeviceId = mPairDeviceId + ", " + deviceId;

            if (index == (length - 1)) {
                Intent intent = new Intent();
                intent.putExtra("BLE_DEVICE", mPairDeviceId);
                setResult(AppConstants.RESULT_CODE_OK, intent);
                finish();
            } else {

                String deviceIdNext = mlistFoundDevice.get((int) (position[index + 1])).split(",")[0];

                String deviceTypeNext = mlistFoundDevice.get((int) (position[index + 1])).split(",")[1];
                if (deviceIdNext.length() > 0) {
                    showPasswordDialog(deviceIdNext, deviceTypeNext, (index + 1), length, position);
                }
            }
            return;
        }

        final String savedPassword = CommonFunction
                .getStringPreferenceValue(deviceId + CommonFunction.PREFERENCE_PASSWORD_PREFIX, "");

        final EditText et = new EditText(this);
        et.setText(savedPassword);

        StringBuilder title = new StringBuilder("Input the ");
        title.append(deviceId).append("'s password");
        StringBuilder message = new StringBuilder("Notice the difference of  number" + 1)
                .append(" and character l").
                append("; number " + 0).append(" and character o");
        String[] items = new String[]{"4s encrypted", "8s encrypted"};
        final String skipMessage = "Skip " + deviceId + "'s password setting and do not add it";
        mPasswordDialog = new AlertDialog.Builder(this).setTitle(title.toString())
                .setMessage(message.toString())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("OK", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();

                        if (input.equals("")) {
                            Toast.makeText(DeviceListActivity.this, "Empty password！", Toast.LENGTH_LONG).show();
                            setDialogShow(mPasswordDialog, false);

                        } else if (input.length() != 8) {
                            Toast.makeText(DeviceListActivity.this, "Password length should be 8！", Toast.LENGTH_LONG).show();
                            setDialogShow(mPasswordDialog, false);
                        } else {
                            boolean ok = mBleManager.getBleReader().addPDList(deviceId, input);
                            if (!ok) {
                                Toast.makeText(DeviceListActivity.this, "Add device fail!", Toast.LENGTH_LONG).show();
                                setDialogShow(mPasswordDialog, false);
                            } else {
                                CommonFunction.saveStringPreferenceValue(deviceId + CommonFunction.PREFERENCE_PASSWORD_PREFIX, input);
                                if (mPairDeviceId.length() == 0)
                                    mPairDeviceId = deviceId;
                                else
                                    mPairDeviceId = mPairDeviceId + ", " + deviceId;

                                setDialogShow(mPasswordDialog, true);
                                dialog.dismiss();
                                dialog.cancel();

                                if (index == (length - 1)) {
                                    Intent intent = new Intent();
                                    intent.putExtra("BLE_DEVICE", mPairDeviceId);
                                    setResult(AppConstants.RESULT_CODE_OK, intent);
                                    finish();
                                } else {

                                    String deviceIdNext = mlistFoundDevice.get((int) (position[index + 1])).split(",")[0];

                                    String deviceTypeNext = mlistFoundDevice.get((int) (position[index + 1])).split(",")[1];
                                    if (deviceIdNext.length() > 0) {
                                        showPasswordDialog(deviceIdNext, deviceTypeNext, (index + 1), length, position);
                                    }
                                }
                            }


                        }


                    }
                })
                .setNegativeButton("Skip", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DeviceListActivity.this, skipMessage, Toast.LENGTH_LONG).show();
                        setDialogShow(mPasswordDialog, true);
                        dialog.dismiss();
                        dialog.cancel();
                        if (index == (length - 1)) {
                            if (mPairDeviceId.length() == 0) {
                                Toast.makeText(DeviceListActivity.this, "Please add one device at least", Toast.LENGTH_LONG).show();
                            } else {
                                Intent intent = new Intent();
                                intent.putExtra("BLE_DEVICE", mPairDeviceId);
                                setResult(AppConstants.RESULT_CODE_OK, intent);
                                finish();
                            }

                        } else {

                            String deviceIdSkip = mlistFoundDevice.get((int) (position[index + 1])).split(",")[0];

                            String deviceTypeSkip = mlistFoundDevice.get((int) (position[index + 1])).split(",")[1];
                            if (deviceIdSkip.length() > 0) {
                                showPasswordDialog(deviceIdSkip, deviceTypeSkip, (index + 1), length, position);
                            }
                        }


                    }
                }).show();
        mPasswordDialog.setCanceledOnTouchOutside(false);

    }

    private void setDialogShow(AlertDialog dialog, boolean notShow) {
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, notShow);
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
    }

    private void actionRepair() {
        mAdapter.clear();
        mAdapter = null;
        mAdapter = new MultipleAdapter(DeviceListActivity.this,
                android.R.layout.simple_list_item_multiple_choice, mlistFoundDevice);
        mLViewFoundDevice.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mLViewFoundDevice.setAdapter(mAdapter);

        mBleManager.getBleReader().stopDeviceDiscovery();
        mBleManager.getBleReader().purgePDList();
        mlistFoundDevice.clear();
        mAdapter.notifyDataSetChanged();
        mLoadDataHandler.removeMessages(MESSAGE_FLAG_SCAN_BLUETOOTH_TIMEOUT);
        mBleManager.getBleReader().startDeviceDiscovery();
        mPbcenter.setVisibility(View.VISIBLE);
        mLoadDataHandler.sendEmptyMessageDelayed(MESSAGE_FLAG_SCAN_BLUETOOTH_TIMEOUT, SCAN_PERIOD);
    }

    private class MultipleAdapter extends ArrayAdapter<String> {

        /* (non-Javadoc)
         * @see android.widget.BaseAdapter#hasStableIds()
         */
        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return true;
        }

        public MultipleAdapter(Context context, int resource) {
            super(context, resource);
            // TODO Auto-generated constructor stub
        }

        public MultipleAdapter(Context context,
                               int resource, ArrayList<String> listString) {
            super(context, resource, listString);
            // TODO Auto-generated constructor stub
        }


    }
}
