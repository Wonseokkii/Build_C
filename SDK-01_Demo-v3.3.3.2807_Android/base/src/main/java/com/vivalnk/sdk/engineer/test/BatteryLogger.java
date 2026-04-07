package com.vivalnk.sdk.engineer.test;

import android.Manifest.permission;
import android.content.Context;
import android.util.Log;
import android.content.Context;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.CommandRequest;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.command.base.CommandType;
import com.vivalnk.sdk.common.utils.FileUtils;
import com.vivalnk.sdk.common.utils.PermissionHelper;
import com.vivalnk.sdk.common.utils.StringUtils;
import com.vivalnk.sdk.demo.base.utils.PermissionUtils;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.PatchStatusInfo;
import com.vivalnk.sdk.utils.DateFormat;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.DateFormatUtils;

public class BatteryLogger extends AbsLogger {

  private static final String TAG = "BatteryLogger";
  Disposable disposable;

  public BatteryLogger(Device device) {
    super(device);
  }

  CommandRequest checkPatchStatusReq = new CommandRequest.Builder()
      .setType(CommandType.checkPatchStatus)
      .setLoggable(false)
      .build();

  private void requestStatus() {

    VitalClient.getInstance().execute(mDevice, checkPatchStatusReq, new Callback() {
      @Override
      public void onStart() {

      }

      @Override
      public void onComplete(Map<String, Object> map) {
        if (map != null && map.get("data") != null && (map
            .get("data") instanceof PatchStatusInfo)) {
          PatchStatusInfo info = (PatchStatusInfo) map.get("data");
          readRemoteRSSI(info);
        }
      }

      @Override
      public void onError(int i, String s) {

      }
    });

  }

  private void readRemoteRSSI(final PatchStatusInfo info) {

    VitalClient.getInstance().readRemoteRssi(mDevice, new Callback() {
      @Override
      public void onStart() {

      }

      @Override
      public void onComplete(Map<String, Object> map) {
        if (null != map && map.get("rssi") != null && (map.get("rssi") instanceof Integer)) {

          final Integer rssi = (Integer) map.get("rssi");

          final String date = DateFormatUtils.format(new Date(), DateFormat.sPattern);

          printLog(mDevice, rssi, date, info);

        }
      }

      @Override
      public void onError(int i, String s) {

      }
    }, false);
  }

  private void printLog(Device mConnectedDevice, Integer rssi, String date, PatchStatusInfo info) {

    if (StringUtils.isEmpty(mConnectedDevice.getSn())) {
      return;
    }

    String log = "date=" + date
        + ", mac=" + mConnectedDevice.getId()
        + ", name=" + mConnectedDevice.getName()
        + ", rssi=" + rssi
        + ", " + info.toString()
        + "\n";
    try {
      FileUtils.writeFile(FileManager.getBatteryFilePath(mConnectedDevice.getSn()), log, true);
    } catch (Throwable throwable) {

    }
  }

  @Override
  public void start() {
    if (disposable == null) {
      disposable = Observable.interval(10, TimeUnit.SECONDS)
          .observeOn(Schedulers.io())
          .subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
              requestStatus();
            }
          });
    }
  }

  @Override
  public void stop() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
      disposable = null;
    }
  }

  @Override
  public boolean isStarted() {
    return disposable != null && !disposable.isDisposed();
  }
}
