package com.vivalnk.sdk.demo.base.app;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.view.Window;
import com.vivalnk.sdk.demo.base.R;
import com.vivalnk.sdk.demo.base.mvp.IPresenter;

/**
 * Created by JakeMo on 18-4-28.
 */
public abstract class BaseToolbarActivity<P extends IPresenter> extends BaseActivity<P> {

  protected Toolbar toolbar;
  protected ActionBar actionBar;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    initToolbar();
  }

  private void initToolbar() {
    toolbar = findViewById(R.id.toolbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
      actionBar = getSupportActionBar();
      if (actionBar != null) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
      }
    }
  }

}
