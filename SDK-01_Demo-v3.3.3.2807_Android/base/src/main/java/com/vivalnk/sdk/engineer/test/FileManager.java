package com.vivalnk.sdk.engineer.test;

import android.text.TextUtils;
import com.vivalnk.sdk.common.utils.FileUtils;
import java.io.File;
import java.text.SimpleDateFormat;

public class FileManager {

  public static final String ROOT_PATH = "/sdcard/VivaLNK/vSDK";

  public static final String DATA_FILE = "data.txt";
  public static final String DATA_FILE_FullMode_RTS = "data_full_mode_rts.txt";
  public static final String RAW_DATA_FILE = "raw_data.txt";
  public static final String BATTERY_FILE = "battery.txt";

  public static String getFileDataPath(String sn, String fileName) {
    if (TextUtils.isEmpty(sn)) {
      return ROOT_PATH + "/" + fileName;
    }

    sn = fixSN(sn);

    return ROOT_PATH + "/" + sn + "/" + fileName;
  }

  public static String getDataFilePath(String sn) {
    if (TextUtils.isEmpty(sn)) {
      return ROOT_PATH + "/" + DATA_FILE;
    }

    sn = fixSN(sn);

    return ROOT_PATH + "/" + sn + "/" + DATA_FILE;
  }

  public static String getDataFilePath_FullMode_RTS(String sn) {
    if (TextUtils.isEmpty(sn)) {
      return ROOT_PATH + "/" + DATA_FILE_FullMode_RTS;
    }

    sn = fixSN(sn);

    return ROOT_PATH + "/" + sn + "/" + DATA_FILE_FullMode_RTS;
  }

  public static String getRawDataFilePath(String sn) {
    if (TextUtils.isEmpty(sn)) {
      return ROOT_PATH + "/" + RAW_DATA_FILE;
    }

    sn = fixSN(sn);

    return ROOT_PATH + "/" + sn + "/" + RAW_DATA_FILE;
  }

  public static String getBatteryFilePath(String sn) {
    if (TextUtils.isEmpty(sn)) {
      return ROOT_PATH + "/" + BATTERY_FILE;
    }

    sn = fixSN(sn);

    return ROOT_PATH + "/" + sn + "/" + BATTERY_FILE;
  }

  public static String getFWBatteryFilePath(String sn) {
    if (TextUtils.isEmpty(sn)) {
      return ROOT_PATH + "/" + BATTERY_FILE;
    }

    sn = fixSN(sn);

    return ROOT_PATH + "/" + sn + "/" + "FW_" + BATTERY_FILE;
  }

  public static String getFBFilePath(String sn, String fileName, long timeStamp) {
    if (TextUtils.isEmpty(sn)) {
      return ROOT_PATH + "/" + fileName;
    }

    sn = fixSN(sn);

    File file = new File(ROOT_PATH + "/" + sn + "/" + fileName);
    if (!file.exists()) {
      SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm.ss");
      try {
        file.createNewFile();
        FileUtils.writeFile(file.getAbsolutePath(), "[HEADER]" + "\n");
        FileUtils.writeFile(file.getAbsolutePath(), "NOTES=" + "\n");
        FileUtils.writeFile(file.getAbsolutePath(), "STARTTIME=" + format.format(timeStamp) + "\n");
        FileUtils.writeFile(file.getAbsolutePath(), "[POINTS]" + "\n");
        FileUtils.writeFile(file.getAbsolutePath(), "[CUSTOM1]" + "\n");
      }catch (Exception e){
        e.printStackTrace();
      }

    }

    return file.getAbsolutePath();
  }


  public static void clearFiles(String sn) {

    sn = fixSN(sn);

    FileUtils.deleteFile(ROOT_PATH + "/" + sn);
  }

  public static String fixSN(String sn) {
    if (sn.contains("/")) {
      sn = sn.replace('/', '_');
    }
    return sn;
  }

}
