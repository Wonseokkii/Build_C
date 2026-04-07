package com.vivalnk.sdk.demo.base.i18n;

import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceModel;
import java.util.Map;

public interface IRequestMessageHandler {
  String getRequestTypeName(DeviceModel model, int type);
  String getErrorMesage(DeviceModel model, int type, String msg);
  String getOnErrorMesage(DeviceModel model, int type, int code, String msg);
  String getOnStartMessage(DeviceModel model, int type);
  String getOnCompleteMessage(DeviceModel model, int type, Map<String, Object> data);

  String getConnectErrorMeesage(Device device, int code, String msg);
  String getDisconnectedMeesage(Device device, boolean isForce);
  String getConnectedMeesage(Device device);
}
