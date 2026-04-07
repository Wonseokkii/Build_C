package com.vivalnk.sdk.demo.vital.ui.device.o2.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;

import com.vivalnk.sdk.common.utils.DensityUtils;
import com.vivalnk.sdk.demo.base.utils.UnitUtils;
import com.vivalnk.sdk.demo.vital.R;

public abstract class WaveViewBase extends FrameLayout {

  //从左往右开始绘制
  public static final int LEFT_IN_RIGHT_OUT = 1;
  //从右往左开始绘制
  public static final int RIGHT_IN_LEFT_OUT = 2;

  protected int mDrawDirection = LEFT_IN_RIGHT_OUT;

  protected volatile int mWidth;
  protected volatile int mHeight;

  protected int screenWidth;
  protected int screenHeight;

  protected int desiredWidth;
  protected int desiredHeight;

  //裁剪圆角角度半径
  protected int mClipPathAngle_top = 50;
  protected int mClipPathAngle_bottom = 50;

  //粗线颜色
  protected int mGridColor;
  //细线颜色
  protected int mGridColorThin;
  //主网格线
  protected Paint gridMajorPaint;
  protected float gridMajorStrokeWidth;
  //次网格线
  protected Paint gridMinorPaint;
  protected float grideMinorStrokeWidth;

  //增益 移速绘制
  protected Paint markTextPaint;
  protected int markTextColor;

  protected Paint curvePaint;

  protected int curveColor;

  protected int flagLength; //空移长度

  protected int mSamplerate = 250;
  protected int amplitude = 10;
  protected boolean drawLine = true;

  //1unit = 1mm = 0.11mv
  // pixel/mm
  protected float pixelsPerUnit;              //pixels/mm
  protected float pixelsPerPoint;              //pixels/mm

  protected int maxDisplayPoints;

  public WaveViewBase(Context context) {
    this(context, null);
  }

  public WaveViewBase(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public WaveViewBase(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr, 0);
  }

  protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    setWillNotDraw(false);

    //init screen info
    DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
    this.screenWidth = dm.widthPixels;
    this.screenHeight = dm.heightPixels;
    desiredWidth = screenWidth;
    pixelsPerUnit = UnitUtils.getCM(getContext(), 0.1f);
    pixelsPerPoint = pixelsPerUnit / (mSamplerate / 25f);
    desiredHeight = (int) (pixelsPerUnit * 5 * 6);

    //init paint
    mGridColor = getContext().getResources().getColor(R.color.color_grid_line);
    mGridColorThin = getContext().getResources().getColor(R.color.color_grid_line_thin);
    curveColor = getContext().getResources().getColor(R.color.color_ecg_line);
    markTextColor = getContext().getResources().getColor(R.color.color_ecg_text);

    //1. grid paint
    //1.1 major paint
    gridMajorStrokeWidth = DensityUtils.dip2px(context, 0.5f);
    gridMajorPaint = getPaintWithColor(mGridColor, gridMajorStrokeWidth);
    //1.2 minor paint
    grideMinorStrokeWidth = DensityUtils.dip2px(context, 0.1f);
    gridMinorPaint = getPaintWithColor(mGridColorThin, grideMinorStrokeWidth);
    //2 wave line paint
    int curveStrokeWidth = DensityUtils.dip2px(context, 1f);
    curvePaint = getPaintWithColor(curveColor, curveStrokeWidth);

