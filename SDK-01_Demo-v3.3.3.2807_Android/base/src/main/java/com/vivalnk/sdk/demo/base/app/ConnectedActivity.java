package com.vivalnk.sdk.demo.base.app;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.CommandRequest;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.demo.base.i18n.ErrorMessageHandler;
import com.vivalnk.sdk.demo.base.mvp.IPresenter;
import com.vivalnk.sdk.demo.repository.device.ConnectEvent;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceModel;

import java.util.Map;
import java.util.Objects;

public abstract class ConnectedActivity<P extends IPresenter> extends BaseToolbarActivity<P> {

  protected Device mDevice;

  protected boolean isOTAing;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    toolbar.setNavigationOnClickListener((v) -> finish());
    initDevice();
    onDeviceInitialized(savedInstanceState);
  }

  protected void onDeviceInitialized(Bundle savedInstanceState) {

  }

  @Override
  protected void onResume() {
    super.onResume();
    try {
      if (mDevice == null || !VitalClient.getInstance().isConnected(mDevice)) {
        finish();
      }
    } catch (Exception e) {
      Log.e("ConnectedActivity", Objects.requireNonNull(e.getMessage()));
    }

  }

  @Subscribe
  public void onConnectEvent(ConnectEvent connectEvent) {
    if (connectEvent == null) {
      return;
    }

    if (!connectEvent.device.equals(mDevice)) {
      return;
    }

    if (ConnectEvent.ON_DISCONNECTED.equalsIgnoreCase(connectEvent.event)
        || ConnectEvent.ON_ERROR.equalsIgnoreCase(connectEvent.event)) {
      if (!isOTAing && isForeground()) {
        finish();
      }
    } else if (connectEvent.event.equals(ConnectEvent.ON_DEVICE_READY)) {
      Log.v(TAG, "device connect success");
    }
  }

  protected CommandRequest getCommandRequest(int type, int timeout) {
    CommandRequest request = new CommandRequest.Builder()
        .setTimeout(timeout)
        .setType(type)
        .build();
    return request;
  }

  protected CommandRequest getCommandRequest(int type, int timeout, String key,
      Object param) {
    CommandRequest request = new CommandRequest.Builder()
        .setTimeout(timeout)
        .setType(type)
        .addParam(key, param)
        .build();
    return request;
  }

  public void execute(final int type) {
    CommandRequest request = getCommandRequest(type, 3000);
    execute(request, null, true, true, true);
  }

  public void execute(final int type, Callback callback) {
    CommandRequest request = getCommandRequest(type, 3000);
    execute(request, callback, true, false, true);
  }

  public void execute(final CommandRequest request) {
    execute(request, null, true, true, true);
  }

  public void execute(CommandRequest request, Callback callback) {
    execute(request, callback, true, false, true);
  }

  public void execute(CommandRequest request, Callback callback, boolean showProgressDialog, boolean showSuccessDialog, boolean showErrorDialog) {
    DeviceManager.getInstance().execute(mDevice, request, new Callback() {
      @Override
      public void onStart() {
        if (showProgressDialog) {
          showProgressDialog(ErrorMessageHandler.getInstance().getOnStartMessage(mDevice.getModel(), request.getType()));
        }
        if (null != callback) {
          callback.onStart();
        }
      }

      @Override
      public void onComplete(Map<String, Object> data) {
        dismissProgressDialog();
        if (showSuccessDialog) {
          showAlertDialog(request.getTypeName(), ErrorMessageHandler.getInstance().getOnCompleteMessage(mDevice.getModel(), request.getType(), data));
        }
        if (null != callback) {
          callback.onComplete(data);
        }
      }

      @Override
      public void onError(int code, String msg) {
        dismissProgressDialog();
        if (showErrorDialog) {
          showAlertDialog(request.getTypeName(), ErrorMessageHandler.getInstance().getOnErrorMesage(mDevice.getModel(), request.getType(), code, msg));
        }
        if (null != callback) {
          callback.onError(code, msg);
        }
      }
    });
  }

  protected void initDevice() {
    try {
      mDevice = (Device) getIntent().getExtras().getSerializable("device");
      String deviceName = mDevice.getName();
      if (mDevice.getModel() == DeviceModel.Checkme_O2) {
        deviceName = deviceName + "(" + mDevice.getSn().substring(mDevice.getSn().length() - 4, mDevice.getSn().length()) + ")";
      }
      setTitle(deviceName);
    } catch (Exception e) {
      this.finish();
    }
  }

  protected void navToConnectedActivity(Device device, Class clazz) {
    Bundle extras = new Bundle();
    extras.putSerializable("device", device);
    navTo(this, extras, clazz);
  }
}
