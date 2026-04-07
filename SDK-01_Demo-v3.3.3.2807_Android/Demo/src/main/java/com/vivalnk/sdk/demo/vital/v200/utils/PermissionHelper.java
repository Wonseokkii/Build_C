package com.vivalnk.sdk.demo.vital.v200.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

/**
 * Created by JakeMo on 18-1-1.
 */

public class PermissionHelper {

  public static boolean hasPermission(Context context, String permission) {
    return PackageManager.PERMISSION_GRANTED == ContextCompat
        .checkSelfPermission(context, permission);
  }

  public static boolean hasPermission(Context context, String... permissions) {
    for (String permission : permissions) {
      if (PackageManager.PERMISSION_GRANTED != ContextCompat
          .checkSelfPermission(context, permission)) {
        return false;
      }
    }
    return true;
  }

}
