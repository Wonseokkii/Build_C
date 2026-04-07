package com.vivalnk.sdk.demo.repository.database.room;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.model.common.IVitalDevice;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

@androidx.room.Entity(
    tableName = "demo_device",
    primaryKeys = {"d_id", "d_model"},
    inheritSuperIndices = true,
    indices = { @androidx.room.Index(value = {"d_id", "d_model"}, unique = true) }
)
public class VitalDevice_Room implements IVitalDevice {
  @ColumnInfo(name = "id")
  public long id;

  @ColumnInfo(name = "d_id")
  @NotNull
  public String deviceID;

  @ColumnInfo(name = "d_sn")
  public String deviceSN;

  @ColumnInfo(name = "d_name")
  public String deviceName;

  @NotNull
  @ColumnInfo(name = "d_model")
  @TypeConverters(DeviceModelConverter.class)
  public DeviceModel deviceModel;

  @ColumnInfo(name = "hw_v")
  public String hwVersion;

  @ColumnInfo(name = "fw_v")
  public String fwVersion;

  @ColumnInfo(name = "info")
  public String deviceInfo;

  public VitalDevice_Room() {

  }
  public VitalDevice_Room(IVitalDevice device) {
    this.id = device.getId();
    this.deviceID = device.getDeviceID();
    this.deviceSN = device.getDeviceSN();
    this.deviceName = device.getDeviceName();
    this.deviceModel = device.getDeviceModel();
    this.hwVersion = device.getHwVersion();
    this.fwVersion = device.getFwVersion();
    this.deviceInfo = DeviceMapConverter.convertToDatabaseValue(device.getExtras());
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
    this.deviceInfo = DeviceMapConverter.convertToDatabaseValue(extras);
  }

  @Override
  public Map<String, Object> getExtras() {
    return DeviceMapConverter.convertToEntityProperty(deviceInfo);
  }

}
