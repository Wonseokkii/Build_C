package com.vivalnk.sdk.demo.base.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import androidx.core.content.ContextCompat;
import android.widget.TextView;
import java.lang.reflect.Field;

/**
 * Created by JakeMo on 18-7-13.
 */
public class DialogUtils {


  public static void showConfirmDilog(Activity activity, String msg,
      OnClickListener okListener, OnClickListener cancelListener) {
    showConfirmDilog(activity, msg, okListener, cancelListener, false);
  }

  public static void showConfirmDilog(Activity activity, String msg,
      OnClickListener okListener, OnClickListener cancelListener, boolean cancelable) {
    showConfirmDilog(activity, msg, okListener, cancelListener, cancelable, null, null);
  }

  public static void showConfirmDilog(Activity activity, String msg,
      final OnClickListener okListener, final OnClickListener cancelListener, boolean cancelable,
      Integer colorMsg, Integer colorTitle) {
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
    alertDialog.setMessage(msg);
    alertDialog.setCancelable(cancelable);
    alertDialog.setPositiveButton("OK", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        if (null != okListener) {
          okListener.onClick(dialog, which);
        }
      }
    });
    alertDialog.setNegativeButton("Cancel", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        if (null != cancelListener) {
          cancelListener.onClick(dialog, which);
        }
      }
    });
    AlertDialog dialog = alertDialog.create();
    if (!activity.isFinishing()) {
      dialog.show();
      changeStyle(dialog, colorMsg, colorTitle);
    }

  }

  private static void changeStyle(AlertDialog dialog, Integer colorMsg, Integer colorTitle) {

    try {
      Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
      mAlert.setAccessible(true);
      Object mController = mAlert.get(dialog);

      // message
      Field mMessage = mController.getClass().getDeclaredField("mMessageView");
      mMessage.setAccessible(true);
      TextView mMessageView = (TextView) mMessage.get(mController);
      if (colorMsg != null) {
        mMessageView.setTextColor(ContextCompat.getColor(dialog.getContext(), colorMsg));//message样式修改成红色
      }

      // title
      Field mTitle = mController.getClass().getDeclaredField("mTitle");
      mMessage.setAccessible(true);
      TextView mTitleView = (TextView) mMessage.get(mController);
      if (colorTitle != null) {
        mMessageView.setTextColor(ContextCompat.getColor(dialog.getContext(), colorTitle));//title样式修改成``色
      }

    } catch (Exception e) {

    }


  }


}
