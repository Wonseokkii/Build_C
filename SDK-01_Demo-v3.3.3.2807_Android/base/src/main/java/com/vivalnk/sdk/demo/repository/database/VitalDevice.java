package com.vivalnk.sdk.demo.repository.database;

import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceInfoUtils;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.model.common.IVitalDevice;
import com.vivalnk.sdk.utils.ObjectUtils;
import java.util.Map;
import java.util.Objects;

public class VitalDevice implements IVitalDevice {
  public long id;
  public String deviceID;
  public String deviceSN;
  public String deviceName;
  public DeviceModel deviceModel;
  public String hwVersion;
  public String fwVersion;
  public Map<String, Object> extras;

  public VitalDevice() {
  }

  public VitalDevice(Device device) {
    setDeviceID(device.getId());
    setDeviceSN(device.getSn());
    setDeviceName(device.getName());
    setDeviceModel(device.getModel());
    setHwVersion(DeviceInfoUtils.getHwVersion(device));
    setFwVersion(DeviceInfoUtils.getFwVersion(device));
    setExtras(device.getExtras());
  }

  public VitalDevice(IVitalDevice device) {
    this.extras = device.getExtras();
    this.id = device.getId();
    this.deviceID = device.getDeviceID();
    this.deviceSN = device.getDeviceSN();
    this.deviceName = device.getDeviceName();
    this.deviceModel = device.getDeviceModel();
    this.hwVersion = device.getHwVersion();
    this.fwVersion = device.getFwVersion();
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public void setId(long id) {
    this.id = id;
  }

  public String getDeviceID() {
    return deviceID;
  }

  public void setDeviceID(String deviceID) {
    this.deviceID = deviceID;
  }

  public String getDeviceSN() {
    return deviceSN;
  }

  public void setDeviceSN(String deviceSN) {
    this.deviceSN = deviceSN;
  }

  @Override
  public String getDeviceName() {
    return deviceName;
  }

  @Override
  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public DeviceModel getDeviceModel() {
    return deviceModel;
  }

  public void setDeviceModel(DeviceModel deviceModel) {
    this.deviceModel = deviceModel;
  }

  public String getHwVersion() {
    return hwVersion;
  }

  public void setHwVersion(String hwVersion) {
    this.hwVersion = hwVersion;
  }

  public String getFwVersion() {
    return fwVersion;
  }

  public void setFwVersion(String fwVersion) {
    this.fwVersion = fwVersion;
  }

  @Override
  public void setExtras(Map<String, Object> extras) {
    this.extras = extras;
  }

  @Override
  public Map<String, Object> getExtras() {
    return extras;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VitalDevice)) return false;
    VitalDevice
        that = (VitalDevice) o;
    return id == that.id &&
        Objects.equals(deviceID, that.deviceID) &&
        Objects.equals(deviceSN, that.deviceSN) &&
        Objects.equals(deviceName, that.deviceName) &&
        deviceModel == that.deviceModel &&
        Objects.equals(hwVersion, that.hwVersion) &&
        Objects.equals(fwVersion, that.fwVersion) &&
        ObjectUtils.mapDeepEquals(extras, that.extras);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, deviceID, deviceSN, deviceName, deviceModel, hwVersion, fwVersion, extras);
  }
}
