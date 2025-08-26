package com.mydishes.mydishes.Utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;

// Класс для упрощения вида в коде TextWatcher. Использовать, когда нужен только один метод из трех
public class TextWatcherUtils {
    /**
     * Добавляет простой TextWatcher к EditText, который реагирует только на изменение текста.
     *
     * @param editText          EditText, к которому добавляется TextWatcher.
     * @param simpleTextChanged Слушатель для обработки изменения текста.
     */
    public static void addSimpleTextWatcher(@NonNull EditText editText, SimpleTextChanged simpleTextChanged) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Nothing!
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Вызов слушателя при изменении текста
                simpleTextChanged.onTextChanged(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Nothing!
            }
        });
    }

    /**
     * Функциональный интерфейс для обработки изменения текста в EditText.
     */
    @FunctionalInterface
    public interface SimpleTextChanged {
        /**
         * Вызывается при изменении текста.
         *
         * @param s Новый текст в EditText.
         */
        void onTextChanged(String s);
    }
}
