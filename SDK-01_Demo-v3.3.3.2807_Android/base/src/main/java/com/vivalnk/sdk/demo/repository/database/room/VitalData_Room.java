package com.vivalnk.sdk.demo.repository.database.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.TypeConverters;
import com.vivalnk.sdk.data.DataJsonConverter;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.model.common.IVitalData;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Created by JakeMo on 18-4-25.
 */
@Entity(
    tableName = "demo_data",
    primaryKeys = {"d_id", "model", "time"},
    inheritSuperIndices = true,
    indices = { @Index(value = {"d_id", "model", "time"}, unique = true) }
)
public class VitalData_Room extends IVitalData implements Serializable {

  @ColumnInfo(name = "id")
  public long id;

  @ColumnInfo(name = "d_id")
  @NotNull
  public String deviceID;

  @ColumnInfo(name = "d_sn")
  public String deviceSN;

  @ColumnInfo(name = "d_name")
  public String deviceName;

  @ColumnInfo(name = "model")
  @TypeConverters(DeviceModelConverter.class)
  @NotNull
  public DeviceModel deviceModel;

  @ColumnInfo(name = "time")
  @NotNull
  public long time;

  @TypeConverters(MapConverter.class)
  public Map<String, Object> extras = new LinkedHashMap<>();

  public VitalData_Room() {
  }

  public VitalData_Room(IVitalData iVitalData) {
    this.extras = iVitalData.getExtras();
    this.id = iVitalData.getId();
    this.deviceID = iVitalData.getDeviceID();
    this.deviceSN = iVitalData.getDeviceSN();
    this.deviceName = iVitalData.getDeviceName();
    this.deviceModel = iVitalData.getDeviceModel();
    this.time = iVitalData.getTime();
  }

  public long getId() {
    return id;
  }

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

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public DeviceModel getDeviceModel() {
    return deviceModel;
  }

  public void setDeviceModel(DeviceModel deviceModel) {
    this.deviceModel = deviceModel;
  }

  public Map<String, Object> getExtras() {
    return extras;
  }

  public void setExtras(Map<String, Object> extras) {
    this.extras = extras;
  }

  @Override
  public void addExtras(Map<String, Object> extras) {
    this.extras.putAll(extras);
  }

  public Long getTime() {
    return time;
  }

  public void setTime(Long time) {
    this.time = time;
  }


  @Override
  public VitalData_Room clone() {
    return new VitalData_Room(this);
  }

}
