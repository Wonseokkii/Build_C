package com.vivalnk.sdk.demo.vital.ui.device.o2.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.vivalnk.sdk.common.utils.DensityUtils;
import com.vivalnk.sdk.demo.vital.R;

import java.util.ArrayList;

public class WaveViewRTS extends WaveViewBase {

  private int flagIndex = 0;
  //反转显示
  protected boolean invert;

  protected Paint crestPaint;
  protected int crestHeight;

  //所需要绘制的坐标点
  protected final ArrayList<PointModel> pointList = new ArrayList<>();

  public WaveViewRTS(Context context) {
    super(context);
  }

  public WaveViewRTS(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public WaveViewRTS(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super.init(context, attrs, defStyleAttr, defStyleRes);
    crestHeight = DensityUtils.dip2px(context, 6);
    int crestColor = getContext().getResources().getColor(R.color.color_ecg_text);
    crestPaint = getPaintWithColor(crestColor, DensityUtils.dip2px(context, 2));
  }

  public void changeInvert() {
    invert = !invert;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  protected void drawECGView(Canvas canvas) {
    synchronized (pointList) {
      int endIndex = Math.min(pointList.size(), maxDisplayPoints);
      for (int i = 0; i < endIndex - 1; i++) {
        float x = i * pixelsPerPoint;
        int index = i;
        float startX = x;
        float endX = x + pixelsPerPoint;

        PointModel start = pointList.get(index);
        PointModel end = pointList.get(index + 1);
        float startY = convertY(start.point);
        float endY = convertY(end.point);

        if (startY < 0) {
          startY = 0;
        }
        if (startY > mHeight) {
          startY = mHeight;
        }

        if (endY < 0) {
          endY = 0;
        }
        if (endY > mHeight) {
          endY = mHeight;
        }

        if (invert) {
          startY = mHeight - startY;
          endY = mHeight - endY;
        }

        if (i != flagIndex) {
          if ((flagIndex - flagLength) < 0) {
            //空白区域
            //1. 0 < i < flagIndex
            //2. (maxDisplayPoints + (flagIndex - flagLength)) < i < maxDisplayPoints
            if ((0 < i && i < flagIndex)
                    || ((maxDisplayPoints + (flagIndex - flagLength)) < i && i < maxDisplayPoints)
            ) {
              /**空白不绘制*/
            }
            else {
              canvas.drawLine(startX, startY, endX, endY, curvePaint);
            }
          }
          else {
            //空白区域
            //(flagIndex - flagLength) < i < flagIndex
            if ((flagIndex - flagLength) < i && i < flagIndex) {
              /**空白不绘制*/
            }
            else {
              canvas.drawLine(startX, startY, endX, endY, curvePaint);
            }
          }
        }

        if (start.isCrest != null && start.isCrest) {
          canvas.drawLine(startX, mHeight, startX, mHeight - crestHeight, crestPaint);
        }

        if (end.isCrest != null && end.isCrest) {
          canvas.drawLine(endX, mHeight, endX, mHeight - crestHeight, crestPaint);
        }
      }
    }
  }

  public void addEcgPoint(PointModel model) {
    synchronized (pointList) {
      if (pointList.size() < maxDisplayPoints - 1) {
        pointList.add(model);
        flagIndex = pointList.size() - 1;
      }
      else if (maxDisplayPoints > 0) {
        if (flagIndex >= maxDisplayPoints || flagIndex >= pointList.size()) {
          flagIndex = 0;
        }

        pointList.set(flagIndex, model);
        flagIndex++;
      }

      while (pointList.size() > maxDisplayPoints) {
        pointList.remove(pointList.size() - 1);
      }
    }

    postInvalidate();
  }

  public void clear() {
    synchronized (pointList) {
      pointList.clear();
    }
    invalidate();
  }

  public static class PointModel {

    public PointModel() {

    }

    public Float point;
    public Boolean isCrest;
  }
}