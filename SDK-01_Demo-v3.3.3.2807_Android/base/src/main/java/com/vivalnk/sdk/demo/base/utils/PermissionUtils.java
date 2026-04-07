package com.vivalnk.sdk.demo.base.utils;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import androidx.core.content.ContextCompat;

/**
 * Created by JakeMo on 18-8-1.
 */
public class PermissionUtils {

  public static void gotoAppDetailSettings(Context context) {
    Intent localIntent = new Intent();
    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    if (VERSION.SDK_INT >= 9) {
      localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
      localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
    } else if (VERSION.SDK_INT <= 8) {
      localIntent.setAction(Intent.ACTION_VIEW);
      localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
      localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
    }
    context.startActivity(localIntent);
  }

  public static boolean checkStoragePermission(Context context) {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      return Environment.isExternalStorageManager();
    } else {
      int result = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE);
      int result1 = ContextCompat.checkSelfPermission(context, permission.WRITE_EXTERNAL_STORAGE);
      return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
  }

}
