package com.vivalnk.sdk.demo.vital.v200.ble;

import com.vivalnk.vdireader.VDIType.DEVICE_TYPE;

import java.io.Serializable;

public class BleDevice implements Serializable{
	
	private static final long serialVersionUID = -5404305906224876702L;

	private String mDeviceId;
	private String mMac;
	private int mRssi;
	private DEVICE_TYPE type;

	public String getDeviceId() {
		return mDeviceId;
	}

	public void setDeviceId(String deviceId) {
		mDeviceId = deviceId;
	}

	public String getMac() {
		return mMac;
	}

	public void setMac(String mac) {
		mMac = mac;
	}

	public void setRSSI(int rssi) {
		mRssi = rssi;
	}

	public int getRSSI() {
		return mRssi;
	}

	public void setDeviceType(DEVICE_TYPE type) {
		this.type = type;
	}

	public DEVICE_TYPE getDeviceType() {
		return type;
	}
}
