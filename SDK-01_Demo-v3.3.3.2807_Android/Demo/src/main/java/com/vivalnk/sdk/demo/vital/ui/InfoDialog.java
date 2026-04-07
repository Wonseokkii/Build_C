package com.vivalnk.sdk.demo.vital.ui;

import static com.vivalnk.sdk.dataparser.battery.Battery.VL_1;
import static com.vivalnk.sdk.dataparser.battery.Battery.VL_2;
import static com.vivalnk.sdk.dataparser.battery.Battery.VL_3;
import static com.vivalnk.sdk.dataparser.battery.Battery.VL_4;
import static com.vivalnk.sdk.dataparser.battery.Battery.VL_5;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.device.vv330.DataStreamMode;
import com.vivalnk.sdk.device.vv330.VV330Manager;
import com.vivalnk.sdk.model.BatteryInfo;
import com.vivalnk.sdk.model.BatteryInfo.ChargeStatus;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceInfoUtils;
import com.vivalnk.sdk.model.PatchStatusInfo;

/**
 * 电量信息显示
 *
 * @author Aslan
 * @date 2019/3/26
 */
public class InfoDialog extends DialogFragment {

  public static final String TAG = "InfoDialog";

  private static final String TAG_PATCH_INFO = "patchInfo";
  private static final String TAG_DEVICE = "device";
  private static final String TAG_BATTERY = "batteryInfo";

  private Device mDevice;
  private PatchStatusInfo patchStatusInfo;
  private BatteryInfo batteryInfo;

  //设备信息
  @BindView(R.id.ll1)
  public LinearLayout ll1;

  @BindView(R.id.tvSampling)
  public TextView tvSampling;

  @BindView(R.id.tvLeanOn)
  public TextView tvLeanOn;

  @BindView(R.id.tvBaseLine)
  public TextView tvBaseLine;


  @BindView(R.id.tvMode)
  public TextView tvMode;

  @BindView(R.id.tvDataStreamMode)
  public TextView tvDataStreamMode;

  //实时数据相关开关
  @BindView(R.id.tvRTSDataSend)
  public TextView tvRTSDataSend;
  @BindView(R.id.tvRTSDataSaveToFlash)
  public TextView tvRTSDataSaveToFlash;

  //330_1 feature
  @BindView(R.id.tv_data_ecg_frequency)
  public TextView tv_data_ecg_frequency;
  @BindView(R.id.tv_data_acc_frequency)
  public TextView tv_data_acc_frequency;
  @BindView(R.id.tv_data_sampling_multiple)
  public TextView tv_data_sampling_multiple;

  @BindView(R.id.tv_rts_enable)
  public TextView tvRTSChannelEnable;
  @BindView(R.id.tv_history_enable)
  public TextView tv_flashChannelEnable;

  @BindView(R.id.tv_leadOffAccEnable)
  public TextView tv_leadOffAccEnable;
  @BindView(R.id.tv_accSamplingEnable)
  public TextView tv_accSamplingEnable;

  //电池信息
  @BindView(R.id.ll2)
  public LinearLayout ll2;

  @BindView(R.id.tvStatus)
  public TextView tvStatus;

  @BindView(R.id.tvLevel)
  public TextView tvLevel;

  @BindView(R.id.tvCanOta)
  public TextView tvCanOta;

  @BindView(R.id.tvCanSampling)
  public TextView tvCanSampling;

  @BindView(R.id.tvCanBleTransmission)
  public TextView tvCanBleTransmission;

  @BindView(R.id.tvVoltage)
  public TextView tvVoltage;

  @BindView(R.id.tvPercent)
  public TextView tvPercent;

  @BindView(R.id.tvTemperature)
  public TextView tvTemperature;

  @BindView(R.id.btOk)
  public Button btOk;

  VV330Manager vv330Manager;

  public static InfoDialog newInstance(Device device, PatchStatusInfo patchStatusInfo) {
    Bundle args = new Bundle();
    args.putSerializable(TAG_PATCH_INFO, patchStatusInfo);
    args.putSerializable(TAG_DEVICE, device);
    InfoDialog fragment = new InfoDialog();
    fragment.setArguments(args);
    return fragment;
  }

