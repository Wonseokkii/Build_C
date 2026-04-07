package com.vivalnk.sdk.demo.vital.v200.ble;

import com.vivalnk.vdireader.VDIType.TEMPERATURE_STATUS;

import java.io.Serializable;

public class BleData implements Serializable{
	
	private static final long serialVersionUID = -1151382250238024534L;
	
	private String deviceId;
	private int batteryPercent;
	private float rawTemperature;
	private float displayTemperature;
	private String fw;
	private String mac;
	private int RSSI;
	private TEMPERATURE_STATUS temperatureStatus;
	private Object realData;

	public Object getRealData() {
		return realData;
	}

	public void setRealData(Object realData) {
		this.realData = realData;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setBatteryPercent(int batteryPercent) {
		this.batteryPercent = batteryPercent;
	}
	
	public int getBatteryPercent() {
		return batteryPercent;
	}

	public void setRawTemperature(float temperature) {
		this.rawTemperature = temperature;
	}
	
	public float getRawTemperature() {
		return rawTemperature;
	}

	public void setDisplayTemperature(float temperature) {
		displayTemperature = temperature;
	}

	public float getDisplayTemperature() {
		return displayTemperature;
	}

	public void setFW(String fw) {
		this.fw = fw;
	}

	public String getFW() {
		return fw;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getMac() {
		return mac;
	}

	public void setRSSI(int RSSI) {
		this.RSSI = RSSI;
	}

	public int getRSSI() {
		return RSSI;
	}

	public void setTemperatureStatus(TEMPERATURE_STATUS status) {
		temperatureStatus = status;
	}

	public TEMPERATURE_STATUS getTemperatureStatus() {
		return temperatureStatus;
	}
}
