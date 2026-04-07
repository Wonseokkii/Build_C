package com.vivalnk.sdk.demo.base.utils;

import android.app.Activity;
import android.os.Build;
import androidx.annotation.ColorRes;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by JakeMo on 18-5-2.
 */
public class StatusBarUtils {

  public static void setStatusBarColor(Activity activity, @ColorRes int colorResId) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(colorResId));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setNavigationBarColor(Activity activity, @ColorRes int colorResId) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(activity.getResources().getColor(colorResId));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
