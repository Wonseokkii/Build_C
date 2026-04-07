package com.vivalnk.sdk.demo.repository.database.room;

import androidx.room.TypeConverter;
import com.vivalnk.sdk.demo.repository.database.convert.BaseMapConverter;
import java.util.Map;

public class MapConverter extends BaseMapConverter {

  @TypeConverter
  @Override
  public Map<String, Object> convertToEntityProperty(String databaseValue) {
    return super.convertToEntityProperty(databaseValue);
  }

  @TypeConverter
  @Override
  public String convertToDatabaseValue(Map<String, Object> entityProperty) {
    return super.convertToDatabaseValue(entityProperty);
  }

}
