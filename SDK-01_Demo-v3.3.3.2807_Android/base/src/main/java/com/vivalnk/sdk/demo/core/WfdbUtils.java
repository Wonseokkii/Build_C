package com.vivalnk.sdk.demo.core;

public class WfdbUtils {
  static {
    System.loadLibrary("wfdb");
  }

  public static native void initFile(String data, String hea);

  public static native void initSignalInfo(int frequency, int format, String desc, String units, int gain, int adcres, int adczero);

  public static native void open();

  public static native void setBaseTime(String time);

  public static native void doSample(int[] array);

  public static native void newHeader();

  public static native void close();

}
