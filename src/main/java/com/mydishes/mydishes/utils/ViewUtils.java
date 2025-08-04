package com.mydishes.mydishes.utils;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mydishes.mydishes.R;

// Утилитарный класс, в котором общие методы, для всего приложения
public class ViewUtils {

    // Вставить отступы для системных панелей навигации. boolean указывает, куда нужно их вставить
    public static void applyInsets(View targetView, boolean top, boolean bottom, boolean left, boolean right) {
        ViewCompat.setOnApplyWindowInsetsListener(targetView, (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            // Сохраняем оригинальные маргины, если ещё не сохранили
            if (v.getTag(R.id.tag_margin_left) == null
                    || v.getTag(R.id.tag_margin_right) == null
                    || v.getTag(R.id.tag_margin_top) == null
                    || v.getTag(R.id.tag_margin_bottom) == null) {
                v.setTag(R.id.tag_margin_left, params.leftMargin);
                v.setTag(R.id.tag_margin_right, params.rightMargin);
                v.setTag(R.id.tag_margin_top, params.topMargin);
                v.setTag(R.id.tag_margin_bottom, params.bottomMargin);
            }

            // Теперь применяем Insets, если нужно
            if (left) params.leftMargin = systemInsets.left + (int) v.getTag(R.id.tag_margin_left);
            if (right)
                params.rightMargin = systemInsets.right + (int) v.getTag(R.id.tag_margin_right);
            if (top) params.topMargin = systemInsets.top + (int) v.getTag(R.id.tag_margin_top);
            if (bottom)
                params.bottomMargin = systemInsets.bottom + (int) v.getTag(R.id.tag_margin_bottom);

            v.setLayoutParams(params);
            return insets;
        });
    }
}
