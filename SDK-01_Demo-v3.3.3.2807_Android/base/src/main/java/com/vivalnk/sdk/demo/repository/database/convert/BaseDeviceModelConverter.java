package com.vivalnk.sdk.demo.repository.database.convert;

import com.vivalnk.sdk.model.DeviceModel;

public class BaseDeviceModelConverter {

  public DeviceModel convertToEntityProperty(Integer databaseValue) {
    if (null == databaseValue) {
      return null;
    }

    return DeviceModel.valueOf(databaseValue);
  }

  public Integer convertToDatabaseValue(DeviceModel entityProperty) {
    return entityProperty == null ? null : entityProperty.value;
  }
}
