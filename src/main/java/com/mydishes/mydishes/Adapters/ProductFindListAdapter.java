package com.mydishes.mydishes.Adapters;

import static com.mydishes.mydishes.Utils.ViewUtils.parseFloatSafe;

import android.app.Activity;
import android.content.Context;
import android.text.InputFilter;
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
import com.mydishes.mydishes.Parser.ParsingStateListener;
import com.mydishes.mydishes.Parser.ProductParseCallback;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.Utils.TextWatcherUtils;

import java.util.Objects;

public class ProductFindListAdapter extends BaseAdapter<Product, ProductFindListAdapter.ProductFindViewHolder> {

    private final Context context;
    private final ParsingStateListener parsingStateListener;
    private final Parser parser = new EdostavkaParser();

    public ProductFindListAdapter(@NonNull Activity activity, ParsingStateListener listener) {
        super(new ProductDiffCallback());
        this.context = activity;
        this.parsingStateListener = listener;
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

        // здесь масса продукта пока что не задана
        holder.productMass.setVisibility(View.GONE);

        // Создаем диалог с вводом массы и обрабатываем его логику
        holder.itemView.setOnClickListener(v -> {
            // раздуваем макет
            View dialogViewMass = LayoutInflater.from(context).inflate(R.layout.dialog_input_mass, null);
            TextInputLayout inputFieldMass = dialogViewMass.findViewById(R.id.inputMass);
            EditText editTextMass = inputFieldMass.getEditText();

            if (editTextMass == null) return;

            // Устанавливаем максимальную длину для EditText (например, 4 символов)
            int maxLength = 4;
            editTextMass.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

            // Отключаем отображение старых ошибок
            TextWatcherUtils.addSimpleTextWatcher(editTextMass, s -> {
                if (inputFieldMass.getError() != null) inputFieldMass.setError(null);
            });

            // создаем диалог для ввода массы выбранного продукта
            AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.enter_products_mass)
                    .setMessage(product.getName())
                    .setView(dialogViewMass)
                    .setPositiveButton(R.string.ok, null) // Релаизация ниже
                    .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                    .create();

            // Обработка введенного значения
            dialog.setOnShowListener(d -> {
                Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(v1 -> {
                    String massStr = editTextMass.getText().toString().trim(); // Renamed to avoid confusion

                    if (massStr.isEmpty() || massStr.length() > 7) {
                        inputFieldMass.setError(context.getString(R.string.error_value));
                        return;
                    }
                    dialog.dismiss(); // Dismiss dialog before starting parsing

                    // Получаем КБЖУ продукта с сайта
                    // Передаем Activity как context, так как Parser.java этого требует
                    parser.parseProductDetailsAsync(product, new ProductParseCallback<>() {
                        @Override
                        public void onParsingStarted() {
                            if (parsingStateListener != null) {
                                parsingStateListener.onParsingStarted();
                            }
                        }

                        @Override
                        public void onSuccess(Product parsedProduct) {
                            parsedProduct.setMass(parseFloatSafe(massStr)); // устанавливаем массу
                            ProductsSelectedManager.add(parsedProduct);
                            Snackbar.make(holder.itemView, "Записан " + parsedProduct.getName(), Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Snackbar.make(holder.itemView, "Ошибка парсинга: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onParsingFinished() {
                            if (parsingStateListener != null) {
                                parsingStateListener.onParsingFinished();
                            }
                        }
                    });
                });
            });
            dialog.show();
        });
    }

    public static final class ProductFindViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productMass;

        public ProductFindViewHolder(View view) {
            super(view);
            productImage = view.findViewById(R.id.productImage);
            productName = view.findViewById(R.id.productName);
            productMass = view.findViewById(R.id.productMass);
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
