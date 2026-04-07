package com.vivalnk.sdk.demo.base.custom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.vivalnk.sdk.demo.base.app.BaseUtils;

public class DialogActivity extends AppCompatActivity {
  private String title;
  private String msg;
  public static void showDialogActivity(Activity activity, String title, String msg) {
    Intent intent = new Intent(activity.getApplicationContext(), DialogActivity.class);
    intent.putExtra("title", title);
    intent.putExtra("msg", msg);
    activity.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setBackgroundDrawable(new ColorDrawable(0));

    // Should do a proper argument verification here
    if (getIntent() != null && getIntent().hasExtra("title") && getIntent().hasExtra("msg")) {
      this.title = getIntent().getStringExtra("title");
      this.msg = getIntent().getStringExtra("msg");
      displayDialog();
      return;
    } {
      BaseUtils.showToast(this, "need title, msg field for dialog", Toast.LENGTH_SHORT);
      finish();
    }
  }

  AlertDialog dialog;
  private void displayDialog() {
    dialog = new AlertDialog.Builder(this)
        .setCancelable(true)
        .setTitle(title)
        .setMessage(msg)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Handle a positive answer
            dialog.dismiss();
            finish();
          }
        })
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Handle a negative answer
            dialog.dismiss();
            finish();
          }
        })
        .setOnDismissListener(new DialogInterface.OnDismissListener() {
          @Override
          public void onDismiss(DialogInterface dialogInterface) {
            finish();
          }
        }).create();
    dialog.setCancelable(true);
    dialog.setCanceledOnTouchOutside(true);
    dialog.show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (dialog.isShowing()) {
      dialog.dismiss();
      dialog = null;
    }
  }
}
