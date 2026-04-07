package com.vivalnk.sdk.demo.base.i18n;

import com.vivalnk.sdk.command.base.CommandAllType;
import com.vivalnk.sdk.command.checkmeo2.base.CommandType;
import com.vivalnk.sdk.common.utils.StringUtils;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.utils.GSON;
import java.util.Map;

public class ErrorMessageHandler_EN implements IRequestMessageHandler {
  @Override
  public String getRequestTypeName(DeviceModel model, int type) {
    String ret;
    if (model == DeviceModel.Checkme_O2) {
      ret = CommandType.getTypeName(type);
    } else {
      ret = CommandAllType.getTypeName(type);
    }
    return ret;
  }

  @Override
  public String getErrorMesage(DeviceModel model, int code, String msg) {
    return ErrorMessageData.getErrorMessage(ErrorMessageData.Locale.en, code, msg);
  }

  @Override
  public String getOnErrorMesage(DeviceModel model, int type, int code, String msg) {
    return "request onError: code = " + code + ", " + getErrorMesage(model, code, msg);
  }

  @Override
  public String getOnStartMessage(DeviceModel model, int type) {
    return "request onStart: " + getRequestTypeName(model, type);
  }

  @Override
  public String getOnCompleteMessage(DeviceModel model, int type, Map<String, Object> data) {
    return "request onComplete: " + (data == null ? "" : "data = " + GSON.toJson(data));
  }

  @Override
  public String getConnectErrorMeesage(Device device, int code, String msg) {
    return "on connect error: code = " + code + ", msg = " + msg + "\ndevice = " + device;
  }

  @Override
  public String getDisconnectedMeesage(Device device, boolean isForce) {
    return "on disconnect: isForce = " + isForce + "\ndevice = " + device;
  }

  @Override
  public String getConnectedMeesage(Device device) {
    return "on connected: " + "\ndevice = " +device;
  }
}
