package com.vivalnk.sdk.demo.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import com.vivalnk.sdk.model.Motion;

public class AccView extends View {

  private static final String TAG = "AccView";;

  private Paint paintEdge;
  private Paint paintText;
  private Paint paintFltCurve;
  private Paint paintFltCurveTransparent;

  private int mSamplerate;

  private final Motion[] xyzList = new Motion[1000];
  private int mValidXYZCount = 0;
  private int mXYZEndPosition = 0;

  private float mACCHeight;

  /**
   *
   * @param context
   *
   */
  public AccView(Context context) {
    super(context);
    init(context);
  }

  public AccView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    paintEdge = new Paint();
    paintEdge.setColor(Color.parseColor("#1b4200"));
    paintEdge.setStrokeWidth(5);

    paintText = new Paint();
    paintText.setColor(Color.WHITE);
    paintText.setTextSize(32.0f);

    paintFltCurve = new Paint();
    paintFltCurve.setColor(Color.parseColor("#76f112"));
    paintFltCurve.setStrokeWidth(2);

    paintFltCurveTransparent = new Paint();
    paintFltCurveTransparent.setColor(Color.TRANSPARENT);
    paintFltCurveTransparent.setStrokeWidth(2);

    mSamplerate = 250;
  }

  private int getXYZValueYPoint(int nValue, int nMin, int nMax) {
    return (int) (mACCHeight - mACCHeight * (nValue - nMin) / (nMax - nMin));
  }

  @Override
  public void onDraw(Canvas canvas) {

    canvas.drawColor(Color.BLACK);

    DisplayMetrics dm = getResources().getDisplayMetrics();
    Log.i(TAG, "Screen width: " + dm.widthPixels + ", height: " + dm.heightPixels
        + ", xdpi: " + dm.xdpi + ", ydpi: " + dm.ydpi);
    float fXPixelsPerCm = dm.xdpi / 2.54f;
    float fGridMinorWidth = fXPixelsPerCm * 0.1f;
    float fXPixelsPerSample = 0.0f;
    if (125 == mSamplerate) {
      fXPixelsPerSample = 2.5f * fXPixelsPerCm / 125.0f;
    } else {
      fXPixelsPerSample = 2.5f * fXPixelsPerCm / 250.0f;
    }

    // draw Edge
    float fEdgeLeft = 10.0f;
    float fEdgeTop = 10.0f;
    float fEdgeWidth = fGridMinorWidth * 100;
    // float fEdgeHeight = 880.0f;
    float fEdgeHeight = dm.heightPixels - 200.0f;

    float xyzHeight = (fEdgeHeight - 10) / 3.1f;
    mACCHeight = xyzHeight;

      canvas.drawLine(fEdgeLeft, fEdgeTop, fEdgeLeft + fEdgeWidth, fEdgeTop, paintEdge); // top
      // edge
      canvas.drawLine(fEdgeLeft, fEdgeTop, fEdgeLeft, fEdgeTop + xyzHeight, paintEdge); // left
      // edge
      canvas.drawLine(fEdgeLeft, fEdgeTop + xyzHeight, fEdgeLeft + fEdgeWidth, fEdgeTop
          + xyzHeight, paintEdge); // bottom edge
      canvas.drawLine(fEdgeLeft + fEdgeWidth, fEdgeTop + xyzHeight, fEdgeLeft + fEdgeWidth,
          fEdgeTop, paintEdge); // right edge

      canvas.drawLine(fEdgeLeft, fEdgeTop + xyzHeight + 5f, fEdgeLeft + fEdgeWidth,
          fEdgeTop + xyzHeight + 5, paintEdge); // top
      // edge
      canvas
          .drawLine(fEdgeLeft, fEdgeTop + xyzHeight + 5f, fEdgeLeft, fEdgeTop + 2 * xyzHeight + 5f,
              paintEdge); // left
      // edge
      canvas.drawLine(fEdgeLeft, fEdgeTop + 2 * xyzHeight + 5f, fEdgeLeft + fEdgeWidth, fEdgeTop
          + 2 * xyzHeight + 5f, paintEdge); // bottom edge
      canvas.drawLine(fEdgeLeft + fEdgeWidth, fEdgeTop + xyzHeight + 5f, fEdgeLeft + fEdgeWidth,
          fEdgeTop + 2 * xyzHeight + 5f, paintEdge); // right edge

      canvas.drawLine(fEdgeLeft, fEdgeTop + 2 * xyzHeight + 2 * 5f, fEdgeLeft + fEdgeWidth,
          fEdgeTop + 2 * xyzHeight + 2 * 5f, paintEdge); // top
      // edge
      canvas.drawLine(fEdgeLeft, fEdgeTop + 2 * xyzHeight + 2 * 5f, fEdgeLeft,
          fEdgeTop + 3 * xyzHeight + 2 * 5f, paintEdge); // left
      // edge
      canvas.drawLine(fEdgeLeft, fEdgeTop + 3 * xyzHeight + 2 * 5f, fEdgeLeft + fEdgeWidth, fEdgeTop
          + 3 * xyzHeight + 2 * 5f, paintEdge); // bottom edge
      canvas.drawLine(fEdgeLeft + fEdgeWidth, fEdgeTop + 2 * xyzHeight + 2 * 5f,
          fEdgeLeft + fEdgeWidth,
          fEdgeTop + 3 * xyzHeight + 2 * 5f, paintEdge); // right edge

    int nMinX = -2100;
    int nMaxX = 2100;
    int nMinY = -2100;
    int nMaxY = 2100;
    int nMinZ = -2100;
    int nMaxZ = 2100;

     /* if (mValidXYZCount != 0) {
        for (int i = 0; i < mValidXYZCount; i++) {
          if (nMinX > xyzList[i].getX()) {
            nMinX = xyzList[i].getX();
          }
          if (nMaxX < xyzList[i].getX()) {
            nMaxX = xyzList[i].getX();
          }

          if (nMinY > xyzList[i].getY()) {
            nMinY = xyzList[i].getY();
          }
          if (nMaxY < xyzList[i].getY()) {
            nMaxY = xyzList[i].getY();
          }

          if (nMinZ > xyzList[i].getZ()) {
            nMinZ = xyzList[i].getZ();
          }
          if (nMaxZ < xyzList[i].getZ()) {
            nMaxZ = xyzList[i].getZ();
          }
        }

        if (nMinX == nMaxX) {
          if (nMinX > 0) {
            nMinX = 0;
          } else if (nMinX < 0) {
            nMaxX = 0;
          } else {
            nMinX = -100;
            nMaxX = 100;
          }
        }

        if (nMinY == nMaxY) {
          if (nMinY > 0) {
            nMinY = 0;
          } else if (nMinY < 0) {
            nMaxY = 0;
          } else {
            nMinY = -100;
            nMaxY = 100;
          }
        }

        if (nMinZ == nMaxZ) {
          if (nMinZ > 0) {
            nMinZ = 0;
          } else if (nMinZ < 0) {
            nMaxZ = 0;
          } else {
            nMinZ = -100;
            nMaxZ = 100;
          }
        }
      }
*/
      canvas.drawText(String.valueOf(nMaxX), fEdgeLeft + 10, fEdgeTop + 20, paintText);
      canvas.drawText(String.valueOf(nMinX), fEdgeLeft + 10, fEdgeTop + xyzHeight - 20, paintText);
      if (mXYZEndPosition == 0 || xyzList[mXYZEndPosition - 1] == null) {
        canvas.drawText("X: 0", fEdgeLeft + fEdgeWidth - 400, fEdgeTop + 50, paintText);
      } else {
        canvas.drawText("X: " + xyzList[mXYZEndPosition - 1].getX(), fEdgeLeft + fEdgeWidth - 400,
            fEdgeTop + 50, paintText);
      }

      canvas.drawText(String.valueOf(nMaxY), fEdgeLeft + 10, fEdgeTop + xyzHeight + 5 + 20,
          paintText);
      canvas.drawText(String.valueOf(nMinY), fEdgeLeft + 10, fEdgeTop + 2 * xyzHeight + 5 - 20,
          paintText);
      if (mXYZEndPosition == 0 || xyzList[mXYZEndPosition - 1] == null) {
        canvas.drawText("Y: 0", fEdgeLeft + fEdgeWidth - 400, fEdgeTop + xyzHeight + 5 + 50,
            paintText);
      } else {
        canvas.drawText("Y: " + xyzList[mXYZEndPosition - 1].getY(), fEdgeLeft + fEdgeWidth - 400,
            fEdgeTop + xyzHeight + 5 + 50, paintText);
      }

      canvas.drawText(String.valueOf(nMaxZ), fEdgeLeft + 10, fEdgeTop + 2 * xyzHeight + 2 * 5 + 20,
          paintText);
      canvas.drawText(String.valueOf(nMinZ), fEdgeLeft + 10, fEdgeTop + 3 * xyzHeight + 2 * 5 - 20,
          paintText);
      if (mXYZEndPosition == 0 || xyzList[mXYZEndPosition - 1] == null) {
        canvas.drawText("Z: 0", fEdgeLeft + fEdgeWidth - 400, fEdgeTop + 2 * xyzHeight + 2 * 5 + 50,
            paintText);
      } else {
        canvas.drawText("Z: " + xyzList[mXYZEndPosition - 1].getZ(), fEdgeLeft + fEdgeWidth - 400,
            fEdgeTop + 2 * xyzHeight + 2 * 5 + 50, paintText);
      }

      for (int i = 0; i < mValidXYZCount - 1; i++) {
        drawLine(canvas, i, fEdgeLeft, fEdgeTop, fXPixelsPerSample, xyzList[i].getX(), xyzList[i + 1].getX(), 0);
        drawLine(canvas, i, fEdgeLeft, fEdgeTop, fXPixelsPerSample, xyzList[i].getY(), xyzList[i + 1].getY(), xyzHeight + 5f);
        drawLine(canvas, i, fEdgeLeft, fEdgeTop, fXPixelsPerSample, xyzList[i].getZ(), xyzList[i + 1].getZ(), 2 * xyzHeight + 2 * 5f);
      /*  // The filtered curve
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
            getXYZValueYPoint(xyzList[i].getX(), nMinX, nMaxX), fEdgeLeft + (i + 1)
                * fXPixelsPerSample,
            getXYZValueYPoint(xyzList[i + 1].getX(), nMinX, nMaxX), paintFltCurve);

        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
            getXYZValueYPoint(xyzList[i].getY(), nMinY, nMaxY) + xyzHeight + 5f, fEdgeLeft + (i + 1)
                * fXPixelsPerSample,
            getXYZValueYPoint(xyzList[i + 1].getY(), nMinY, nMaxY) + xyzHeight + 5f, paintFltCurve);

        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
            getXYZValueYPoint(xyzList[i].getZ(), nMinZ, nMaxZ) + 2 * xyzHeight + 2 * 5f,
            fEdgeLeft + (i + 1)
                * fXPixelsPerSample,
            getXYZValueYPoint(xyzList[i + 1].getZ(), nMinZ, nMaxZ) + 2 * xyzHeight + 2 * 5f,
            paintFltCurve);*/


      }

    // update
    super.onDraw(canvas);
  }

  private void drawLine(Canvas canvas, int i, float fEdgeLeft, float fEdgeTop, float fXPixelsPerSample, int start, int end, float offset) {
    int nMinX = -2100;
    int nMaxX = 2100;
    offset = offset + fEdgeTop;
    if (start > nMaxX) {
      if (end > nMaxX) {
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(start, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(end, nMinX, nMaxX) + offset, paintFltCurveTransparent);
      } else if (end < nMinX) {
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(start, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(nMaxX, nMinX, nMaxX) + offset, paintFltCurveTransparent);
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(nMaxX, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(nMinX, nMinX, nMaxX) + offset, paintFltCurve);
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(nMinX, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(end, nMinX, nMaxX) + offset, paintFltCurveTransparent);
      } else {
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(start, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(nMaxX, nMinX, nMaxX) + offset, paintFltCurveTransparent);
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(nMaxX, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(end, nMinX, nMaxX) + offset, paintFltCurve);
      }
    } else if (start < nMinX) {
        if (end > nMaxX) {
          canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                  getXYZValueYPoint(start, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                          * fXPixelsPerSample,
                  getXYZValueYPoint(nMinX, nMinX, nMaxX) + offset, paintFltCurveTransparent);
          canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                  getXYZValueYPoint(nMinX, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                          * fXPixelsPerSample,
                  getXYZValueYPoint(nMaxX, nMinX, nMaxX) + offset, paintFltCurve);
          canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                  getXYZValueYPoint(nMaxX, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                          * fXPixelsPerSample,
                  getXYZValueYPoint(end, nMinX, nMaxX) + offset, paintFltCurveTransparent);
        } else if (end < nMinX) {
          canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                  getXYZValueYPoint(start, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                          * fXPixelsPerSample,
                  getXYZValueYPoint(end, nMinX, nMaxX) + offset, paintFltCurveTransparent);
        } else {
          canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                  getXYZValueYPoint(start, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                          * fXPixelsPerSample,
                  getXYZValueYPoint(nMinX, nMinX, nMaxX) + offset, paintFltCurveTransparent);
          canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                  getXYZValueYPoint(nMinX, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                          * fXPixelsPerSample,
                  getXYZValueYPoint(end, nMinX, nMaxX) + offset, paintFltCurve);
        }
    } else {
      if (end > nMaxX) {
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(start, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(nMaxX, nMinX, nMaxX) + offset, paintFltCurve);
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(nMaxX, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(end, nMinX, nMaxX) + offset, paintFltCurveTransparent);
      } else if (end < nMinX) {
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(start, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(nMinX, nMinX, nMaxX) + offset, paintFltCurve);
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(nMinX, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(end, nMinX, nMaxX) + offset, paintFltCurveTransparent);
      } else {
        canvas.drawLine(fEdgeLeft + i * fXPixelsPerSample,
                getXYZValueYPoint(start, nMinX, nMaxX) + offset, fEdgeLeft + (i + 1)
                        * fXPixelsPerSample,
                getXYZValueYPoint(end, nMinX, nMaxX) + offset, paintFltCurve);
      }
    }
  }

  public void addAccData(Motion[] acc) {

    if (acc == null || acc.length == 0) {
      return;
    }

    for (int i = 0; i < acc.length; i++) {
      xyzList[mXYZEndPosition] = acc[i];

      mXYZEndPosition++;
      if (mXYZEndPosition >= 1000) {
        mXYZEndPosition = 0;
      }

      if (mValidXYZCount < 1000) {
        mValidXYZCount++;
      }
    }
    postInvalidate();
  }

}