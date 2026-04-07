package com.vivalnk.sdk.demo.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetrics;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import com.vivalnk.sdk.utils.DateFormat;
import java.util.List;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Created by JakeMo on 18-5-27.
 */
public class DragEcgView extends AbsEcgView {

  private GestureDetectorCompat mDetector;
  private OverScroller mScroller;
  /* Positions of the last motion event */
  private float mInitialX, mInitialY;
  /* Drag threshold */
  private int mTouchSlop;

  private int maxIndex;

  private SimpleOnGestureListener mListener = new SimpleOnGestureListener() {
    public boolean onDown(MotionEvent e) {
      if (!mScroller.isFinished()) {
        mScroller.abortAnimation();
      }
      return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      fling((int) -velocityX, (int) -velocityY);
      return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      updateView(distanceX, distanceY);
      return true;
    }

  };

  public DragEcgView(Context context) {
    this(context, null);
  }

  public DragEcgView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DragEcgView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initScroller(context, attrs, defStyleAttr, 0);
  }

  protected void initScroller(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    mDetector = new GestureDetectorCompat(getContext(), mListener);
    mScroller = new OverScroller(getContext());
    //Get system constants for touch thresholds
    mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mInitialX = event.getX();
        mInitialY = event.getY();
        break;
      case MotionEvent.ACTION_MOVE:
        final float x = event.getX();
        final float y = event.getY();
        final int yDiff = (int) Math.abs(y - mInitialY);
        final int xDiff = (int) Math.abs(x - mInitialX);
        //Verify that either difference is enough to be a drag
        if (yDiff > mTouchSlop || xDiff > mTouchSlop) {
          //Start capturing events
          return true;
        }
        break;
    }

    return super.onInterceptTouchEvent(event);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return mDetector.onTouchEvent(event);
  }

  /*
   * Utility method to initialize the Scroller and start redrawing
   */
  public void fling(int velocityX, int velocityY) {
    int max = (int) Math.max(0, pixelsPerPoint * pointList.size());
    mScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY,
        -max, max,
        0, 0);
    //Y轴不移动

    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    FontMetrics fm = markTextPaint.getFontMetrics();
    if (pointList.size() > 0) {
      String text = DateFormatUtils.format(pointList.get(maxIndex).time, DateFormat.sPattern);
      canvas.drawText(text, mWidth - markTextPaint.measureText(text) - 5, Math.abs(fm.leading + fm.ascent),
          markTextPaint);
    }
  }

  @Override
  protected String getMartText() {
    return mGain + "mm/mV  " + mSamplerate + "HZ";
  }

  @Override
  protected void drawECGView(Canvas canvas) {
    //从右向左绘制
    synchronized (pointList) {
      int minIndex = Math.max(0, maxIndex - maxDisplayPoints);
      for (int i = maxIndex; i > minIndex; i--) {
        curvePaint.setColor(pointList.get(i).color);
        float x = mWidth - (maxIndex - i) * pixelsPerPoint;
        convertMV2Yaxis(pointList.get(i));
        convertMV2Yaxis(pointList.get(i - 1));
        canvas.drawLine(x, pointList.get(i).y, x - pixelsPerPoint, pointList.get(i - 1).y, curvePaint);
      }
    }

  }

  public void addEcgPointList(List<EcgPoint> ecgPointList) {
    synchronized (pointList) {
      if (revert) {
        for (int i = 0; i < ecgPointList.size(); i++) {
          ecgPointList.get(i).mv = 0 - ecgPointList.get(i).mv;
        }
      }
      pointList.addAll(ecgPointList);
      maxIndex = pointList.size() - 1;
    }
    postInvalidate();
  }

  public void addEcgPoint(EcgPoint point, boolean invalidate) {
    synchronized (pointList) {
      while (pointList.size() > maxDisplayPoints) {
        pointList.remove(0);
      }
      //将新坐标添加到列表末尾
      EcgPoint pointInput = ensureDeNullPoint(point);
      if (revert) {
        pointInput.mv = 0 - pointInput.mv;
      }
      pointList.add(pointInput);
      maxIndex = pointList.size() - 1;
    }

    if (invalidate) {
      //开始绘图
      postInvalidate();
    }

  }


  @Override
  public void scrollTo(int x, int y) {
    super.scrollTo(x, y);
  }

  @Override
  public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      int x = mScroller.getCurrX();
      int y = mScroller.getCurrY();
      updateView(x, y);
      postInvalidateOnAnimation();
    }
  }

  private void updateView(float distanceX, float distanceY) {

    int offset = (int) (distanceX / pixelsPerPoint);  //偏移量

    //向左滑动
    if (distanceX > 0) {
      maxIndex = Math.min(pointList.size() - 1, maxIndex + offset); //offset > 0
    } else {
    //向右滑动
      maxIndex = Math.max(Math.min(pointList.size() - 1, maxDisplayPoints), maxIndex + offset);
    }
    postInvalidateOnAnimation();
  }


}
