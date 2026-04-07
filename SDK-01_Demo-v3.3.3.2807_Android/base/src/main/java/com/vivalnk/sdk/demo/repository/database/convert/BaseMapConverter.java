package com.vivalnk.sdk.demo.repository.database.convert;

import com.vivalnk.google.gson.Gson;
import com.vivalnk.google.gson.GsonBuilder;
import com.vivalnk.google.gson.reflect.TypeToken;
import com.vivalnk.sdk.model.common.MapDeserializer;
import java.lang.reflect.Type;
import java.util.Map;

public class BaseMapConverter {

  private static Gson gson;
  private static Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
  static {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(mapType, new MapDeserializer());
    gson = gsonBuilder.create();
  }

  public Map<String, Object> convertToEntityProperty(String databaseValue) {
    if (null == databaseValue) {
      return null;
    }

    Map<String, Object> ret = gson.fromJson(databaseValue, mapType);

    return ret;
  }

  public String convertToDatabaseValue(Map<String, Object> entityProperty) {
    return gson.toJson(entityProperty, mapType);
  }
}
