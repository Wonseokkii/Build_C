package com.vivalnk.sdk.demo.base.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by JakeMo on 17-11-21.
 */

public class UnitUtils {

  /**
   * 获取px值
   *
   * @param cmNUM 多少厘米
   */
  public static int getCM(Context context, float cmNUM) {
    DisplayMetrics dm = context.getResources().getDisplayMetrics();
    Unit unit = new Unit(dm.ydpi);
    //1cm的像素点
    return (int) (cmNUM * unit.getPixelsPerUnit());
  }

  public static class Unit {

    private float dpi;

    Unit(float dpi) {
      this.dpi = dpi;
    }

    //一厘米的像素点
    public float getPixelsPerUnit() {
      return dpi / 2.54f;
    }
  }

}
