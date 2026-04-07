package com.vivalnk.sdk.demo.repository.database.room;

import androidx.room.TypeConverter;
import com.vivalnk.sdk.demo.repository.database.convert.BaseDeviceModelConverter;
import com.vivalnk.sdk.model.DeviceModel;

public class DeviceModelConverter extends BaseDeviceModelConverter {
  @TypeConverter
  @Override
  public DeviceModel convertToEntityProperty(Integer databaseValue) {
    return super.convertToEntityProperty(databaseValue);
  }

  @TypeConverter
  @Override
  public Integer convertToDatabaseValue(DeviceModel entityProperty) {
    return super.convertToDatabaseValue(entityProperty);
  }
}
