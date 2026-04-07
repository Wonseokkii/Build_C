package com.vivalnk.sdk.demo.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import com.vivalnk.sdk.demo.base.R;
import com.vivalnk.sdk.demo.base.utils.UnitUtils;
import com.vivalnk.sdk.common.utils.DensityUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JakeMo on 18-5-2.
 */
public abstract class AbsEcgView extends FrameLayout {

  public static final String TAG = "EcgView";

  protected volatile int mWidth;
  protected volatile int mHeight;

  protected int screenWidth;
  protected int screenHeight;

  protected float desiredWidth;
  protected float desiredHeight;

  protected int mGridColor;
  //主网格线
  protected Paint gridMajorPaint;
  protected float gridMajorStrokeWidth = DensityUtils.dip2px(getContext(), 2F);
  //次网格线
  protected Paint gridMinorPaint;
  protected float grideMinorStrokeWidth = DensityUtils.dip2px(getContext(), 1F);

  //增益 移速绘制
  protected Paint markTextPaint;
  protected int markTextColor;
  protected int markTextSize = DensityUtils.dip2px(getContext(), 16F);

  protected Paint curvePaint;

  protected int curveColor;
  protected int curveColorTransparent;

  protected int mSamplerate = 250;
  //protected float mSampleTotalTime = 1000f; //采样总时间(ms)

  protected int mGain = 10;

  //1unit = 1mm = 0.11mv
  // pixel/mm
  protected float pixelsPerMM;              //pixels/mm
  protected float pixelsPerPoint;              //pixels/point
  // mv/mm
  protected float mvPerUnit = 0.1f;           //mv/mm
  // ms/mm
  //protected float timePerPoint = mSampleTotalTime / mSamplerate;  //ms/per_point

  protected int maxDisplayPoints;

  protected boolean drawStandard = true;

  protected boolean revert;

  //最右边的点的坐标index
  //protected int maxIndex;


  //所需要绘制的坐标点
  protected ArrayList<EcgPoint> pointList;

  public AbsEcgView(Context context) {
    this(context, null);
  }

  public AbsEcgView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AbsEcgView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    pixelsPerMM = UnitUtils.getCM(getContext(), 0.1f);
    pixelsPerPoint = pixelsPerMM / (mSamplerate / 25f);
    desiredHeight = pixelsPerMM * 5 * 6;

    //init paint
    mGridColor = getContext().getResources().getColor(R.color.color_grid_line);
    curveColor = getContext().getResources().getColor(R.color.black);
    markTextColor = getContext().getResources().getColor(R.color.colorPrimary);

    //1. grid paint
    //1.1 major paint
    gridMajorPaint = getPaintWithColor(mGridColor, gridMajorStrokeWidth);
    //1.2 minor paint
    gridMinorPaint = getPaintWithColor(mGridColor, grideMinorStrokeWidth);
    //2 wave line paint
    curvePaint = getPaintWithColor(curveColor, grideMinorStrokeWidth * 1.5F);

    //text paint
    markTextPaint = new Paint();
    markTextPaint.setColor(curveColor);
    markTextPaint.setTextSize(markTextSize);

