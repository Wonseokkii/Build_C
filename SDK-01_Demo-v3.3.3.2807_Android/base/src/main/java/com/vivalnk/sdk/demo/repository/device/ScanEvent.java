package com.vivalnk.sdk.demo.repository.device;

import com.vivalnk.sdk.model.Device;

/**
 * Created by JakeMo on 18-4-30.
 */
public class ScanEvent {
  public static final String ON_START = "ON_START";
  public static final String ON_DEVICEFOUND = "ON_DEVICEFOUND";
  public static final String ON_STOP = "ON_STOP";
  public static final String ON_ERROR = "ON_ERROR";

  public Device device;
  public String event;
  public int code;
  public String msg;

  public static ScanEvent onStart() {
    ScanEvent
        event = new ScanEvent();
    event.event = ON_START;
    return event;
  }

  public static ScanEvent onDeviceFound(Device device) {
    ScanEvent
        event = new ScanEvent();
    event.device = device;
    event.event = ON_DEVICEFOUND;
    return event;
  }

  public static ScanEvent onStop() {
    ScanEvent
        event = new ScanEvent();
    event.event = ON_STOP;
    return event;
  }

  public static ScanEvent onError(int code, String msg) {
    ScanEvent
        event = new ScanEvent();
    event.event = ON_ERROR;
    event.code = code;
    event.msg = msg;
    return event;
  }

  @Override
  public String toString() {
    return "ScanEvent{" +
        "device=" + device +
        ", event='" + event + '\'' +
        ", code=" + code +
        ", msg='" + msg + '\'' +
        '}';
  }
}
