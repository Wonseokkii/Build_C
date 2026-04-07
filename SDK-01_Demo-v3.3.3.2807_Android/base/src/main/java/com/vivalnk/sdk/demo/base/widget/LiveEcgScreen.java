package com.vivalnk.sdk.demo.base.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.vivalnk.sdk.demo.base.R;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.DeviceInfoUtils;
import com.vivalnk.sdk.model.SampleData;
import com.vivalnk.sdk.model.common.DataType;
import com.vivalnk.sdk.model.common.DataType.DataKey;
import com.vivalnk.sdk.open.BaseLineRemover;
import com.vivalnk.sdk.open.BaseLineRemover.Listener;
import com.vivalnk.sdk.open.utils.DenoiseTool;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by JakeMo on 18-5-6.
 */
public class LiveEcgScreen implements Callback {

  public static final String TAG = "LiveEcgScreen";

  private RTSEcgView ecgView;
  private Context mContext;

  private int color;
  //ecg原始点
  private LinkedBlockingQueue<EcgPoint> ecgRawList;

  private int mSampleRate;          //采样总数
  private float mSampleDeltaTime; //采样总时间(ms)
  private float mDrawPerPointTime; //采样总时间(ms)

  private boolean stop = false;

  // ms/mm
  private float timePerPoint;  //ms/per_point

  private SampleData preEcgData;

  private Handler mHandler;
  private HandlerThread mHandlerThread;
  private Runnable runnable;

  private BaseLineRemover remover;

  private boolean denoisy;

  private Device device;

  public LiveEcgScreen(Context context, Device device, RTSEcgView ecgView) {
    this.mContext = context.getApplicationContext();
    this.device = device;
    this.ecgView = ecgView;
    this.ecgView.setSampleRate(DeviceInfoUtils.getEcgSamplingFrequency(device));
    remover = new BaseLineRemover(device, new Listener() {
      @Override
      public void onDataPop(Device device, SampleData data) {

      }
    });
    ecgRawList = new LinkedBlockingQueue<>();

    initThread();
    color = mContext.getResources().getColor(R.color.black);

  }

  private void initThread() {

    mHandlerThread = new HandlerThread(TAG);
    mHandlerThread.start();
    mHandler = new Handler(mHandlerThread.getLooper(), this);
    runnable = new Runnable() {
      @Override
      public void run() {
        //one point
        EcgPoint ecgPoint = ecgRawList.poll();
        if (null != ecgPoint) {
          ecgView.addEcgPoint(ecgPoint);
        }
        mHandler.postDelayed(runnable, (long) Math.max(Math.floor(mDrawPerPointTime), 1));
      }
    };

  }

  public void setDrawDirection(int direction) {
    ecgView.setDrawDirection(direction);
  }

  public void showMarkPoint(boolean show) {
    ecgView.showMarkPoint(show);
  }

  public void update(SampleData ecgData) {

    int[] rawEcgArray = ecgData.getECG();

    if (null == preEcgData || rawEcgArray == null || rawEcgArray.length <= 0
        || (Math.abs(preEcgData.time - ecgData.time) >= 2000)) {
      preEcgData = ecgData;
      return;
    } else {
      if (mSampleDeltaTime <= 0) { //若未初始化过，即未计算过，则计算一次，便不再计算
        long temp = ecgData.getTime() - preEcgData.getTime();

        //若前后差距正常，即连续的点
        if (0 < temp && temp < 2000) {
          mSampleRate = preEcgData.getECG().length;
          timePerPoint = temp / (mSampleRate * 1.0F);
          ecgView.setSampleRate(mSampleRate, (int) temp);
          mDrawPerPointTime = (temp - 20) / (mSampleRate * 1.0f);
          mSampleDeltaTime = temp;
          mHandler.post(runnable);
        } else {
          //若前后差距不正常，即不连续的点，则不更新mSampleDeltaTime，但是仍然接连绘制两个包

        }
      }
    }
    //出现偏差  跑快一点
    long temp = 1000;
    if (ecgRawList.size() > rawEcgArray.length * 300) {
      ecgRawList.clear();
      mDrawPerPointTime = (temp - 20) / (mSampleRate * 1.0f);
    } else if (ecgRawList.size() > rawEcgArray.length * 180) {
      mDrawPerPointTime = (temp - 500) / (mSampleRate * 1.0f);
    } else if (ecgRawList.size() > rawEcgArray.length * 100) {
      mDrawPerPointTime = (temp - 200) / (mSampleRate * 1.0f);
    } else if (ecgRawList.size() > rawEcgArray.length * 10) {
      mDrawPerPointTime = (temp - 100) / (mSampleRate * 1.0f);
    } else {
      mDrawPerPointTime = (temp - 20) / (mSampleRate * 1.0f);
    }

    if (mSampleDeltaTime <= 0) {
      //若未经过初始化，则不绘制
      return;
    }

    long startTime = ecgData.time;
    SampleData removerData = remover.handle(ecgData);

    if (denoisy && removerData != null && removerData.getData(DataKey.denoiseEcg) != null) {
      int[] denoiseEcg = removerData.getData(DataKey.denoiseEcg);
      for (int i = 0; i < denoiseEcg.length; i++) {
        EcgPoint ecgPoint = new EcgPoint();
        ecgPoint.time = (long) (startTime * 1.0D + timePerPoint * i);
        ecgPoint.mv = denoiseEcg[i] / (ecgData.getMagnification() * 1.0f) ;
        ecgPoint.color = color;
        ecgRawList.add(ecgPoint);
      }
    } else {
      for (int i = 0; i < rawEcgArray.length; i++) {
        EcgPoint ecgPoint = new EcgPoint();
        ecgPoint.time = (long) (startTime * 1.0D + timePerPoint * i);
        ecgPoint.mv = rawEcgArray[i] / (ecgData.getMagnification() * 1.0f); //mv
        ecgPoint.color = color;
        ecgRawList.add(ecgPoint);
      }
    }

    preEcgData = ecgData;

  }

  public void showStandard(boolean drawStandard) {
    this.ecgView.showStandardd(drawStandard);
  }

  @Override
  public boolean handleMessage(Message msg) {
    return false;
  }

  public void stopUpdate() {
    mHandler.removeCallbacksAndMessages(null);
  }

  public void switchGain() {
    ecgView.switchGain();
  }

  public void revert(boolean revert) {
    ecgView.revert(revert);
  }

  public void denoisy(boolean denoisy) {
    this.denoisy = denoisy;
  }

  public void clear() {
    remover.destroy();
    ecgView.clearDataList();
  }

  public void destroy() {
    stopUpdate();
    clear();
  }

}