    //text paint
    markTextPaint = new Paint();
    markTextPaint.setColor(markTextColor);
    markTextPaint.setTextSize(DensityUtils.sp2px(context, 16));
  }

  public void setGridColor(@ColorInt int color) {
    mGridColor = color;
    gridMajorPaint = getPaintWithColor(mGridColor, gridMajorStrokeWidth);
    gridMinorPaint = getPaintWithColor(mGridColorThin, grideMinorStrokeWidth);
  }

  /** 设置上半部分圆角半径 */
  public void setGridCorner_TopRadius(int radius) {
    mClipPathAngle_top = radius;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);

    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    //Measure Width
    if (widthMode == MeasureSpec.EXACTLY) {
      //Must be this size
    } else if (widthMode == MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      widthSize = Math.min(desiredWidth, widthSize);
    } else {
      //Be whatever you want
      widthSize = desiredWidth;
    }

    //Measure Height
    if (heightMode == MeasureSpec.EXACTLY) {
      //Must be this size
    } else if (heightMode == MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      heightSize = Math.min(desiredHeight, heightSize);
    } else {
      //Be whatever you want
      heightSize = desiredHeight;
    }

    //MUST CALL THIS
    setMeasuredDimension(widthSize, (int) (heightSize + gridMajorStrokeWidth + 1));
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    this.mWidth = w;
    this.mHeight = h;
    this.maxDisplayPoints = (int) (mWidth / pixelsPerPoint);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  protected void onDraw(Canvas canvas) {
    //横轴：中间开始向两边画
    //中线
    if (drawLine) {
      drawClipPath(canvas);
      drawUpGrid(canvas);
      drawDownGrid(canvas);
      drawVerticalGrid(canvas);
    }
    drawECGView(canvas);
  //  drawMarkText(canvas);
  }

  //圆角裁剪路径
  protected void drawClipPath(Canvas canvas) {
    int angle_top = mClipPathAngle_top;
    int angle_bottom = mClipPathAngle_bottom;
    Path path = new Path();
    path.moveTo(angle_top, 0);
    path.lineTo(mWidth - angle_top, 0);
    path.quadTo(mWidth, 0, mWidth, angle_top);//第一个角
    path.lineTo(mWidth, mHeight - angle_top);
    path.quadTo(mWidth, mHeight, mWidth - angle_bottom, mHeight);//2
    path.lineTo(angle_bottom, mHeight);
    path.quadTo(0, mHeight, 0, mHeight - angle_bottom);//3
    path.lineTo(0, angle_top);
    path.quadTo(0, 0, angle_top, 0);//4
    canvas.clipPath(path);
  }

  /**
   * 纵轴：最右边开始向左画
   */
  protected void drawVerticalGrid(Canvas canvas) {
    if (mDrawDirection == LEFT_IN_RIGHT_OUT) {
      //纵轴：最左边边开始向右画
      float x = gridMajorStrokeWidth / 2;
      int count = 0;
      while (x < mWidth) {
        if (count % 5 == 0) {
          canvas.drawLine(x, 0, x, mHeight, gridMajorPaint);
        } else {
          canvas.drawLine(x, 0, x, mHeight, gridMinorPaint);
        }
        count++;
        x += pixelsPerUnit;
      }
    } else {
      //纵轴：最右边开始向左画
      float x = mWidth - gridMajorStrokeWidth / 2;
      int count = 0;
      while (x >= 0) {
        if (count % 5 == 0) {
          canvas.drawLine(x, 0, x, mHeight, gridMajorPaint);
        } else {
          canvas.drawLine(x, 0, x, mHeight, gridMinorPaint);
        }
        count++;
        x -= pixelsPerUnit;
      }
    }
  }

  /**
   * 绘制下半部分网格
   */
  protected void drawDownGrid(Canvas canvas) {
    float y = mHeight / 2.0f;
    int count = 0;
    while (y <= mHeight) {
      if (count % 5 == 0) {
        canvas.drawLine(0, y, mWidth, y, gridMajorPaint);
      } else {
        canvas.drawLine(0, y, mWidth, y, gridMinorPaint);
      }
      count++;
      y += pixelsPerUnit;
    }
  }

  /**
   * 绘制上半部分网格
   */
  protected void drawUpGrid(Canvas canvas) {
    float y = mHeight / 2.0f;
    int upCount = 0;
    while (y >= 0) {
      if (upCount % 5 == 0) {
        canvas.drawLine(0, y, mWidth, y, gridMajorPaint);
      } else {
        canvas.drawLine(0, y, mWidth, y, gridMinorPaint);
      }
      upCount++;
      y -= pixelsPerUnit;
    }
  }

  private void drawMarkText(Canvas canvas) {
    Paint.FontMetrics fm = markTextPaint.getFontMetrics();
    float textW = markTextPaint.measureText(getMartText());
    float textH = Math.abs(fm.leading + fm.ascent);
    canvas.drawText(getMartText(), (mWidth - textW) / 2, mHeight - textH, markTextPaint);
  }

  protected String getMartText() {
    return "10 mm/mV   25 mm/s";
  }

  protected abstract void drawECGView(Canvas canvas);

  public void setSampleRate(int sampleRate) {
    this.mSamplerate = sampleRate;
    pixelsPerPoint = pixelsPerUnit / (mSamplerate / 25f);
    this.maxDisplayPoints = (int) (mWidth / pixelsPerPoint);
    this.flagLength = (int) (mSamplerate / 40f) * 10;   //10mm的点数
    postInvalidate();
  }
  
  public void drawLine(boolean isDraw) {
    drawLine = isDraw;
    postInvalidate();
  }

  public int getSamplerate() {
    return mSamplerate;
  }

  public void updateAmplitude(int amplitude){
    if (amplitude > 0) {
      this.amplitude = amplitude;
      postInvalidate();
    }
  }

  protected final float mTopOffsetY = 180.0f;
  protected float convertY(float ecg) {
//    return (mHeight + mTopOffsetY) / 2.0f - ecg * pixelsPerUnit * amplitude ;
    return mHeight / 2.0f - ecg * pixelsPerUnit * amplitude;
  }

  public void setDrawDirection(int direction) {
    mDrawDirection = direction;
  }

  protected Paint getPaintWithColor(@ColorInt int color, float strokeWidth) {
    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setColor(color);
    paint.setStrokeWidth(strokeWidth);
    return paint;
  }
}
