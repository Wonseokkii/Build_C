package com.vivalnk.sdk.demo.vital;

import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.demo.vital.ui.BleSigGlucoseActivity;
import com.vivalnk.sdk.device.sig.glucose.BleSIG_GlucoseManager;
import com.vivalnk.sdk.utils.GSON;

import java.util.Map;

public class CallbackUtils {
    private static class SingletonHolder {
        private static final CallbackUtils INSTANCE = new CallbackUtils();
    }

    private CallbackUtils() {
    }

    public static CallbackUtils getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void getAllRecords(BleSigGlucoseActivity activity, BleSIG_GlucoseManager manager, String tag) {
        manager.getAllRecords(new Callback() {
            @Override
            public void onStart() {
                activity.showProgressDialog("staring...");
            }

            @Override
            public void onComplete(Map<String, Object> data) {
                activity.dismissProgressDialog();
                activity.showAlertDialog("Result of Get All Records", GSON.toJson(data));
                VitalLog.d(tag, GSON.toJson(data));
            }

            @Override
            public void onError(int code, String msg) {
                activity.dismissProgressDialog();
                activity.showAlertDialog("Result of Get All Records", "error code = " + code + ", msg = " + msg);
            }
        });
    }

    public void getFirstRecord(BleSigGlucoseActivity activity, BleSIG_GlucoseManager manager, String tag) {
        manager.getFirstRecord(new Callback() {
            @Override
            public void onStart() {
                activity.showProgressDialog("staring...");
            }

            @Override
            public void onComplete(Map<String, Object> data) {
                activity.dismissProgressDialog();
                activity.showAlertDialog("Result of Get First Records", GSON.toJson(data));
                VitalLog.d(tag, GSON.toJson(data));
            }

            @Override
            public void onError(int code, String msg) {
                activity.dismissProgressDialog();
                activity.showAlertDialog("Result of Get First Records", "error code = " + code + ", msg = " + msg);
            }
        });
    }

    public void getLastRecord(BleSigGlucoseActivity activity, BleSIG_GlucoseManager manager, String tag) {
        manager.getLastRecord(new Callback() {
            @Override
            public void onStart() {
                activity.showProgressDialog("staring...");
            }

            @Override
            public void onComplete(Map<String, Object> data) {
                activity.dismissProgressDialog();
                activity.showAlertDialog("Result of Get First Records", GSON.toJson(data));
                VitalLog.d(tag, GSON.toJson(data));
            }

            @Override
            public void onError(int code, String msg) {
                activity.dismissProgressDialog();
                activity.showAlertDialog("Result of Get First Records", "error code = " + code + ", msg = " + msg);
            }
        });
    }
}
