package com.vivalnk.sdk.demo.repository.database.room;

import androidx.room.TypeConverter;
import com.vivalnk.google.gson.Gson;
import com.vivalnk.google.gson.GsonBuilder;
import com.vivalnk.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class DeviceMapConverter {

  private static Gson gson;
  private static Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
  static {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(mapType, new DeviceMapDeserializer());
    gson = gsonBuilder.create();
  }
  public DeviceMapConverter() { }

  @TypeConverter
  public static Map<String, Object> convertToEntityProperty(String databaseValue) {
    if (null == databaseValue) {
      return null;
    }

    Map<String, Object> ret = gson.fromJson(databaseValue, mapType);

    return ret;
  }

  @TypeConverter
  public static String convertToDatabaseValue(Map<String, Object> entityProperty) {
    return gson.toJson(entityProperty, mapType);
  }

}