    //
    pointList = new ArrayList<>();

  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//    //TODO
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);

    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    float width = widthSize;
    float height = heightSize;

    //Measure Width
    if (widthMode == MeasureSpec.EXACTLY) {
      //Must be this size
      width = widthSize;
    } else if (widthMode == MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      width = Math.min(desiredWidth, widthSize);
    } else {
      //Be whatever you want
      width = desiredWidth;
    }

    //Measure Height
    if (heightMode == MeasureSpec.EXACTLY) {
      //Must be this size
      height = heightSize;
    } else if (heightMode == MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      height = Math.min(desiredHeight, heightSize);
    } else {
      //Be whatever you want
      height = desiredHeight;
    }

    //MUST CALL THIS
    setMeasuredDimension((int) width, (int) (height + gridMajorStrokeWidth + 1));
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    this.mWidth = w;
    this.mHeight = h;
    this.maxDisplayPoints = (int) (mWidth / pixelsPerPoint);
    if (maxDisplayPoints == 0) {
      maxDisplayPoints = (int) (128 * 4.5);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    //横轴：中间开始向两边画
    //中线
    drawUpGrid(canvas);
    //绘制下半部分网格
    drawDownGrid(canvas);
    //绘制纵轴
    drawVerticalGrid(canvas);

    drawECGView(canvas);
    drawMarkText(canvas);
  }

  protected void drawVerticalGrid(Canvas canvas) {
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

  protected void drawDownGrid(Canvas canvas) {
    float downY = mHeight / 2.0f;
    int downCount = 0;
    while (downY <= mHeight) {
      if (downCount % 5 == 0) {
        canvas.drawLine(0, downY, mWidth, downY, gridMajorPaint);
      } else {
        canvas.drawLine(0, downY, mWidth, downY, gridMinorPaint);
      }
      downCount++;
      downY += pixelsPerMM;
    }
  }

  protected void drawUpGrid(Canvas canvas) {
    //绘制上半部分网格
    float upY = mHeight / 2.0f;
    int upCount = 0;
    while (upY >= 0) {
      if (upCount % 5 == 0) {
        canvas.drawLine(0, upY, mWidth, upY, gridMajorPaint);
      } else {
        canvas.drawLine(0, upY, mWidth, upY, gridMinorPaint);
      }
      upCount++;
      upY -= pixelsPerMM;
    }
  }

  private void drawMarkText(Canvas canvas) {
    Paint.FontMetrics fm = markTextPaint.getFontMetrics();
    float textH = Math.abs(fm.leading + fm.ascent);
    canvas.drawText(getMartText(), 0, textH, markTextPaint);


    if (drawStandard) {
      canvas.drawText("0mV", 0, mHeight/2 + textH/2, markTextPaint);
      drawStandard(canvas);
    }

  }

  private void drawStandard(Canvas canvas) {

    float lowH = mHeight / 2 - 5 * pixelsPerMM;
    float hightH = lowH - mGain * pixelsPerMM;

    gridMinorPaint.setColor(curveColor);
    canvas.drawLine(0, lowH, 1 * pixelsPerMM, lowH, gridMinorPaint);
    canvas.drawLine(1 * pixelsPerMM, lowH, 1 * pixelsPerMM, hightH, gridMinorPaint);
    canvas.drawLine(1 * pixelsPerMM, hightH, 6 * pixelsPerMM, hightH, gridMinorPaint);
    canvas.drawLine(6 * pixelsPerMM, hightH, 6 * pixelsPerMM, lowH, gridMinorPaint);
    canvas.drawLine(6 * pixelsPerMM, lowH, 7 * pixelsPerMM, lowH, gridMinorPaint);
    gridMinorPaint.setColor(mGridColor);

  }

  protected abstract String getMartText();
  protected abstract void drawECGView(Canvas canvas);

  public void clearDataList() {
    synchronized (pointList) {
      pointList.clear();
      postInvalidate();
    }
  }

  public void addEcgPointList(List<EcgPoint> ecgPointList) {
    synchronized (pointList) {
      for (int i = 0; i < ecgPointList.size(); i++) {
        EcgPoint point = ensureDeNullPoint(ecgPointList.get(i));
        if (revert) {
          point.mv = 0 - point.mv;
        }
        ecgPointList.set(i, point);
      }
      pointList.addAll(ecgPointList);
    }
    postInvalidate();
  }

  public ArrayList<EcgPoint> getEcgPointList() {
    return pointList;
  }

  public void setSampleRate(int sampleRate, int deltaTime) {
    this.mSamplerate = sampleRate;
    float sampleLength = deltaTime / 1000f * 25f; //根据25mm适配
    initSampleParams(sampleLength);
    postInvalidate();
  }

  public void setSampleRate(int sampleRate) {
    setSampleRate(sampleRate, 1000);
  }

  protected void initSampleParams(float sampleLength) {
    pixelsPerPoint = pixelsPerMM / (mSamplerate / sampleLength);
    maxDisplayPoints = (int) (mWidth / pixelsPerPoint);
    if (maxDisplayPoints == 0) {
      maxDisplayPoints = (int) (128 * 4.5);
    }
  }

  public void setGain(int gain) {
    this.mGain = gain;
    postInvalidate();
  }

  protected void convertMV2Yaxis(EcgPoint point) {
    point.y = mHeight / 2.0f - point.mv * pixelsPerMM * mGain;
  }

  protected EcgPoint ensureDeNullPoint(EcgPoint ecgPoint) {
    if (ecgPoint != null) {
      return ecgPoint;
    }
    //若为null，则代表当前时间点坐标为空，绘制透明
    EcgPoint emptyPoint = new EcgPoint();
    emptyPoint.color = getResources().getColor(R.color.color_grid_transparent);
    return emptyPoint;
  }


  protected Paint getPaintWithColor(@ColorInt int color, float strokeWidth) {
    Paint paint = new Paint();
    paint.setColor(color);
    paint.setAntiAlias(true);
    paint.setStrokeWidth(strokeWidth);
    paint.setStyle(Style.FILL);
    return paint;
  }

  protected Paint getPaintWithColorID(@ColorRes int colorID, float strokeWidth) {
    int color = getContext().getResources().getColor(colorID);
    return getPaintWithColor(color, strokeWidth);
  }

  public void switchGain() {
    mGain = mGain == 5 ? 10 : 5;
    postInvalidate();
  }

  public void revert(boolean revert) {
    this.revert = revert;

    synchronized (pointList) {
      for (int i = 0; i < pointList.size(); i++) {
        EcgPoint point = pointList.get(i);
        point.mv = 0 - point.mv;
      }
    }

    postInvalidate();
  }
}
