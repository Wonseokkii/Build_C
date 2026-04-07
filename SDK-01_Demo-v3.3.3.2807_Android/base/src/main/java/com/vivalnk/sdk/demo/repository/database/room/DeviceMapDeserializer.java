package com.vivalnk.sdk.demo.repository.database.room;

import com.vivalnk.google.gson.JsonDeserializationContext;
import com.vivalnk.google.gson.JsonDeserializer;
import com.vivalnk.google.gson.JsonElement;
import com.vivalnk.google.gson.JsonObject;
import com.vivalnk.google.gson.JsonParseException;
import com.vivalnk.google.gson.JsonPrimitive;
import com.vivalnk.sdk.model.DeviceInfoKey;
import com.vivalnk.sdk.model.DeviceInfoUtils;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.model.common.DataType;
import com.vivalnk.sdk.model.common.IDataDeserializer;
import com.vivalnk.sdk.model.common.IMapParser;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DeviceMapDeserializer implements JsonDeserializer<Map<String, Object>>, IMapParser {
  @Override
  public Map<String, Object> deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    if (json.isJsonObject()) {
      return parseMap(json.getAsJsonObject(), typeOfT, context);
    } else {
      return null;
    }
  }

  private Map<String, Object> parseMap(JsonObject jsonObject, Type typeOfT, JsonDeserializationContext context) {

    if (jsonObject == null && jsonObject.isJsonNull()) {
      return null;
    }
    Map<String, Object> ret = IDataDeserializer.deserializeMapPrimitive(jsonObject, this);

    //对于Primitive.Number 以及 复杂类型的解析: String以及boolean的解析, 可以不用手动解析
    if (jsonObject.has(DeviceInfoKey.sn)) {
      ret.put(DeviceInfoKey.sn, jsonObject.get(DeviceInfoKey.sn).getAsString());
    }
    if (jsonObject.has(DeviceInfoKey.accSamplingAccuracy)) {
      ret.put(DeviceInfoKey.accSamplingAccuracy, jsonObject.get(DeviceInfoKey.accSamplingAccuracy).getAsInt());
    }
    if (jsonObject.has(DeviceInfoKey.accSamplingFrequency)) {
      String hz = jsonObject.get(DeviceInfoKey.accSamplingFrequency).getAsString();
      //兼容老版本database
      if (hz.toLowerCase().contains("hz")) {
        int accHZ = DeviceInfoUtils.getAccSFFromDeviceAttributeTable(getMap(DeviceInfoKey.accSamplingFrequency, hz));
        ret.put(DeviceInfoKey.accSamplingFrequency, accHZ);
      } else {
        ret.put(DeviceInfoKey.accSamplingFrequency, jsonObject.get(DeviceInfoKey.accSamplingFrequency).getAsInt());
      }
    }
    if (jsonObject.has(DeviceInfoKey.ecgSamplingFrequency)) {
      String hz = jsonObject.get(DeviceInfoKey.ecgSamplingFrequency).getAsString();
      //兼容老版本database
      if (hz.toLowerCase().contains("hz")) {
        int accHZ = DeviceInfoUtils.getEcgSFFromDeviceAttributeTable(getMap(DeviceInfoKey.ecgSamplingFrequency, hz));
        ret.put(DeviceInfoKey.ecgSamplingFrequency, accHZ);
      } else {
        ret.put(DeviceInfoKey.ecgSamplingFrequency, jsonObject.get(DeviceInfoKey.ecgSamplingFrequency).getAsInt());
      }
    }
    if (jsonObject.has(DeviceInfoKey.encryption)) {
      String enc = jsonObject.get(DeviceInfoKey.encryption).getAsString();
      //兼容老版本database
      if (enc.toUpperCase().contains("ENC")) {
        boolean encryption = DeviceInfoUtils.isEncryption(getMap(DeviceInfoKey.encryption, enc));
        ret.put(DeviceInfoKey.encryption, encryption);
      } else {
        ret.put(DeviceInfoKey.encryption, jsonObject.get(DeviceInfoKey.encryption).getAsBoolean());
      }
    }
    if (jsonObject.has(DeviceInfoKey.fwVersion)) {
      ret.put(DeviceInfoKey.fwVersion, jsonObject.get(DeviceInfoKey.fwVersion).getAsString());
    }
    if (jsonObject.has(DeviceInfoKey.hwVersion)) {
      ret.put(DeviceInfoKey.hwVersion, jsonObject.get(DeviceInfoKey.hwVersion).getAsString());
    }
    if (jsonObject.has(DeviceInfoKey.hasHR)) {
      String enc = jsonObject.get(DeviceInfoKey.hasHR).getAsString();
      //兼容老版本database
      if (enc.toUpperCase().contains("HR")) {
        boolean encryption = DeviceInfoUtils.hasHR(getMap(DeviceInfoKey.hasHR, enc));
        ret.put(DeviceInfoKey.hasHR, encryption);
      } else {
        ret.put(DeviceInfoKey.hasHR, jsonObject.get(DeviceInfoKey.hasHR).getAsBoolean());
      }
    }
    if (jsonObject.has(DeviceInfoKey.magnification)) {
      String magnification = jsonObject.get(DeviceInfoKey.magnification).getAsString();
      if (magnification.contains("*")) {
        int magnificationI = DeviceInfoUtils.getMagnification(getMap(DeviceInfoKey.hasHR, magnification));
        ret.put(DeviceInfoKey.magnification, magnificationI);
      } else {
        ret.put(DeviceInfoKey.magnification, jsonObject.get(DeviceInfoKey.magnification).getAsInt());
      }
    }
    if (jsonObject.has(DeviceInfoKey.manufacturer)) {
      ret.put(DeviceInfoKey.manufacturer, jsonObject.get(DeviceInfoKey.manufacturer).getAsString());
    }
    if (jsonObject.has(DeviceInfoKey.model)) {
      ret.put(DeviceInfoKey.model, DeviceModel.valueOf(jsonObject.get(DeviceInfoKey.model).getAsString()));
    }
    if (jsonObject.has(DeviceInfoKey.productType)) {
      ret.put(DeviceInfoKey.productType, jsonObject.get(DeviceInfoKey.productType).getAsInt());
    }
    return ret;
  }

  @Override
  public Class getDataType(String key) {
    return DeviceInfoKey.getDataType(key);
  }

  private Map<String, Object> getMap(String key, Object value) {
    Map<String, Object> ret = new HashMap<>();
    ret.put(key, value);
    return ret;
  }

}
