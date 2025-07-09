package com.mydishes.mydishes.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.mydishes.mydishes.Models.ProductsManager;
import com.mydishes.mydishes.R;

public class RecyclerViewDishesAdapter extends RecyclerView.Adapter<RecyclerViewDishesAdapter.DishesViewHolder> {

    private final Context context;

    public RecyclerViewDishesAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public DishesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_dishes, parent, false);
        return new DishesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishesViewHolder holder, int position) {
        // Получаем и устанавливаем данные
        ProductsManager.Product product = ProductsManager.get(position);

        Glide.with(context)
                .load(product.getImageURL())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.imageView);

        holder.textView.setText(product.getName());

        // Создаем диалог с вводом массы и обрабытваем его логику
        holder.itemView.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_input_mass, null);

            TextInputLayout inputField = dialogView.findViewById(R.id.inputMass);
            EditText editText = inputField.getEditText();

            if (editText == null) return;

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    inputField.setError(null);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.enter_products_mass)
                    .setMessage(product.getName())
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, null) // временно null!
                    .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                    .create();

            dialog.setOnShowListener(d -> {
                Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(v1 -> {
                    String input = editText.getText().toString().trim();

                    if (input.isEmpty() || input.length() > 7) {
                        inputField.setError(context.getString(R.string.error_value));
                    } else {
                        inputField.setError(null);

                        // Обработка введённого значения

                        // Делаем запрос к url через product.getProductURL()
                        // Получаем КБЖУ продукта
                        // Дозаполняем поля product инфой о КБЖУ
                        // Добавляем именно этот product в список продуктов текущего блюда

                        dialog.dismiss();
                    }
                });
            });

            dialog.show();

        });
    }

    @Override
    public int getItemCount() {
        return ProductsManager.size();
    }

    public static final class DishesViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;

        public DishesViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            textView = view.findViewById(R.id.textView);
        }


    }
}