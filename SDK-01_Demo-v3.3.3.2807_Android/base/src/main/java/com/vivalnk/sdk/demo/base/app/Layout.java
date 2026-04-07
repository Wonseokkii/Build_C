package com.vivalnk.sdk.demo.base.app;

import android.view.View;

public class Layout {
  final static int intType = 1;
  final static int viewType = 2;
  public int type;
  public Object value;
  private Layout(int type, Object value) {
    this.type = type;
    this.value = value;
  }

  public static Layout createLayoutByID(int id) {
    return new Layout(intType, id);
  }

  public static Layout createLayoutByView(View view) {
    return new Layout(viewType, view);
  }
}
