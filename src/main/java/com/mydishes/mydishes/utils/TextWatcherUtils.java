package com.mydishes.mydishes.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class TextWatcherUtils {
    public static void addSimpleTextWatcher(EditText editText, SimpleTextChanged simpleTextChanged) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Nothing!
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                simpleTextChanged.onTextChanged(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Nothing!
            }
        });
    }

    @FunctionalInterface
    public interface SimpleTextChanged {
        void onTextChanged(String s);
    }
}
