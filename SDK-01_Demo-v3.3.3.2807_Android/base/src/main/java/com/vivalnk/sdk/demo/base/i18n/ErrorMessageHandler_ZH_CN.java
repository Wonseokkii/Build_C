package com.vivalnk.sdk.demo.base.i18n;

import com.vivalnk.sdk.command.base.CommandAllType;
import com.vivalnk.sdk.command.checkmeo2.base.CommandType;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceModel;
import com.vivalnk.sdk.utils.GSON;
import java.util.Map;

public class ErrorMessageHandler_ZH_CN implements IRequestMessageHandler{

  @Override
  public String getErrorMesage(DeviceModel model, int code, String msg) {
    return ErrorMessageData.getErrorMessage(ErrorMessageData.Locale.cn, code, msg);
  }

  @Override
  public String getOnErrorMesage(DeviceModel model, int type, int code, String msg) {
    return "请求错误：错误码 = " + code + ", " + getErrorMesage(model, code, msg);
  }

  @Override
  public String getOnStartMessage(DeviceModel model, int type) {
    return "请求开始, 请求类型:" + getRequestTypeName(model, type);
  }

  @Override
  public String getOnCompleteMessage(DeviceModel model, int type, Map<String, Object> data) {
    return "请求完成, 请求类型:" + (data == null ? "" : ", 数据 = " + GSON.toJson(data));
  }

  @Override
  public String getRequestTypeName(DeviceModel model, int type) {

    switch (type) {
      case (CommandAllType.checkFlashDataStatus):
        return "检查闪存状态";
      case (CommandAllType.uploadFlash):
        return "上传闪存数据";
      case (CommandAllType.cancelUploadFlash):
        return "取消上传闪存数据";
      case (CommandAllType.eraseFlash):
        return "擦除闪存数据";
      case (CommandAllType.selfTest):
        return "自检";
      case (CommandAllType.setPatchClock):
        return "设置时钟";
      case (CommandAllType.startOTA):
        return "发送OTA命令";
      case (CommandAllType.switchMode):
        return "切换工作模式";
      case (CommandAllType.startSampling):
        return "开启采样";
      case (CommandAllType.stopSampling):
        return "停止采样";
      case (CommandAllType.shutdown):
        return "关机";
      case (CommandAllType.setUserInfoToFlash):
        return "设置用户信息到闪存";
      case (CommandAllType.eraseUserInfoFromFlash):
        return "擦除闪存的用户信息";
      case (CommandAllType.readUserInfoFromFlash):
        return "读取闪存的用户信息";
      case (CommandAllType.readSnFromPatch):
        return "读取设备的序列号";
      case (CommandAllType.readPatchVersion):
        return "读取设备版本信息";
      case (CommandAllType.checkPatchStatus):
        return "读取设备状态信息";
      case (CommandAllType.readDeviceInfo):
        return "读取设备信息";
      case (CommandAllType.sendDataAck):
        return "发送数据ACK";
      case (CommandAllType.sendHeartBeat):
        return "发送心跳包";
      case (CommandAllType.closeBaselineAlgorithm):
        return "关闭基线漂移算法";
      case (CommandAllType.openBaselineAlgorithm):
        return "开启基线漂移算法";
      case (CommandAllType.closeRTSSend):
        return "关闭实时数据发送";
      case (CommandAllType.openRTSSend):
        return "开启实时数据发送";
      case (CommandAllType.readSignature):
        return "读取设备签名";
      case (CommandAllType.writeRFToPath):
        return "写入RF数值";
      case (CommandAllType.writeSnToPath):
        return "写入设备序列号";
      case (CommandAllType.writeHWVersion):
        return "写入设备硬件版本号";
      case (CommandAllType.writeSecurityKey):
        return "写入安全密钥";
      case (CommandAllType.readSecurityKey):
        return "读取安全密钥";
      case (CommandAllType.switchSamplingMode):
        return "切换采样频率";
      case (CommandAllType.startAccCalibration):
        return "开启ACC校准";
      case (CommandAllType.stopAccCalibration):
        return "关闭ACC校准";
      case (CommandAllType.getAccCalibrationOffset):
        return "获取ACC校准偏移";
      case (CommandAllType.setAccCalibrationOffset):
        return "设置ACC校准偏移";
      case (CommandAllType.openFlashSave):
        return "打开实时数据存储Flash";
      case (CommandAllType.closeFlashSave):
        return "停止实时数据存储Flash";
      case (CommandAllType.eraseOverFlash):
        return "擦除Flash数据溢出丢失统计";
      case (CommandAllType.readOverFlash):
        return "读取Flash数据溢出丢失统计";
      case (CommandAllType.readPatchClock):
        return "读取设备当前时钟";
      case (CommandAllType.writeProductionLineInfo):
        return "写入产线信息";
      default:
        String ret = "";
        if (model == DeviceModel.Checkme_O2) {
          ret = CommandType.getTypeName(type);
        } else {
          ret = CommandAllType.getTypeName(type);
        }
        return ret;
    }

  }

  @Override
  public String getConnectErrorMeesage(Device device, int code, String msg) {
    return "连接错误: 错误码 = " + code + ", 错误信息 = " + msg + "\n设备 = " + device;
  }

  @Override
  public String getDisconnectedMeesage(Device device, boolean isForce) {
    return "断开连接: 是否主动 = " + isForce + "\n设备 = " + device;
  }

  @Override
  public String getConnectedMeesage(Device device) {
    return "连接成功: " + "\n设备 = " + device;
  }

}
