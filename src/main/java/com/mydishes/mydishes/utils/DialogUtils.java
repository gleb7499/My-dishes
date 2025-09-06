package com.mydishes.mydishes.utils;

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

    /**
     * Отображает диалоговое окно для редактирования имени блюда.
     *
     * @param context         Контекст приложения.
     * @param currentDishName Текущее имя блюда (может быть null, если имя не задано).
     * @param listener        Слушатель для получения нового имени блюда.
     */
    public static void showEditDishNameDialog(Context context, String currentDishName, OnDishNameEnteredListener listener) {
        // Создание и настройка MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        // Загрузка макета диалогового окна
        View dialogView = inflater.inflate(R.layout.dialog_input_name, null);
        builder.setView(dialogView);

        // Получение ссылок на элементы макета
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

        // Создание AlertDialog
        AlertDialog alertDialog = builder.create();

        // Устанавливаем режим изменения размера для окна диалога (НЕ РАБОТАЕТ!)
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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
                // Валидация введенного имени
                if (TextUtils.isEmpty(newName)) {
                    editTextDishName.setError(context.getString(R.string.error_value));
                } else {
                    // Вызов слушателя с новым именем
                    if (listener != null) {
                        listener.onDishNameEntered(newName);
                    }
                    alertDialog.dismiss();
                }
            });
        });

        // Отображение диалогового окна
        alertDialog.show();
    }

    /**
     * Отображает диалоговое окно для ввода массы продукта.
     *
     * @param context     Контекст приложения.
     * @param productName Название продукта, для которого вводится масса.
     * @param listener    Слушатель для получения введенной массы.
     */
    public static void showInputMassDialog(Context context, String productName, OnMassEnteredListener listener) {
        // Загрузка макета диалогового окна
        View dialogViewMass = LayoutInflater.from(context).inflate(R.layout.dialog_input_mass, null);
        TextInputLayout inputFieldMass = dialogViewMass.findViewById(R.id.inputMass);
        EditText editTextMass = inputFieldMass.getEditText();

        if (editTextMass == null) return;

        // Установка максимальной длины для поля ввода массы
        int maxLength = 4;
        editTextMass.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        // Добавление TextWatcher для сброса ошибки при изменении текста
        TextWatcherUtils.addSimpleTextWatcher(editTextMass, s -> {
            if (inputFieldMass.getError() != null) inputFieldMass.setError(null);
        });

        // Создание и настройка AlertDialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.enter_products_mass)
                .setMessage(productName)
                .setView(dialogViewMass)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .create();

        // Установка слушателя на отображение диалога для автоматического фокуса и показа клавиатуры
        dialog.setOnShowListener(d -> {
            // Установка фокуса на поле ввода массы
            editTextMass.requestFocus();
            // Показ клавиатуры с задержкой
            editTextMass.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editTextMass, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);

            // Получение кнопки "ОК" и установка обработчика нажатия для валидации
            Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(v1 -> {
                String massStr = editTextMass.getText().toString().trim();
                // Валидация введенной массы
                if (massStr.isEmpty() || massStr.length() > maxLength) {
                    inputFieldMass.setError(context.getString(R.string.error_value));
                    return;
                }
                // Вызов слушателя с введенной массой
                if (listener != null) {
                    listener.onMassEntered(massStr);
                }
                dialog.dismiss();
            });
        });
        // Отображение диалогового окна
        dialog.show();
    }

    /**
     * Функциональный интерфейс для обработки ввода имени блюда.
     */
    @FunctionalInterface
    public interface OnDishNameEnteredListener {
        /**
         * Вызывается, когда пользователь ввел новое имя блюда.
         *
         * @param newName Новое имя блюда.
         */
        void onDishNameEntered(String newName);
    }

    /**
     * Функциональный интерфейс для обработки ввода массы.
     */
    @FunctionalInterface
    public interface OnMassEnteredListener {
        /**
         * Вызывается, когда пользователь ввел массу.
         * @param mass Введенная масса в виде строки.
         */
        void onMassEntered(String mass);
    }
}
