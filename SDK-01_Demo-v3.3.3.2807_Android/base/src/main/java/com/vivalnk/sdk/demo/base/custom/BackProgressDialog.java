package com.vivalnk.sdk.demo.base.custom;

import android.app.Activity;
import android.app.ProgressDialog;
import com.vivalnk.sdk.common.utils.log.LogUtils;
import com.vivalnk.sdk.common.utils.log.VitalLog;

/**
 * Created by JakeMo on 18-8-9.
 */
public class BackProgressDialog extends ProgressDialog {

  private static final String TAG = "BackProgressDialog";
  private Activity mActivity;

  public BackProgressDialog(Activity activity) {
    super(activity);
    this.mActivity = activity;
  }

  public BackProgressDialog(Activity activity, int theme) {
    super(activity, theme);
    this.mActivity = activity;
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    if (isShowing()) {
      dismiss();
    }
  }

  @Override
  public void show() {
    if (mActivity.isFinishing() || mActivity.isDestroyed()) {
      VitalLog.d(TAG, "activity is not running");
      VitalLog.d(TAG, LogUtils.getStackTraceString(new Throwable()));
      return;
    }
    super.show();
  }
}
