package com.vivalnk.sdk.engineer.test;

import android.Manifest.permission;
import android.content.Context;
import android.util.Log;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.PermissionHelper;
import com.vivalnk.sdk.demo.base.app.BaseApplication;
import com.vivalnk.sdk.demo.base.utils.SPUtils;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import android.content.Context;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.utils.FileUtils;
import com.vivalnk.sdk.demo.base.utils.PermissionUtils;
import com.vivalnk.sdk.demo.base.utils.SPUtils;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.SampleData;
import com.vivalnk.sdk.model.SampleData.DataKey;
import java.util.Map;

public class FBLogger extends AbsLogger {

  private static final String TAG = "FBLogger";

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onDataEvent(final SampleData data) {
    if (data == null) {
      return;
    }

    String sn = data.getDeviceSN();

    if (!(Boolean)SPUtils.get(sn + "fbTest", false)){
      return;
    }
    String logFileName = SPUtils.get(sn + "fbTestFile", "");

    postIO(new Runnable() {
      @Override
      public void run() {
        SampleData sampleData = data;
        int[] rri = sampleData.getData(DataKey.rri);
        long timeStamp = sampleData.getData(DataKey.time);
        if (rri != null) {
          for (int i = 0;i<rri.length; i++) {
            if(rri[i] > 0) {
              FileUtils.writeFile(FileManager.getFBFilePath(sn, logFileName, timeStamp), String.valueOf(rri[i]) + "\n");
            }
          }
        }
      }
    });


  }
}
