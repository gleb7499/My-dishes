package com.mydishes.mydishes.Utils;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.mydishes.mydishes.R;

public class DialogUtils {

    public static void showEditDishNameDialog(Context context, String currentDishName, OnDishNameEnteredListener listener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        View dialogView = inflater.inflate(R.layout.dialog_input_name, null);
        builder.setView(dialogView);

        TextInputLayout inputFieldName = dialogView.findViewById(R.id.inputName);
        EditText editTextDishName = inputFieldName.getEditText();
        if (editTextDishName == null) return;

        // Устанавливаем текст в поле ввода, если оно не null
        if (currentDishName != null) {
            editTextDishName.setText(currentDishName);
            editTextDishName.setSelection(currentDishName.length()); // Установить курсор в конец
        }

        // Устанавливаем максимальную длину для EditText (например, 50 символов)
        int maxLength = 50;
        editTextDishName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        // Новая строка для заголовка: R.string.edit_dish_name_title
        builder.setTitle(R.string.enter_products_name);
        builder.setPositiveButton(R.string.ok, null); // Обработчик будет установлен позже для валидации
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();

        // Устанавливаем режим изменения размера для окна диалога (НЕ РАБОТАЕТ!)
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        // Переопределяем OnClickListener для PositiveButton для валидации
        // и добавляем логику фокуса и показа клавиатуры
        alertDialog.setOnShowListener(dialogInterface -> {
            // Устанавливаем фокус на поле ввода
            editTextDishName.requestFocus();

            // Показываем клавиатуру с небольшой задержкой
            editTextDishName.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editTextDishName, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100); // 100 миллисекунд задержки

            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String newName = editTextDishName.getText().toString().trim();
                if (TextUtils.isEmpty(newName)) {
                    editTextDishName.setError(context.getString(R.string.error_value));
                } else {
                    if (listener != null) {
                        listener.onDishNameEntered(newName);
                    }
                    alertDialog.dismiss();
                }
            });
        });

        alertDialog.show();
    }

    @FunctionalInterface
    public interface OnDishNameEnteredListener {
        void onDishNameEntered(String newName);
    }
}
