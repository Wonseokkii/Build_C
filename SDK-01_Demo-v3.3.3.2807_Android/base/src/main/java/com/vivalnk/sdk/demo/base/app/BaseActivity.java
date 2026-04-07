package com.vivalnk.sdk.demo.base.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.vivalnk.sdk.demo.base.R;
import com.vivalnk.sdk.demo.base.common.ActivityStackManager;
import com.vivalnk.sdk.demo.base.custom.BackProgressDialog;
import com.vivalnk.sdk.demo.base.mvp.IPresenter;
import com.vivalnk.sdk.common.eventbus.EventBus;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JakeMo on 18-4-26.
 */
public abstract class BaseActivity<P extends IPresenter> extends AppCompatActivity {

  public final String TAG = getClass().getSimpleName();

  protected ProgressDialog progressDialog;
  private List<AlertDialog> alertDialogList;

  private boolean isForeground = false;

  @Nullable
  protected P mPresenter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    beforeSetContentView(savedInstanceState);

    Layout layout = getLayout();
    if (layout.type == Layout.intType) {
      setContentView(((int) layout.value));
    } else if(layout.type == Layout.viewType) {
      setContentView((View)layout.value);
    }
    ButterKnife.bind(this);

    afterSetContentView(savedInstanceState);

    beforeEventRegister(savedInstanceState);
    EventBus.getDefault().register(this);
    afterEventRegister(savedInstanceState);

    progressDialog = new BackProgressDialog(BaseActivity.this);
    progressDialog.setCanceledOnTouchOutside(false);
    progressDialog.setMessage("Waiting...");

    alertDialogList = new ArrayList<>();

    Log.v(TAG, "----onCreate()----");
  }


  protected void beforeSetContentView(Bundle savedInstanceState) {}
  protected void afterSetContentView(Bundle savedInstanceState) {}
  protected void beforeEventRegister(Bundle savedInstanceState) {}
  protected void afterEventRegister(Bundle savedInstanceState) {}

  @Subscribe
  public void onEvent(Object obj) {
    //empty event for base register
  }

  protected abstract Layout getLayout();

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Log.v(TAG, "----onNewIntent()----");
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.v(TAG, "----onStart()----");
  }

  @Override
  protected void onResume() {
    isForeground = true;
    super.onResume();
    Log.v(TAG, "----onResume()----");
  }

  @Override
  protected void onPause() {
    super.onPause();
    isForeground = false;
    Log.v(TAG, "----onPause()----");
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.v(TAG, "----onStop()----");
  }

  public boolean isForeground() {
    return isForeground;
  }

  public void exitApp() {
    ActivityStackManager.getInstance().exitApp();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
    Log.v(TAG, "----onDestroy()----");
    dismissProgressDialog();
    if (mPresenter != null) {
      mPresenter.onDestroy();
      mPresenter = null;
    }
    for (AlertDialog alertDialog : alertDialogList) {
      alertDialog.dismiss();
    }
  }

  @Override
  public void finish() {
    if (!this.isFinishing()) {
      super.finish();
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

  public void showProgressDialog() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        dismissProgressDialog();
        if (!progressDialog.isShowing() && !isFinishing()) {
          progressDialog.setMessage("Waiting...");
          progressDialog.show();
        }
      }
    });
  }

  public void showProgressDialog(final String msg) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        dismissProgressDialog();
        if (!progressDialog.isShowing() && !isFinishing()) {
          progressDialog.setMessage(msg);
          progressDialog.show();
        }
      }
    });
  }

  public void dismissProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
    }
  }

  protected BackProgressDialog createProgressDialog(int max, String msg, boolean cancelable, boolean outsideCancelable) {
    BackProgressDialog dialog = new BackProgressDialog(this);
    dialog.setCancelable(cancelable);
    dialog.setCanceledOnTouchOutside(outsideCancelable);
    dialog.setMax(max);
    dialog.setMessage(msg);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    return dialog;
  }

  public void showAlertDialog(String title, String msg) {
    showAlertDialog(title, msg, null, null);
  }

  public void showAlertDialog(String title, String msg,
      OnClickListener okListener,
      OnClickListener cancelListener) {
    showAlertDialog(title, msg, "OK", "Cancel", okListener, cancelListener);
  }

  public void showAlertDialog(String title, String msg, String okText, String cancelText,
      OnClickListener okListener,
      OnClickListener cancelListener) {
    if (this.isFinishing()) {
      return;
    }

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        showAlertDialogSync(title, msg, okText, cancelText, okListener, cancelListener);
      }
    });
  }

  public AlertDialog showAlertDialogSync(String title, String msg) {
    return showAlertDialogSync(title, msg, "OK", "Cancel", null, null);
  }

  public AlertDialog showAlertDialogSync(String title, String msg, String okText, String cancelText, OnClickListener okListener, OnClickListener cancelListener) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
    final AlertDialog dialog = builder.setTitle(title)
        .setIcon(null)
        .setMessage(msg)
        .setPositiveButton(okText, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (okListener != null) {
              okListener.onClick(dialog, which);
            }
            dialog.dismiss();
          }
        })
        .setNegativeButton(cancelText, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (cancelListener != null) {
              cancelListener.onClick(dialog, which);
            }
            dialog.dismiss();
            alertDialogList.remove(dialog);
          }
        })
        .create();
    dialog.setCanceledOnTouchOutside(false);
    if (!dialog.isShowing()) {
      dialog.show();
      alertDialogList.add(dialog);
    }
    return dialog;
  }

  public void showToast(@StringRes int resId) {
    showToast(resId, Toast.LENGTH_SHORT);
  }

  public void showToast(@StringRes int resId, int showType) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        BaseUtils.showToast(getApplicationContext(), resId, showType);
      }
    });
  }

  public void showToast(CharSequence text) {
    showToast(text, Toast.LENGTH_SHORT);
  }

  public void showToast(CharSequence text, int showType) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        BaseUtils.showToast(getApplicationContext(), text, showType);
      }
    });
  }

  public <T extends Activity> void navTo(Class<T> clazz) {
    navTo(this, null, clazz);
  }

  public static <T extends Activity> void navTo(Context context, Class<T> clazz) {
    navTo(context, null, clazz);
  }

  public static <T extends Activity> void navTo(Context context, Bundle extras, Class<T> clazz) {
    BaseUtils.navToActivity(context, extras, clazz);
  }

  public static <T extends Fragment> T createFragment(Class<T> clazz) {
    return createFragment(null, clazz);
  }

  public static <T extends Fragment> T createFragment(Bundle extras, Class<T> clazz) {
    return BaseUtils.createFragment(extras, clazz);
  }

}
