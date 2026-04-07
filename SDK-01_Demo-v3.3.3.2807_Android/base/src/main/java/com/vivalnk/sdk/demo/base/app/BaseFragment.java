package com.vivalnk.sdk.demo.base.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.vivalnk.sdk.demo.base.custom.BackProgressDialog;
import com.vivalnk.sdk.common.eventbus.EventBus;
import com.vivalnk.sdk.common.eventbus.Subscribe;

/**
 * Created by JakeMo on 18-4-26.
 */
public abstract class BaseFragment extends Fragment {

  protected final String TAG = getClass().getSimpleName();

  protected ProgressDialog progressDialog;

  protected FragmentActivity mActivity;

  protected abstract Layout getLayout();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    setRetainInstance(true);
    EventBus.getDefault().register(this);
    Log.v(TAG, "----onCreate()----");
  }

  @Subscribe
  public void onEvent(Object obj) {
    //empty event for base register
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mActivity = getActivity();
    Log.v(TAG, "----onAttach()----");
  }

  @Nullable
  @Override
  @CallSuper
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    Layout layout = getLayout();
    View view = null;
    if (layout.type == Layout.intType) {
      view = inflater.inflate((Integer) getLayout().value, container, false);
    } else if (layout.type == Layout.viewType) {
      view = ((View) layout.value);
    }
    ButterKnife.bind(this, view);
    Log.v(TAG, "----onCreateView()----");
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Log.v(TAG, "----onViewCreated()----");
  }

  @Override
  public void onStart() {
    super.onStart();
    Log.v(TAG, "----onStart()----");
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.v(TAG, "----onResume()----");
  }

  @Override
  public void onPause() {
    super.onPause();
    Log.v(TAG, "----onPause()----");
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.v(TAG, "----onStop()----");
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Log.v(TAG, "----onDestroyView()----");
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mActivity = null;
    Log.v(TAG, "----onDetach()----");
  }

  @Override
  public void onDestroy() {
    EventBus.getDefault().unregister(this);
    super.onDestroy();
    Log.v(TAG, "----onDestroy()----");
  }

  public void showProgressDialog() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        dismissProgressDialog();
        if (!progressDialog.isShowing() && isVisible()) {
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
        if (!progressDialog.isShowing() && isVisible()) {
          progressDialog.setMessage(msg);
          progressDialog.show();
        }
      }
    });
  }

  public void dismissProgressDialog() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (progressDialog != null && progressDialog.isShowing()) {
          progressDialog.dismiss();
        }
      }
    });
  }

  protected BackProgressDialog createProgressDialog(int max, String msg) {
    BackProgressDialog dialog = new BackProgressDialog(getActivity());
    dialog.setCancelable(false);
    dialog.setCanceledOnTouchOutside(false);
    dialog.setMax(max);
    dialog.setMessage(msg);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    return dialog;
  }

  public void showAlertDialog(String title, String msg,
      DialogInterface.OnClickListener okListener,
      DialogInterface.OnClickListener cancelListener) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final AlertDialog dialog = builder.setTitle(title)
            .setIcon(null)
            .setMessage(msg)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", cancelListener)
            .create();
        dialog.setCanceledOnTouchOutside(false);
        if (!dialog.isShowing()) {
          dialog.show();
        }
      }
    });
  }

  public void showToast(@StringRes int resId) {
    showToast(resId, Toast.LENGTH_SHORT);
  }

  public void showToast(@StringRes int resId, int showType) {
    BaseUtils.showToast(this.getContext(), resId, showType);
  }

  public void showToast(CharSequence text) {
    showToast(text, Toast.LENGTH_SHORT);
  }

  public void showToast(CharSequence text, int showType) {
    BaseUtils.showToast(this.getContext(), text, showType);
  }

  public <T extends Activity> void navTo(Class<T> clazz) {
    navTo(this.getContext(), null, clazz);
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

  protected void runOnUiThread(Runnable runnable) {
    if (mActivity != null) {
      mActivity.runOnUiThread(runnable);
    }
  }

}
