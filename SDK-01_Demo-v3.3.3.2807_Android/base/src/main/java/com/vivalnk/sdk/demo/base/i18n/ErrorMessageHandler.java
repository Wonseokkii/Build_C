package com.vivalnk.sdk.demo.base.i18n;

import android.os.Build;
import android.os.LocaleList;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceModel;
import java.util.Locale;
import java.util.Map;

public class ErrorMessageHandler implements IRequestMessageHandler{

  private static IRequestMessageHandler handler_en;
  private static IRequestMessageHandler handler_zh_cn;

  private static String zh_cn = "zh-CN";
  private static String en_US = "en-US";

  private static class SingletonHolder {
    private static final ErrorMessageHandler INSTANCE = new ErrorMessageHandler();
  }
  private ErrorMessageHandler() {}

  public static final ErrorMessageHandler getInstance() {
    return SingletonHolder.INSTANCE;
  }

  static {
    handler_en = new ErrorMessageHandler_EN();
    handler_zh_cn = new ErrorMessageHandler_ZH_CN();
  }

  private static IRequestMessageHandler getRequestMessageHandler() {
    Locale locale;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      locale = LocaleList.getDefault().get(0);
    } else {
      locale = Locale.getDefault();
    }

    String language = locale.getLanguage() + "-" + locale.getCountry();

    if (language.contains(zh_cn)) {
      return handler_zh_cn;
    } else if (language.contains(en_US)) {
      return handler_en;
    } else {
      return handler_en;
    }
  }

  @Override
  public String getRequestTypeName(DeviceModel model, int type) {
    return getRequestMessageHandler().getRequestTypeName(model, type);
  }

  @Override
  public String getErrorMesage(DeviceModel model, int type, String msg) {
    return getRequestMessageHandler().getErrorMesage(model, type, msg);
  }

  @Override
  public String getOnErrorMesage(DeviceModel model, int type, int code, String msg) {
    return getRequestMessageHandler().getOnErrorMesage(model, type, code, msg);
  }

  public String getOnStartMessage(DeviceModel model, int type) {
    return getRequestMessageHandler().getOnStartMessage(model, type);
  }

  public String getOnCompleteMessage(DeviceModel model, int type, Map<String, Object> data) {
    return getRequestMessageHandler().getOnCompleteMessage(model, type, data);
  }

  @Override
  public String getConnectErrorMeesage(Device device, int code, String msg) {
    return getRequestMessageHandler().getConnectErrorMeesage(device, code, msg);
  }

  @Override
  public String getDisconnectedMeesage(Device device, boolean isForce) {
    return getRequestMessageHandler().getDisconnectedMeesage(device, isForce);
  }

  @Override
  public String getConnectedMeesage(Device device) {
    return getRequestMessageHandler().getConnectedMeesage(device);
  }
}
