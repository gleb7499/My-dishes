package com.mydishes.mydishes.utils;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ViewUtils {

    public static void applyInsets(View targetView, boolean top, boolean bottom, boolean left, boolean right) {
        ViewCompat.setOnApplyWindowInsetsListener(targetView, (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (top) params.topMargin += systemInsets.top;
            if (bottom) params.bottomMargin += systemInsets.bottom;
            if (left) params.leftMargin += systemInsets.left;
            if (right) params.rightMargin += systemInsets.right;

            v.setLayoutParams(params);
            return insets;
        });
    }
}
