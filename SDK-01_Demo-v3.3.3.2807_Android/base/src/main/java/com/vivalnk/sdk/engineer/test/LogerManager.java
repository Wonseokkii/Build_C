package com.vivalnk.sdk.engineer.test;

import com.vivalnk.sdk.demo.repository.device.ConnectEvent;
import com.vivalnk.sdk.common.eventbus.EventBus;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.data.stream.packagelost.DisContinuousEvent;
import com.vivalnk.sdk.model.Device;

public class LogerManager {

  private LoggerMap packageLoggerMap;
  private LoggerMap batteryLoggerMap;

  private AbsLogger dataLogger = new DataLogger();
  private AbsLogger fwBatteryLogger = new FWBatteryLogger();
  private AbsLogger fbLogger = new FBLogger();

  private static class SingletonHolder {
    private static final LogerManager
        INSTANCE = new LogerManager();
  }

  private LogerManager() {
    batteryLoggerMap = new LoggerMap();
    packageLoggerMap = new LoggerMap();
  }

  public static LogerManager getInstance() {
    return SingletonHolder.INSTANCE;
  }

  public void init() {
    DisContinuousEvent.openEvent();
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
      dataLogger.start();
      fwBatteryLogger.start();
      fbLogger.start();
    }
  }

  @Subscribe
  public void onConnectEvent(ConnectEvent connectEvent) {
    if (connectEvent == null) {
      return;
    }

    if (ConnectEvent.ON_START.equalsIgnoreCase(connectEvent.event)) {

    } else if (ConnectEvent.ON_CONNECTED.equalsIgnoreCase(connectEvent.event)) {

    } else if (ConnectEvent.ON_DEVICE_READY.equalsIgnoreCase(connectEvent.event)) {
      startPackageLostLogger(connectEvent.device);
      startBatteryLogger(connectEvent.device);
    } else if (ConnectEvent.ON_DISCONNECTED.equalsIgnoreCase(connectEvent.event)) {
      stopBatteryLogger(connectEvent.device);
      stopPackageLostLogger(connectEvent.device);
    } else if (ConnectEvent.ON_ERROR.equalsIgnoreCase(connectEvent.event)) {
      stopBatteryLogger(connectEvent.device);
      stopPackageLostLogger(connectEvent.device);
    }
  }

  private void startPackageLostLogger(Device device) {
    AbsLogger logger = packageLoggerMap.getLooger(device);
    if(logger == null){
      logger = new PackageLostLogger(device);
      packageLoggerMap.putBatteryLogger(device, logger);
    }
    if (!logger.isStarted()) {
      logger.start();
    }
  }
  private void stopPackageLostLogger(Device device) {
    packageLoggerMap.stopLogger(device);
  }

  public PackageLostLogger getPackageLostLogger(Device device) {
    return (PackageLostLogger) packageLoggerMap.getLooger(device);
  }

  public void putBatteryLogger(Device device, AbsLogger logger) {
    batteryLoggerMap.putBatteryLogger(device, logger);
  }
  public void removeBatteryLogger(Device device) {
    batteryLoggerMap.removeLogger(device);
  }
  public void startBatteryLogger(Device device) {
    batteryLoggerMap.startLogger(device);
  }
  public boolean isBatteryLoggerOpen(Device device) {
    return batteryLoggerMap.isLoggerOpen(device);
  }
  public void stopBatteryLogger(Device device) {
    batteryLoggerMap.startLogger(device);
  }

  public void destroy() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
      //stop battery logger
      batteryLoggerMap.destroy();
      packageLoggerMap.destroy();
      dataLogger.stop();
      fwBatteryLogger.stop();
      fbLogger.stop();
    }
  }

}
