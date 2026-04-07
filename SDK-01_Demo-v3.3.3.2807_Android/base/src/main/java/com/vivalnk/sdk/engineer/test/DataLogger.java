package com.vivalnk.sdk.engineer.test;

import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.FileUtils;
import com.vivalnk.sdk.common.utils.log.LogLevel;
import com.vivalnk.sdk.device.vv330.DataStreamMode;
import com.vivalnk.sdk.model.SampleData;
import com.vivalnk.sdk.model.common.DataType;
import com.vivalnk.sdk.utils.DateFormat;
import org.apache.commons.lang3.time.DateFormatUtils;

public class DataLogger extends AbsLogger {

  private static final String TAG = "DataLogger";

  public static boolean isLogData = true;

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onDataEvent(final SampleData sampleData) {
    if (sampleData == null || !isLogData) {
      return;
    }

    DataStreamMode mode = sampleData.getData(DataType.DataKey.dataStreamMode);
    long timeStamp = System.currentTimeMillis();
    String tag = "DATA";
    LogLevel priority = LogLevel.INFO;
    String threadInfo = Thread.currentThread().toString();
    String message = sampleData.toFileString();
    StringBuffer sb = new StringBuffer();
    sb.setLength(0);
    sb.append("\n")
        .append(DateFormatUtils.format((timeStamp), DateFormat.sPattern))
        .append(" -- ").append("Log[").append(tag).append(",").append(priority.toString()).append("]")
        .append(" -- ").append(threadInfo)
        .append(" :")
        .append(message);

    if (mode != null && mode == DataStreamMode.FullDualMode) {
      //在FullMode模式下，只打印flash数据, rts数据仅用作图形展示
      if (sampleData.isFlash() == false) {
        FileUtils.writeFile(FileManager.getDataFilePath_FullMode_RTS(sampleData.getDeviceSN()), sb.toString());
        return;
      }
    }
    FileUtils.writeFile(FileManager.getDataFilePath(sampleData.getDeviceSN()), sb.toString());

  }

}
