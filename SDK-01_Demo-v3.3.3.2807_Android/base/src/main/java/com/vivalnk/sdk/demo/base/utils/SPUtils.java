package com.vivalnk.sdk.demo.base.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.vivalnk.sdk.demo.base.app.BaseApplication;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * Created by JakeMo on 17-11-20.
 */

public class SPUtils {

  private static final String TAG = "SPUtils";

  private static String DEFAULT_DIR = "/sdcard/PL_DATA";
  private static String DEFAULT_FILE = "vivalnk_sp";
  private static String spDir = DEFAULT_DIR;
  private static String spName = DEFAULT_FILE;

  private static SPUtils sInstance;

  private static Context sContext;

  public static SPUtils getInstance() {
    return getInstance(BaseApplication.getAppContext(), null);
  }

  public static SPUtils getInstance(String dir) {
    return getInstance(BaseApplication.getAppContext(), dir);
  }

  public static SPUtils getInstance(Context context, String dir) {

    if (sInstance == null) {
      synchronized (SPUtils.class) {
        if (sInstance == null) {
          sInstance = new SPUtils(context, dir);
        }
      }
    }

    return sInstance;
  }

  private SPUtils(Context context, String dir) {
    sContext = context.getApplicationContext();
    if (!TextUtils.isEmpty(dir)) {
      setDir(dir);
    }
  }

  public SPUtils setFile(String name) {
    spName = name;
    return this;
  }

  public SPUtils setDir(String dir) {

    if (TextUtils.isEmpty(dir)) {
      return this;
    }

    File mPreferencesDir = new File(dir);
    if (!mPreferencesDir.exists()) {
      mPreferencesDir.mkdirs();
    }

    try {
      Field field = ContextWrapper.class.getDeclaredField("mBase");
      field.setAccessible(true);
      Object obj = field.get(sContext.getApplicationContext());
      field = obj.getClass().getDeclaredField("mPreferencesDir");
      field.setAccessible(true);
      field.set(obj, mPreferencesDir);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return this;
  }

  private static SharedPreferences getPreferences(String fileName) {
    if (TextUtils.isEmpty(fileName)) {
      fileName = spName;
    }
    return sContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
  }

  public static void put(String key, Object object) {
    put(spName, key, object);
  }

  public static void put(String fileName, String key, Object object) {

    if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(key)
        || object == null) {
      return;
    }

    SharedPreferences.Editor editor = getPreferences(fileName).edit();
    if (object instanceof String) {
      editor.putString(key, (String) object);
    } else if (object instanceof Integer) {
      editor.putInt(key, (Integer) object);
    } else if (object instanceof Boolean) {
      editor.putBoolean(key, (Boolean) object);
    } else if (object instanceof Float) {
      editor.putFloat(key, (Float) object);
    } else if (object instanceof Long) {
      editor.putLong(key, (Long) object);
    } else {
      editor.putString(key, object.toString());
    }

    editor.apply();
  }

  public static <T> T get(String key, Object defaultValue) {
    return (T) get(spName, key, defaultValue);
  }

  public static Object get(String fileName, String key, Object defaultValue) {

    if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(key)) {
      return null;
    }

    SharedPreferences sp = getPreferences(fileName);

    if (defaultValue instanceof String) {
      return sp.getString(key, (String) defaultValue);
    } else if (defaultValue instanceof Integer) {
      return sp.getInt(key, (Integer) defaultValue);
    } else if (defaultValue instanceof Boolean) {
      return sp.getBoolean(key, (Boolean) defaultValue);
    } else if (defaultValue instanceof Float) {
      return sp.getFloat(key, (Float) defaultValue);
    } else if (defaultValue instanceof Long) {
      return sp.getLong(key, (Long) defaultValue);
    }

    return null;
  }

  public static Map<String, ?> getAll(Context context) {
    return getAll(spName);
  }

  /**
   * SP中获取所有键值对
   *
   * @return Map对象
   */
  public static Map<String, ?> getAll(String fileName) {
    if (TextUtils.isEmpty(fileName)) {
      return null;
    }
    return getPreferences(fileName).getAll();
  }

  public static boolean contains(String key) {
    return contains(spName, key);
  }

  public static boolean contains(String fileName, String key) {
    if (TextUtils.isEmpty(fileName)) {
      return false;
    }
    return getPreferences(fileName).contains(key);
  }


  public void putSet(String key, Set<String> values) {
    put(spName, key, values);
  }

  public void putSet(String fileName, String key, Set<String> values,
      boolean isCommit) {
    if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(key) || values.isEmpty()) {
      return;
    }
    SharedPreferences sp = getPreferences(fileName);
    if (isCommit) {
      sp.edit().putStringSet(key, values).commit();
    } else {
      sp.edit().putStringSet(key, values).apply();
    }
  }

  public static void remove(String key) {
    remove(spName, key);
  }

  public static void remove(String fileName, String key) {
    remove(fileName, key, false);
  }

  public static void remove(String fileName, String key, boolean isCommit) {
    if (sContext == null || TextUtils.isEmpty(fileName)) {
      return;
    }
    SharedPreferences sp = getPreferences(fileName);
    if (isCommit) {
      sp.edit().remove(key).commit();
    } else {
      sp.edit().remove(key).apply();
    }
  }

  public void clear() {
    clear(spName);
  }
  /**
   * SP中清除所有数据
   */
  public void clear(String fileName) {
    clear(fileName, false);
  }

  /**
   * SP中清除所有数据
   *
   * @param fileName
   * @param isCommit {@code true}: {@link SharedPreferences.Editor#commit()}<br> {@code false}:
   * {@link SharedPreferences.Editor#apply()}
   */
  public void clear(String fileName, final boolean isCommit) {
    if (sContext == null || TextUtils.isEmpty(fileName)) {
      return;
    }
    SharedPreferences sp = getPreferences(fileName);
    if (isCommit) {
      sp.edit().clear().commit();
    } else {
      sp.edit().clear().apply();
    }
  }

}
