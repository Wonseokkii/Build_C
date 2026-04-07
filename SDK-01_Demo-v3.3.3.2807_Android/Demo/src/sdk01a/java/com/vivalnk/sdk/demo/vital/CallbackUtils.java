package com.vivalnk.sdk.demo.vital;

import com.vivalnk.sdk.demo.vital.ui.BleSigGlucoseActivity;
import com.vivalnk.sdk.device.sig.glucose.BleSIG_GlucoseManager;

public class CallbackUtils {
    private static class SingletonHolder {
        private static final CallbackUtils INSTANCE = new CallbackUtils();
    }

    private CallbackUtils() {
    }

    public static CallbackUtils getInstance() {
        return CallbackUtils.SingletonHolder.INSTANCE;
    }

    public void getAllRecords(BleSigGlucoseActivity activity, BleSIG_GlucoseManager manager, String tag) {
    }

    public void getFirstRecord(BleSigGlucoseActivity activity, BleSIG_GlucoseManager manager, String tag) {
    }

    public void getLastRecord(BleSigGlucoseActivity activity, BleSIG_GlucoseManager manager, String tag) {
    }
}