  public static InfoDialog newInstance(Device device, BatteryInfo batteryInfo) {
    Bundle args = new Bundle();
    args.putSerializable(TAG_BATTERY, batteryInfo);
    args.putSerializable(TAG_DEVICE, device);
    InfoDialog fragment = new InfoDialog();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    assert (args != null);

    mDevice = (Device) args.getSerializable(TAG_DEVICE);
    if(DeviceInfoUtils.isVVEcgDevice(mDevice)) {
      vv330Manager = new VV330Manager(mDevice);
    }

    patchStatusInfo = (PatchStatusInfo) args.getSerializable(TAG_PATCH_INFO);
    if (patchStatusInfo == null) {
      batteryInfo = (BatteryInfo) args.getSerializable(TAG_BATTERY);
    } else {
      batteryInfo = patchStatusInfo.batteryInfo;
    }
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    View view = LayoutInflater.from(getContext()).inflate(R.layout.content_patch_status_info, null);
    ButterKnife.bind(this, view);

    iniData();
    Builder builder = new Builder(getContext())
        .setTitle(R.string.info)
        .setView(view);
    return builder.create();
  }

  private String formatBatteryStatus(Context context, ChargeStatus status) {
    if (status == ChargeStatus.INCHARGING_NOT_COMPLETE) {
      return context.getString(R.string.battery_info_status_charger_connected_not_complete);
    } else if (status == ChargeStatus.INCHARGING_COMPLETE) {
      return context.getString(R.string.battery_info_status_charger_connected_complete);
    } else if (status == ChargeStatus.NOT_INCHARGING) {
      return context.getString(R.string.battery_info_status_charger_not_connected);
    } else if (status == ChargeStatus.INCHARGING_ABNORMAL) {
      return context.getString(R.string.battery_info_status_charger_connected_abnormal);
    } else if (status == ChargeStatus.INCHARGING_STOPPED) {
      return context.getString(R.string.battery_info_status_charger_connected_stopped);
    } else if (status == ChargeStatus.INCHARGING_NOT_STARTED) {
      return context.getString(R.string.battery_info_status_charger_connected_not_started);
    } else {
      return context.getString(R.string.battery_info_status_unknown);
    }
  }

  private String forLevel(int level) {
    String ret = "";
    switch (level) {
      case VL_1:
        ret = "V3≤V";
        break;
      case VL_2:
        ret = "V2≤V<V3";
        break;
      case VL_3:
        ret = "V1≤V<V2";
        break;
      case VL_4:
        ret = "V0≤V<V1";
        break;
      case VL_5:
        ret = "V≤V0";
        break;
      default:
        break;
    }
    return ret;
  }

