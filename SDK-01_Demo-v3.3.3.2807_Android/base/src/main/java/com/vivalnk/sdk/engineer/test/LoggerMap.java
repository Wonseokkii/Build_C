package com.vivalnk.sdk.engineer.test;

import com.vivalnk.sdk.model.Device;
import java.util.HashMap;
import java.util.Map;

public class LoggerMap {
  private Map<Device, AbsLogger> loggerMap;
  public LoggerMap() {
    loggerMap = new HashMap<>();
  }

  public void putBatteryLogger(Device device, AbsLogger logger) {
    loggerMap.put(device, logger);
  }

  public AbsLogger getLooger(Device device) {
    return loggerMap.get(device);
  }

  public void removeLogger(Device device) {
    loggerMap.remove(device);
  }

  public void startLogger(Device device) {
    AbsLogger logger = loggerMap.get(device);
    if(logger != null){
      logger.start();
    }
  }

  public boolean isLoggerOpen(Device device) {
    AbsLogger logger = loggerMap.get(device);
    if (logger != null) {
      return logger.isStarted();
    } else {
      return false;
    }
  }

  public void stopLogger(Device device) {
    AbsLogger logger = loggerMap.get(device);
    if (null != logger) {
      logger.stop();
    }
  }

  public void destroy() {
    for (Map.Entry<Device, AbsLogger> loggerEntry : loggerMap.entrySet()) {
      AbsLogger logger = loggerMap.remove(loggerEntry.getKey());
      if (logger != null) {
        logger.stop();
      }
    }
  }

}
