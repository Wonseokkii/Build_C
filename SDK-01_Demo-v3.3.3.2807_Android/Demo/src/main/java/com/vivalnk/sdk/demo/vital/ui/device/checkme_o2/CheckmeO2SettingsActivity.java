package com.vivalnk.sdk.demo.vital.ui.device.checkme_o2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.device.checkmeo2.CheckmeO2Manager;
import com.vivalnk.sdk.model.Device;
import org.jetbrains.annotations.NotNull;

public class CheckmeO2SettingsActivity extends ConnectedActivity {

  @Override
  protected void onDeviceInitialized(Bundle savedInstanceState) {

    if (savedInstanceState == null) {
      Bundle extras = new Bundle();
      extras.putSerializable("device", mDevice);
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.settings, SettingsFragment.class, extras)
          .commit();
    }
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

  }

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.settings_activity);
  }

  public static class SettingsFragment extends PreferenceFragmentCompat {

    CheckmeO2Manager manager;
    Device mDevice;
    String deviceIdKey;

    @Override
    public void setPreferencesFromResource(int preferencesResId,
        @Nullable @org.jetbrains.annotations.Nullable String key) {
      super.setPreferencesFromResource(preferencesResId, key);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
      return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
      super.onNavigateToScreen(preferenceScreen);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

      Bundle extras = getArguments();
      mDevice = (Device) extras.getSerializable("device");
      deviceIdKey = mDevice.getId().replace(":", "");
//      manager = new CheckmeO2Manager(mDevice);
//      manager.setRealtimeDataInterval((long)(DeviceManager.getInstance().checkme_o2RealtimeDataInterval*1000));
//      manager.setWhetherAllowRegularReadHistoryData(DeviceManager.getInstance().allowCheckme_02ReadFile);

      setPreferencesFromResource(R.xml.root_preferences, rootKey);
      EditTextPreference rtsSpo2Frequency = findPreference("rtsSpo2Frequency");
      if (rtsSpo2Frequency != null) {
        prefixKey(mDevice, rtsSpo2Frequency);
        rtsSpo2Frequency.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
          @Override
          public void onBindEditText(@NonNull @NotNull EditText editText) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
          }
        });
        rtsSpo2Frequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
          @Override
          public boolean onPreferenceChange(Preference preference, Object newValue) {
            try {
              int internal = Integer.valueOf(newValue + "");
//              manager.setRealtimeDataInterval(internal);
            } catch (NumberFormatException e) {

            }
            return false;
          }
        });
      }
    }
  }

  public static void prefixKey(Device device, Preference preference) {
    String deviceIdKey = device.getId().replace(":", "");
    preference.setKey(deviceIdKey + "_" + preference.getKey());
  }

}