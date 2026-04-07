package com.vivalnk.sdk.demo.vital.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.autofill.AutofillManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;

/**
 * @author: Billows
 * @date: 2023/7/26 16:02
 */
public class MyAppCompatCheckBox extends AppCompatCheckBox {
    public MyAppCompatCheckBox(@NonNull Context context) {
        super(context);
    }

    public MyAppCompatCheckBox(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyAppCompatCheckBox(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private OnCheckedChangeListener mListener;

    @Override
    public void setOnCheckedChangeListener(final OnCheckedChangeListener listener) {
        mListener = listener;
        super.setOnCheckedChangeListener(listener);
    }

    public void setChecked(final boolean checked, final boolean alsoNotify) {
        if (!alsoNotify) {
            super.setOnCheckedChangeListener(null);
            super.setChecked(checked);
            super.setOnCheckedChangeListener(mListener);
            return;
        }
        super.setChecked(checked);
    }

    public void toggle(boolean alsoNotify) {
        if (!alsoNotify) {
            super.setOnCheckedChangeListener(null);
            super.toggle();
            super.setOnCheckedChangeListener(mListener);
        }
        super.toggle();
    }
}