  private void iniData() {
    if (patchStatusInfo != null) {
      ll1.setVisibility(View.VISIBLE);
      tvSampling.setText(getString(R.string.patch_info_sampling,
          patchStatusInfo.sampling ? getString(R.string.yes) : getString(R.string.no)));
      tvLeanOn.setText(getString(R.string.patch_info_leadOn,
          patchStatusInfo.leadOn ? getString(R.string.yes) : getString(R.string.no)));

      //vv310: sample mode
      if (patchStatusInfo.baseLineAlgoOpen != null) {
        tvBaseLine.setVisibility(View.VISIBLE);
        tvBaseLine.setText(getString(R.string.patch_info_baselinealgoopen, patchStatusInfo.baseLineAlgoOpen == null ? null
            : (patchStatusInfo.baseLineAlgoOpen ? getString(R.string.yes) : getString(R.string.no))));
      } else {
        tvBaseLine.setVisibility(View.GONE);
      }

      tv_data_ecg_frequency.setText(getString(R.string.patch_info_ecg_frequency, patchStatusInfo.getEcgSampleFrequency() + ""));
      tv_data_acc_frequency.setText(getString(R.string.patch_info_acc_frequency, patchStatusInfo.getAccSampleFrequency() + ""));
      tv_data_sampling_multiple.setText(getString(R.string.patch_info_sampling_multiple, patchStatusInfo.getSamplingMultiple() + ""));

      //VV330_1: leadOffAccEnable status
      if (patchStatusInfo.getExtra(PatchStatusInfo.Key.leadOffAccEnable) != null) {
        boolean leadOffAccEnable = patchStatusInfo.getExtra(PatchStatusInfo.Key.leadOffAccEnable);
        tv_leadOffAccEnable.setVisibility(View.VISIBLE);
        tv_leadOffAccEnable.setText(getString(R.string.patch_info_leadOffAccEnable, leadOffAccEnable ? getString(R.string.yes) : getString(R.string.no)));
      } else {
        tv_leadOffAccEnable.setVisibility(View.GONE);
      }
      //VV330_1: accSamplingEnable status
      if (patchStatusInfo.getExtra(PatchStatusInfo.Key.accSamplingEnable) != null && DeviceInfoUtils.isVV330_1(mDevice)) {
        boolean accSamplingEnable = patchStatusInfo.getExtra(PatchStatusInfo.Key.accSamplingEnable);
        tv_accSamplingEnable.setVisibility(View.VISIBLE);
        tv_accSamplingEnable.setText(getString(R.string.patch_info_accSamplingEnable, accSamplingEnable ? getString(R.string.yes) : getString(R.string.no)));
      } else {
        tv_accSamplingEnable.setVisibility(View.GONE);
      }

      //vv310: sample mode
      if (patchStatusInfo.mode != null) {
        tvMode.setVisibility(View.VISIBLE);
        tvMode.setText(getString(R.string.patch_info_mode, patchStatusInfo.mode));
      } else {
        tvMode.setVisibility(View.GONE);
      }

      //vv310: sample mode
      if (vv330Manager != null && vv330Manager.getDataStreamMode() != null) {
        tvDataStreamMode.setVisibility(View.VISIBLE);
        tvDataStreamMode.setText(getString(R.string.patch_data_stream_mode, vv330Manager.getDataStreamMode()));
      } else {
        tvDataStreamMode.setVisibility(View.GONE);
      }

      //vv330: rts data send switcher
      if (patchStatusInfo.RTSDataSend != null && DeviceInfoUtils.isVV330(mDevice)) {
        tvRTSDataSend.setVisibility(View.VISIBLE);
        tvRTSDataSend.setText(getString(R.string.patch_info_rts_data_send, (patchStatusInfo.RTSDataSend ? getString(R.string.yes) : getString(R.string.no))));
      } else {
        tvRTSDataSend.setVisibility(View.GONE);
      }

      //vv330: rts data save to flash
      if (patchStatusInfo.RTSDataSaveToFlash != null && DeviceInfoUtils.isVVEcgDevice(mDevice)) {
        tvRTSDataSaveToFlash.setVisibility(View.VISIBLE);
        tvRTSDataSaveToFlash.setText(getString(R.string.patch_info_rts_data_save_to_flash, (patchStatusInfo.RTSDataSaveToFlash ? getString(R.string.yes) : getString(R.string.no))));
      } else {
        tvRTSDataSaveToFlash.setVisibility(View.GONE);
      }

      //vv330_1 rts channel enable/disable
      if (patchStatusInfo.RTSChannelEnable != null && DeviceInfoUtils.isVV330_1(mDevice)) {
        tvRTSChannelEnable.setVisibility(View.VISIBLE);
        tvRTSChannelEnable.setText(getString(R.string.patch_info_rts_channel_enable, (patchStatusInfo.RTSChannelEnable
            ? getString(R.string.yes) : getString(R.string.no))));
      } else {
        tvRTSChannelEnable.setVisibility(View.GONE);
      }
      //vv330_1 flash channel enable/disable
      if (patchStatusInfo.flashChannelEnable != null && DeviceInfoUtils.isVV330_1(mDevice)) {
        tv_flashChannelEnable.setVisibility(View.VISIBLE);
        tv_flashChannelEnable.setText(getString(R.string.patch_info_flash_channel_enable, (patchStatusInfo.flashChannelEnable
            ? getString(R.string.yes) : getString(R.string.no))));
      } else {
        tv_flashChannelEnable.setVisibility(View.GONE);
      }


    } else {
      ll1.setVisibility(View.GONE);
    }

    tvStatus.setText(getString(R.string.battery_info_status,
        formatBatteryStatus(getContext(), batteryInfo.getStatus())));
    tvLevel.setText(getString(R.string.battery_info_level, forLevel(batteryInfo.getLevel())));
    tvCanOta
        .setText(getString(R.string.battery_info_can_ota,
            batteryInfo.canOTA() ? getString(R.string.yes) : getString(R.string.no)));
    tvCanSampling.setText(
        getString(R.string.battery_info_can_sampling,
            batteryInfo.canSampling() ? getString(R.string.yes) : getString(R.string.no)));
    tvCanBleTransmission.setText(
        getString(R.string.battery_info_can_ble_transmission,
            batteryInfo.canBleTransmission() ? getString(R.string.yes) : getString(R.string.no)));
    tvVoltage.setText(
        getString(R.string.battery_info_voltage, String.valueOf(batteryInfo.getVoltage())));
    tvPercent.setText(
        getString(R.string.battery_info_percent, String.valueOf(batteryInfo.getPercent())));
    if (batteryInfo.getTemperature() == null) {
      tvTemperature.setVisibility(View.GONE);
    } else {
      tvTemperature.setText(
          getString(R.string.battery_info_temperature, String.valueOf(batteryInfo.getTemperature())));
    }
  }

  private void setPatchInfoText(TextView tv, Object obj, String text) {
    if (obj != null) {
      tv.setVisibility(View.VISIBLE);
      tv.setText(text);
    } else {
      tv.setVisibility(View.GONE);
    }
  }

  @OnClick(R.id.btOk)
  public void onOKClick() {
    this.dismiss();
  }
}
