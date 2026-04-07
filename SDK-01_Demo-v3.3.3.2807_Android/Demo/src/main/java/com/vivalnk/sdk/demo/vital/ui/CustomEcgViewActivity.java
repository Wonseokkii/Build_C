package com.vivalnk.sdk.demo.vital.ui;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import butterknife.BindView;
import butterknife.OnClick;

import com.github.gzuliyujiang.colorpicker.ColorPicker;
import com.github.gzuliyujiang.colorpicker.OnColorPickedListener;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.widget.CustomEcgView;

public class CustomEcgViewActivity extends ConnectedActivity {

  private static final String TAG = "CustomEcgViewActivity";

  @BindView(R.id.rtsEcgView)
  CustomEcgView rtsEcgView;

  @BindView(R.id.btnSwitchGain)
  Button btnSwitchGain;

  @BindView(R.id.btnRevert)
  Button btnRevert;

  private volatile boolean revert;

  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    btnRevert.setText(revert ? "De-Revert" : "Revert");
    rtsEcgView.setup(mDevice);
  }

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.activity_custom_ecg_view);
  }


  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    rtsEcgView.destroy();
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
  }

  @OnClick(R.id.btnRevert)
  public void clickBtnRevert() {
    revert = !revert;
    rtsEcgView.revert(revert);
    btnRevert.setText(revert ? "De-Revert" : "Revert");
  }

  @OnClick(R.id.btnSwitchGain)
  public void clickSwitchGain() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Switch ECG Gain")
        .setItems(R.array.ecg_gains, (dialog, which) -> {
          int gain;
          if (which == 0) {
            gain = 5;
          } else if (which == 1) {
            gain = 10;
          } else {
            gain = 20;
          }
          rtsEcgView.switchGain(gain);
        });
    builder.create().show();
  }

  @OnClick(R.id.btnSetGridColor)
  public void btnSetGridColor(View v) {
      ColorPicker picker = new ColorPicker(this);
      picker.setInitColor(0xFF0000);
      picker.setOnColorPickListener(new OnColorPickedListener() {
          @Override
          public void onColorPicked(int pickedColor) {
              rtsEcgView.setGridColor(pickedColor);
          }
      });
      picker.show();
  }

  @OnClick(R.id.btnSetWaveformStrokeColor)
  public void btnSetWaveformStrokeColor(View v) {
      ColorPicker picker = new ColorPicker(this);
      picker.setInitColor(0xFF0000);
      picker.setOnColorPickListener(new OnColorPickedListener() {
          @Override
          public void onColorPicked(int pickedColor) {
              rtsEcgView.setEcgWaveformStrokeColor(pickedColor);
          }
      });
      picker.show();
  }

  @OnClick(R.id.btnSetWaveformStrokeWidth)
  public void btnSetWaveformStrokeWidth() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Set Waveform Stroke Width")
        .setItems(R.array.ecg_wave_stroke_width, (dialog, which) -> {
          rtsEcgView.setEcgWaveformStrokeWidth(which + 1);
        });
    builder.create().show();
  }

  @OnClick(R.id.btnSetPaperSpeed)
  public void btnSetPaperSpeed() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Set ECG Paper Speed")
        .setItems(R.array.ecg_paper_speed, (dialog, which) -> {
          /**
           *     <item>12.5 mm/s</item>
           *     <item>25 mm/s</item>
           *     <item>50 mm/s</item>
           *     <item>100 mm/s</item>
           */
          float speed = 25;
          if (which == 0) {
            speed = 12.5f;
          } else if (which == 1) {
            speed = 25;
          } else if(which == 2){
            speed = 50;
          } else {
            speed = 100;
          }
          rtsEcgView.setPaperSpeed(speed);
        });
    builder.create().show();
  }

}
