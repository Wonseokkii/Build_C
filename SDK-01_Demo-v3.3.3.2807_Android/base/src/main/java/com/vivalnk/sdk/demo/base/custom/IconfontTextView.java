package com.vivalnk.sdk.demo.base.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.vivalnk.sdk.demo.base.R;

public class IconfontTextView extends AppCompatTextView {

  public IconfontTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public IconfontTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public IconfontTextView(Context context) {
    super(context);
    init();
  }

  private void init() {
    //Font name should not contain "/".
    Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "iconfont.ttf");
    setTypeface(tf);
  }

}


