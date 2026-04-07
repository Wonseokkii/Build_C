package com.vivalnk.sdk.demo.base.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.vivalnk.sdk.demo.base.R;
import java.util.ArrayList;
import java.util.List;

public class LogListDialogView {

  public static final int MAX_SIZE = 500;

  private AlertDialog mDataLogDialog;
  private List<String> mDataLogList;
  private ListView mDataLogListView;
  private ArrayAdapter<String> mDataLogAdapter;

  private Handler mUiHandler;

  public LogListDialogView() {
    mUiHandler = new Handler(Looper.getMainLooper());
  }

  public void create(Activity activity, List<String> list) {
    this.mDataLogList = list;
    //当dataList没有任何内容的时候
    //就算为adapter设置了list, 为ListView设置了这个adapter
    //处于Dialog中的ListView
    //先show()之后，再为list add内容，然后notifyDataChanged()
    //是无法显示ListView的
    //因此，采用空字符串的item，来处理这个问题
    //TODO 待后续深入研究
    mDataLogList.add(" ");
    mDataLogAdapter = new ArrayAdapter<>(activity,
        android.R.layout.simple_list_item_1, mDataLogList);
    mDataLogListView = new ListView(activity);
    mDataLogListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));
    mDataLogListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    mDataLogListView.setAdapter(mDataLogAdapter);
    mDataLogListView.setDivider(new ColorDrawable(Color.MAGENTA));
    mDataLogListView.setDividerHeight(1);

    mDataLogDialog = new AlertDialog.Builder(activity)
        .setView(mDataLogListView)

        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .setNegativeButton(R.string.clear, new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            mDataLogList.clear();
            mDataLogAdapter.notifyDataSetChanged();
          }
        }).create();
    mDataLogListView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            removeScrollRunnable();
            mDataLogListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            break;
          case MotionEvent.ACTION_UP:
            resumeScrollModeDelay();
            break;
        }
        return false;
      }
    });

    mDataLogDialog.setCanceledOnTouchOutside(false);
  }

  /***
   * 避免手抬起来就会滑动，而无法手动继续滑动
   * 解决方案：延迟这个滑动模式的更改
   */
  private Runnable resumeScrollRunnable = new Runnable() {
    @Override
    public void run() {
      mDataLogListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    }
  };
  private void removeScrollRunnable() {
    mUiHandler.removeCallbacks(resumeScrollRunnable);
  }
  private void resumeScrollModeDelay() {
    mUiHandler.postDelayed(resumeScrollRunnable, 2000);
  }

  public void create(Activity activity) {
    create(activity, new ArrayList<>());
  }

  public void updateLog(String msg) {
    mUiHandler.post(new Runnable() {
      @Override
      public void run() {
        mDataLogList.add(msg);
        while (mDataLogList.size() > MAX_SIZE) {
          mDataLogList.remove(0);
        }
        mDataLogAdapter.notifyDataSetChanged();
      }
    });
  }

  public void notifyDataSetChangedDelay(long timeMillis) {
    mUiHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        mDataLogAdapter.notifyDataSetChanged();
      }
    }, timeMillis);
  }

  public void show() {
    mUiHandler.post(new Runnable() {
      @Override
      public void run() {
        if (!mDataLogDialog.isShowing()) {
          //还未attach activity
          if (mDataLogDialog.getOwnerActivity() == null) {
            mDataLogDialog.show();
            return;
          }
          //若已经attached, 并且activity没有在finishing，则可以show
          if(!mDataLogDialog.getOwnerActivity().isFinishing()) {
            mDataLogDialog.show();
            return;
          }
        }
      }
    });
  }

  public void dismiss() {
    mUiHandler.post(new Runnable() {
      @Override
      public void run() {
        if (mDataLogDialog.isShowing()) {
          mDataLogDialog.dismiss();
        }
      }
    });
  }

}
