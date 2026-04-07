package com.vivalnk.sdk.demo.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import com.vivalnk.sdk.common.utils.DensityUtils;
import com.vivalnk.sdk.utils.DateFormat;
import java.util.List;
import org.apache.commons.lang3.time.DateFormatUtils;

public class RTSEcgView extends AbsEcgView {

  //从左往右开始绘制
  public static final int LEFT_IN_RIGHT_OUT = 1;
  //从右往左开始绘制
  public static final int RIGHT_IN_LEFT_OUT = 2;

  protected int mDrawDirection = LEFT_IN_RIGHT_OUT;

  //是否展示索引光标
  private boolean show = false;

  //flag index: 插入点索引， 即光标位置
  protected int flagIndex;
  protected int flagLength; //空移长度

  protected Paint flagPaint;
  protected int flagColor;

  protected int maxIndex;

  public RTSEcgView(Context context) {
    this(context, null);
  }

  public RTSEcgView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RTSEcgView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initMy();
  }

  private void initMy() {
    //3 flag point paint
    flagColor = getContext().getResources().getColor(android.R.color.holo_red_dark);
    flagPaint = getPaintWithColor(flagColor, DensityUtils.dip2px(getContext(), grideMinorStrokeWidth));
    flagPaint.setStyle(Paint.Style.FILL_AND_STROKE);
  }

  public void showMarkPoint(boolean show) {
    this.show = show;
  }

  public void showStandardd(boolean drawStandard) {
    this.drawStandard = drawStandard;
  }

  @Override
  protected String getMartText() {
    return mGain + "mm/mV  25mm/S  " + mSamplerate + "HZ";
  }

  @Override
  protected void initSampleParams(float sampleLength) {
    super.initSampleParams(sampleLength);
    float mm_per_point = mSamplerate/25f;
    flagLength = (int) mm_per_point * 10;   //10mm的点数
  }

  @Override
  protected void drawVerticalGrid(Canvas canvas) {
    if (mDrawDirection == LEFT_IN_RIGHT_OUT) {
      //纵轴：最左边边开始向右画
      float leftX = gridMajorStrokeWidth / 2;
      int leftCount = 0;
      while (leftX < mWidth) {
        if (leftCount % 5 == 0) {
          canvas.drawLine(leftX, 0, leftX, mHeight, gridMajorPaint);
        } else {
          canvas.drawLine(leftX, 0, leftX, mHeight, gridMinorPaint);
        }
        leftCount++;
        leftX += pixelsPerMM;
      }
    } else {
      //纵轴：最右边开始向左画
      float leftX = mWidth - gridMajorStrokeWidth / 2;
      int leftCount = 0;
      while (leftX >= 0) {
        if (leftCount % 5 == 0) {
          canvas.drawLine(leftX, 0, leftX, mHeight, gridMajorPaint);
        } else {
          canvas.drawLine(leftX, 0, leftX, mHeight, gridMinorPaint);
        }
        leftCount++;
        leftX -= pixelsPerMM;
      }
    }
  }

  @Override
  protected void drawECGView(Canvas canvas) {
    synchronized (pointList) {

      if (pointList.size() == 0) {
        return;
      }

      if (mDrawDirection == LEFT_IN_RIGHT_OUT) {
        //从左向右绘制
        int minIndex = Math.max(0, maxIndex - maxDisplayPoints);
        for (int i = minIndex; i < maxIndex; i++) {
          curvePaint.setColor(pointList.get(i).color);
          //float x = mWidth - (maxIndex - i) * pixelsPerPoint;
          float x = i * pixelsPerPoint;

          convertMV2Yaxis(pointList.get(i));
          convertMV2Yaxis(pointList.get(i + 1));

          //if poitList.size() < maxDisplayPoints
          if (flagIndex == maxIndex && (i + 1) == flagIndex && show) {
            canvas.drawCircle(x, pointList.get(i).y, gridMajorStrokeWidth / 1.5f, flagPaint);
          }

          if (i != flagIndex) {

            if ((flagIndex + flagLength) > maxDisplayPoints) {
              //空白区域
              //1. flagIndex < i < maxDisplayPoints
              //2. 0 < i < (flagIndex + flagLength) % maxDisplayPoints
              if ((flagIndex < i && i < maxDisplayPoints)
                  || (0 < i && i < ((flagIndex + flagLength) % maxDisplayPoints))
              ) {
                /**空白不绘制*/
              } else {

                canvas.drawLine(x, pointList.get(i).y, x + pixelsPerPoint, pointList.get(i + 1).y, curvePaint);
              }

            } else {
              //空白区域
              //1. flagIndex < i, i < (flagIndex + flagLength)
              if (flagIndex < i && i < (flagIndex + flagLength)) {
                /**空白不绘制*/
              } else {

                canvas.drawLine(x, pointList.get(i).y, x + pixelsPerPoint, pointList.get(i + 1).y, curvePaint);
              }
            }

          } else if (show) {
            //绘制圆点
            canvas.drawCircle(x, pointList.get(i).y, gridMajorStrokeWidth / 1.5f, flagPaint);
          }
        }
      } else if(mDrawDirection == RIGHT_IN_LEFT_OUT) {
        //从右向左绘制
        int minIndex = Math.max(0, maxIndex - maxDisplayPoints);
        for (int i = maxIndex; i > minIndex; i--) {
          curvePaint.setColor(pointList.get(i).color);
          float x = mWidth - (maxIndex - i) * pixelsPerPoint;

          convertMV2Yaxis(pointList.get(i));
          convertMV2Yaxis(pointList.get(i - 1));

          //if poitList.size() < maxDisplayPoints
          if (flagIndex == minIndex && (i - 1) == flagIndex && show) {
            canvas.drawCircle(x, pointList.get(i - 1).y, gridMajorStrokeWidth / 1.5f, flagPaint);
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
              } else {

                canvas.drawLine(x, pointList.get(i).y, x - pixelsPerPoint, pointList.get(i - 1).y, curvePaint);
              }

            } else {
              //空白区域
              //(flagIndex - flagLength) < i < flagIndex
              if ((flagIndex - flagLength) < i && i < flagIndex) {
                /**空白不绘制*/
              } else {

                canvas.drawLine(x, pointList.get(i).y, x - pixelsPerPoint, pointList.get(i - 1).y, curvePaint);
              }
            }

          } else if (show) {
            //绘制圆点
            canvas.drawCircle(x, pointList.get(i).y, gridMajorStrokeWidth / 1.5f, flagPaint);
          }
        }
      }

      drawRTSTime(canvas);

    }
  }

  private void drawRTSTime(Canvas canvas) {

    if (pointList.size() <= 0) {
      return;
    }

    if (flagIndex < 0 || flagIndex >= pointList.size()) {
      return;
    }

    Paint.FontMetrics fm = markTextPaint.getFontMetrics();
    float textH = Math.abs(fm.leading + fm.ascent);
    String text = DateFormatUtils.format(pointList.get(flagIndex).time, DateFormat.sPattern);
    float textW = markTextPaint.measureText(text);
    canvas.drawText(text, mWidth - textW - gridMajorStrokeWidth, textH, markTextPaint);
  }

  public void addEcgPoint(EcgPoint point, boolean invalidate) {
    synchronized (pointList) {
      EcgPoint ecgPoint = ensureDeNullPoint(point);

      if (revert) {
        ecgPoint.mv = 0 - ecgPoint.mv;
      }

      if (mDrawDirection == LEFT_IN_RIGHT_OUT) {
        //左边往右边画
        if (pointList.size() < maxDisplayPoints) {
          pointList.add(ecgPoint);
          flagIndex = pointList.size() - 1;
        } else {
          flagIndex = (flagIndex + 1) % maxDisplayPoints;
          pointList.set(flagIndex, ecgPoint);
        }

        while (pointList.size() > maxDisplayPoints) {
          pointList.remove(pointList.size() - 1);
        }

      } else if (mDrawDirection == RIGHT_IN_LEFT_OUT) {
        //从右往左画
        if (pointList.size() < maxDisplayPoints) {
          pointList.add(0, ecgPoint);
          flagIndex = 0;
        } else {
          if (flagIndex == 0) {
            flagIndex = maxDisplayPoints - 1;
          } else {
            flagIndex = (flagIndex - 1) % maxDisplayPoints;
          }
          pointList.set(flagIndex, ecgPoint);
        }

        while (pointList.size() > maxDisplayPoints) {
          pointList.remove(0);
        }

      }

      maxIndex = pointList.size() - 1;

    }

    if (invalidate) {
      postInvalidate();
    }

  }

  public void addEcgPoint(EcgPoint point) {
    addEcgPoint(point, true);
  }

  public void addEcgPointList(List<EcgPoint> ecgPointList) {
    synchronized (pointList) {
      for (int i = 0; i < ecgPointList.size(); i++) {
        addEcgPoint(ecgPointList.get(i), false);
      }
    }
    postInvalidate();
  }

  public void setDrawDirection(int direction) {
    mDrawDirection = direction;
  }
}
