package com.vivalnk.sdk.demo.vital.v200.ble;


import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import com.vivalnk.model.CacheTempData;
import com.vivalnk.model.ChargerInfo;
import com.vivalnk.model.DeviceInfo;
import com.vivalnk.model.TemperatureInfo;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.utils.DateFormat;
import com.vivalnk.sdk.utils.GSON;
import com.vivalnk.vdireader.VDICommonBleListener;
import com.vivalnk.vdireader.VDICommonBleReader;
import com.vivalnk.vdireader.VDIType;
import com.vivalnk.vdireaderimpl.CommonFunction;
import com.vivalnk.vdireaderimpl.VDIBleThermometer;
import com.vivalnk.vdireaderimpl.VDIBleThermometerL;
import com.vivalnk.vdiutility.viLog;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BleManager implements VDICommonBleListener {


	private static final String TAG = "BluetoothManager";

	private static volatile BleManager mInstance = null;

    private VDICommonBleReader mBleReader = null;
    private Context mContext;
    private static final String LOCAL_PATH = Environment.getExternalStorageDirectory()
            .getPath() + "/vivalnk/v200";

	private WeakReference<Handler> mHandlerRef;


	public void destroy() {
		if (mHandlerRef != null)
			mHandlerRef.clear();
		if (mBleReader != null)
			mBleReader.destroy();


		mHandlerRef = null;
		mBleReader = null;
		mInstance = null;
	}


	public static BleManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (BleManager.class) {
				if (mInstance == null) {
					mInstance = new BleManager(context);
				}
			}
		}

		return mInstance;
	}

    private BleManager(Context context) {
        mContext = context;
        initLogFileSave();
        if (Build.VERSION.SDK_INT >= 21)
            mBleReader = new VDIBleThermometerL(mContext);
        else
            mBleReader = new VDIBleThermometer(mContext);
        if (DeviceManager.getInstance().allowConnectLastConnectedDevice)
            mBleReader.connectLastDevice();
        mBleReader.setListener(this);
    }

	public VDICommonBleReader getBleReader() {
		return mBleReader;
	}



	public void setHandler(Handler handler) {
		if (mHandlerRef == null) {
			mHandlerRef = new WeakReference<Handler>(handler);
		} else {
			mHandlerRef.clear();
			mHandlerRef = null;
			mHandlerRef = new WeakReference<Handler>(handler);
		}
	}

	public int getPDListLength(){
		return mBleReader.getPDListLength();
	}

    public ArrayList<String> iteratePDList(){
        return mBleReader.iteratePDList();
    }

	@Override
	public void onNewDeviceDiscovered(DeviceInfo info) {
		// TODO Auto-generated method stub
		if (mHandlerRef == null) {
			viLog.e(TAG, "mHandlerRef is null.");
			return;
		}

		Handler handler = mHandlerRef.get();
		if (handler == null) {
			viLog.e(TAG, "handler is null.");
			return;
		}

		BleDevice bleDevice = new BleDevice();
		bleDevice.setDeviceId(info.getSN());
    bleDevice.setMac(info.getMacAddress());
    bleDevice.setRSSI(info.getRSSI());
    bleDevice.setDeviceType(info.getDeviceType());
		handler.obtainMessage(BleConstants.MESSAGE_DEVICE_FOUND, bleDevice)
		.sendToTarget();
	}

	public void onCachedRealTemperatureUpdated(CacheTempData data){
		if (mHandlerRef == null) {
			viLog.e(TAG, "mHandlerRef is null.");
			return;
		}

		Handler handler = mHandlerRef.get();
		if (handler == null) {
			viLog.e(TAG, "handler is null.");
			return;
		}


		String currentTime = formatter.format(new Date(data.getTempTime()));
		float rawWithOutOffset = data.getRawTemperature() - CommonFunction.getFloatPreferenceValue(
				data.getSN() + CommonFunction.PREFERENCE_OFFEST_PREFIX,
				CommonFunction.DEFAULT_OFFEST);
		String log = data.getSN() + ";RealTime: " + currentTime + ", " + data.getSN() + ", rawWithoutOffset temperature " + rawWithOutOffset
				+ ", raw temperature " + data.getRawTemperature() + ",display temperature " + data.getDisplayTemperature()  + ", temperature status "
				+ data.getTemperatureStatus() + ", fw " + data.getPatchFW() + ", battery " + data.getPatchBatteryLevel() + ", mac " + data.getMacAddress()
				+ ", rssi " + data.getRSSI() + "\n";

        handler.obtainMessage(BleConstants.MESSAGE_CACHE_REAL_UPDATED, log)
                .sendToTarget();

        final String date = DateFormatUtils.format(new Date(), DateFormat.sPattern);
        String print = date + "   RealTime: " + GSON.toJson(data);
        outPrintLog(data.getSN(), print);
    }

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public void onCachedHistoryTemperatureUpdated(CacheTempData data, List<CacheTempData> dataList) {
        if (mHandlerRef == null) {
            viLog.e(TAG, "mHandlerRef is null.");
            return;
        }

        Handler handler = mHandlerRef.get();
        if (handler == null) {
            viLog.e(TAG, "handler is null.");
            return;
        }

        String currentTime = formatter.format(new Date(data.getTempTime()));
        float rawWithOutOffset = data.getRawTemperature() - CommonFunction.getFloatPreferenceValue(
                data.getSN() + CommonFunction.PREFERENCE_OFFEST_PREFIX,
                CommonFunction.DEFAULT_OFFEST);
        String log = data.getSN() + ";RealTimeWithHistory: " + currentTime + ", " + data.getSN() + ", rawWithoutOffset temperature " + rawWithOutOffset
                + ", raw temperature " + data.getRawTemperature() + ",display temperature " + data.getDisplayTemperature() + ", temperature status "
                + data.getTemperatureStatus() + ", fw " + data.getPatchFW() + ", battery " + data.getPatchBatteryLevel() + ", mac " + data.getMacAddress()
                + ", rssi " + data.getRSSI() + "\n";
        final String date = DateFormatUtils.format(new Date(), DateFormat.sPattern);
        String print = date + "   RealTimeWithHistory: " + GSON.toJson(data);
        if (dataList != null && dataList.size() > 0) {
            int size = dataList.size();
            StringBuilder totalLog = new StringBuilder("History:").append("\n");
            StringBuilder totalLogPrint = new StringBuilder();
            totalLogPrint.append(print);
            for (int i = 0; i < size; i++) {
                currentTime = formatter.format(new Date(dataList.get(i).getTempTime()));
                rawWithOutOffset = dataList.get(i).getRawTemperature() - CommonFunction.getFloatPreferenceValue(
                        dataList.get(i).getSN() + CommonFunction.PREFERENCE_OFFEST_PREFIX,
                        CommonFunction.DEFAULT_OFFEST);
                totalLog.append(currentTime).append(", ").append(dataList.get(i).getSN()).append(", rawWithoutOffset temperature ")
                        .append(rawWithOutOffset).append(", raw temperature ").append(dataList.get(i).getRawTemperature())
                        .append(", display temperature ").append(dataList.get(i).getDisplayTemperature())
                        .append(", fw ").append(dataList.get(i).getPatchFW())
                        .append(", battery ").append(dataList.get(i).getPatchBatteryLevel()).append(", mac ")
                        .append(dataList.get(i).getMacAddress()).append(", rssi ").append(dataList.get(i).getRSSI())
                        .append("\n");
                totalLogPrint.append("\n");
                totalLogPrint.append("       History:");
                totalLogPrint.append(GSON.toJson(dataList.get(i)));
            }
            handler.obtainMessage(BleConstants.MESSAGE_CACHE_HISTORY_UPDATED, log + totalLog.toString())
                    .sendToTarget();
            outPrintLog(data.getSN(), totalLogPrint.toString());
        } else {
            handler.obtainMessage(BleConstants.MESSAGE_CACHE_HISTORY_UPDATED, log)
                    .sendToTarget();
            String log2 = date + "   RealTimeWithHistory: " + GSON.toJson(data);
            outPrintLog(data.getSN(), log2);
        }


    }

    public void onTemperatureUpdated(TemperatureInfo info) {

        if (mHandlerRef == null) {
            viLog.e(TAG, "mHandlerRef is null.");
            return;
        }

        Handler handler = mHandlerRef.get();
        if (handler == null) {
            viLog.e(TAG, "handler is null.");
            return;
        }

        BleData bleData = new BleData();
        bleData.setDeviceId(info.getSN());
        bleData.setBatteryPercent(info.getPatchBatteryLevel());
        bleData.setRSSI(info.getRSSI());
        bleData.setRawTemperature(info.getRawTemperature());
        bleData.setDisplayTemperature(info.getDisplayTemperature());
        bleData.setTemperatureStatus(info.getTemperatureStatus());
        bleData.setFW(info.getPatchFW());
        bleData.setMac(info.getMacAddress());
        handler.obtainMessage(BleConstants.MESSAGE_TEMPERATURE_UPDATE, bleData)
                .sendToTarget();
        final String date = DateFormatUtils.format(new Date(), DateFormat.sPattern);
        String log = date + "   Realtime: " + GSON.toJson(info);
        outPrintLog(info.getSN(), log);
    }

    @Override
    public void onChargerInfoUpdate(ChargerInfo info) {
        if (mHandlerRef == null) {
            viLog.e(TAG, "mHandlerRef is null.");
            return;
        }

        Handler handler = mHandlerRef.get();
        if (handler == null) {
            viLog.e(TAG, "handler is null.");
            return;
        }


        handler.obtainMessage(BleConstants.MESSAGE_CHARGER_INFO_UPDATED, info)
                .sendToTarget();
    }

    @Override
    public void onDeviceLost(String deviceId) {

        if (mHandlerRef == null) {
            viLog.e(TAG, "mHandlerRef is null.");
            return;
        }

        Handler handler = mHandlerRef.get();
        if (handler == null) {
            viLog.e(TAG, "handler is null.");
            return;
        }

        handler.obtainMessage(BleConstants.MESSAGE_DEVIE_LOST, deviceId)
                .sendToTarget();
    }

    public void onTemperatureAbnormalStatusUpdate(String deviceId, VDIType.ABNORMAL_TEMPERATURE_STATUS status) {
        if (mHandlerRef == null) {
            viLog.e(TAG, "mHandlerRef is null.");
            return;
        }

        Handler handler = mHandlerRef.get();
        if (handler == null) {
            viLog.e(TAG, "handler is null.");
            return;
        }
        String notification = "";
        if (status == VDIType.ABNORMAL_TEMPERATURE_STATUS.LOW_TEMPERATURE) {
            notification = deviceId + " low temperature notification,temperature lower than 34.5 Celsius!";
        }
        handler.obtainMessage(BleConstants.MESSAGE_TEMPERATURE_ABNORAML, notification)
                .sendToTarget();
    }


    @Override
    public void onTemperatureMissed(String deviceId) {
        if (mHandlerRef == null) {
            viLog.e(TAG, "mHandlerRef is null.");
            return;
        }

        Handler handler = mHandlerRef.get();
        if (handler == null) {
            viLog.e(TAG, "handler is null.");
            return;
        }

        handler.obtainMessage(BleConstants.MESSAGE_TEMPERATURE_MISSED, deviceId)
                .sendToTarget();
    }

    public void phoneBluetoothOff() {
        if (mHandlerRef == null) {
            viLog.e(TAG, "mHandlerRef is null.");
            return;
        }

        Handler handler = mHandlerRef.get();
        if (handler == null) {
            viLog.e(TAG, "handler is null.");
            return;
        }

        handler.obtainMessage(BleConstants.MESSAGE_PHONE_BLUETOOTH_OFF)
                .sendToTarget();
    }

    public void phoneLocationOff() {
        if (mHandlerRef == null) {
            viLog.e(TAG, "mHandlerRef is null.");
            return;
        }

        Handler handler = mHandlerRef.get();
        if (handler == null) {
            viLog.e(TAG, "handler is null.");
            return;
        }

        handler.obtainMessage(BleConstants.MESSAGE_PHONE_LOCATION_OFF)
                .sendToTarget();
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

    private void outPrintLog(String deviceId, String log) {
        try {
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
        }
    }

}
