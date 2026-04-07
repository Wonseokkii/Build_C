package com.vivalnk.sdk.demo.base.eventbus;

import com.vivalnk.sdk.model.Device;

public class OTAEvent {

  public static final String ON_START = "ON_START";
  public static final String ON_PROGRESS_CHANGE = "ON_PROGRESS_CHANGE";
  public static final String ON_COMPLETE = "ON_COMPLETE";
  public static final String ON_CANCEL = "ON_CANCEL";
  public static final String ON_ERROR = "ON_ERROR";

  public OTAEvent(String tag, Device device) {
    this.tag = tag;
    this.device = device;
  }

  public OTAEvent(String tag, Device device, String msg) {
    this.tag = tag;
    this.device = device;
    this.msg = msg;
  }

  public OTAEvent(String tag, Device device, int percent) {
    this.tag = tag;
    this.device = device;
    this.percent = percent;
  }

  public OTAEvent(String tag, Device device, int code, String msg) {
    this.tag = tag;
    this.device = device;
    this.code = code;
    this.msg = msg;
  }

  public String tag;
  public Device device;
  public int percent;
  public int code;
  public String msg;
}
