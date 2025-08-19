package com.mydishes.mydishes.Adapters;

import static com.mydishes.mydishes.Utils.ViewUtils.parseFloatSafe;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Models.ProductsSelectedManager;
import com.mydishes.mydishes.Parser.EdostavkaParser;
import com.mydishes.mydishes.Parser.Parser;
import com.mydishes.mydishes.Parser.ProductParseCallback;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.Utils.TextWatcherUtils;

import java.util.Objects;

public class ProductFindListAdapter extends BaseAdapter<Product, ProductFindListAdapter.ProductFindViewHolder> {

    private final Context context;
    private final static Parser parser = new EdostavkaParser(); // Оставляем статическим, если это оправдано дизайном

    public ProductFindListAdapter(@NonNull Activity activity) {
        super(new ProductDiffCallback());
        this.context = activity;
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.list_item_product;
    }

    @Override
    protected ProductFindViewHolder createViewHolder(@NonNull View itemView, int viewType) {
        return new ProductFindViewHolder(itemView);
    }

    @Override
    protected void bind(@NonNull ProductFindViewHolder holder, @NonNull Product product) {
        // Установили фото продукта
        Glide.with(context) // Используем context из конструктора адаптера
                .load(product.getImageURL())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.productImage);

        // Установили наименование продукта
        holder.productName.setText(product.getName());

        // Создаем диалог с вводом массы и обрабатываем его логику
        holder.itemView.setOnClickListener(v -> {
            View dialogViewMass = LayoutInflater.from(context).inflate(R.layout.dialog_input_mass, null);
            TextInputLayout inputFieldMass = dialogViewMass.findViewById(R.id.inputMass);
            EditText editTextMass = inputFieldMass.getEditText();

            if (editTextMass == null) return;

            TextWatcherUtils.addSimpleTextWatcher(editTextMass, s -> inputFieldMass.setError(null));

            AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.enter_products_mass)
                    .setMessage(product.getName())
                    .setView(dialogViewMass)
                    .setPositiveButton(R.string.ok, null) // Релаизация ниже
                    .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                    .create();

            dialog.setOnShowListener(d -> {
                Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(v1 -> {
                    String mass = editTextMass.getText().toString().trim();
                    if (mass.isEmpty() || mass.length() > 7) {
                        inputFieldMass.setError(context.getString(R.string.error_value));
                    } else {
                        inputFieldMass.setError(null);
                        parser.parseProductDetailsAsync((Activity) context, product, new ProductParseCallback() {
                            @Override
                            public void onSuccess(Product parsedProduct) {
                                // Важно: используем объект parsedProduct, который содержит детали КБЖУ
                                // и устанавливаем ему массу, которую ввел пользователь
                                parsedProduct.setMass(parseFloatSafe(mass));
                                ProductsSelectedManager.add(parsedProduct);
                                Snackbar.make(holder.itemView, "Записан " + parsedProduct.getName(), Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Snackbar.make(holder.itemView, "Ошибка парсинга: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });
                        dialog.dismiss();
                    }
                });
            });
            dialog.show();
        });
    }

    public static final class ProductFindViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;

        public ProductFindViewHolder(View view) {
            super(view);
            productImage = view.findViewById(R.id.productImage);
            productName = view.findViewById(R.id.productName);
        }
    }

    private static class ProductDiffCallback extends DiffUtil.ItemCallback<Product> {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.equals(newItem);
        }
    }
}
