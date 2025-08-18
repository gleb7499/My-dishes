package com.mydishes.mydishes.Adapters;

import static com.mydishes.mydishes.Utils.NutritionCalculator.parseFloatSafe;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;
import java.util.Objects;

// Класс-адаптер для отображения найденных продуктов при парсинге продуктового сайта
public class ProductFindListAdapter extends RecyclerView.Adapter<ProductFindListAdapter.ProductFindViewHolder> {

    private final Context context;
    private final static Parser parser = new EdostavkaParser();
    private final List<Product> products;

    public ProductFindListAdapter(@NonNull Activity activity, List<Product> products) {
        this.context = activity;
        this.products = products;
    }

    // Обновление списка с учетом предыдущего содержимого
    public void submitList(List<Product> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new GenericDiffCallback<>(
                        products,                         // старый список
                        newItems,                             // новый список
                        (oldProduct, newProduct) -> Objects.equals(oldProduct.getName(), newProduct.getName()), // сравнение ID
                        Product::equals                           // сравнение содержимого
                )
        );
        products.clear();
        products.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    // Создается view для RecyclerView
    @NonNull
    @Override
    public ProductFindViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_found_product, parent, false);
        return new ProductFindViewHolder(view);
    }

    // Управление данными текущей view
    @Override
    public void onBindViewHolder(@NonNull ProductFindViewHolder holder, int position) {
        // Получаем и устанавливаем данные
        Product product = products.get(position);

        // Установили фото продукта
        Glide.with(context)
                .load(product.getImageURL())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.imageView);

        // Установили наименование продукта
        holder.textView.setText(product.getName());

        // Создаем диалог с вводом массы и обрабытваем его логику
        holder.itemView.setOnClickListener(v -> {
            // Надуваем XML макет в view вид
            View dialogViewMass = LayoutInflater.from(context).inflate(R.layout.dialog_input_mass, null);

            TextInputLayout inputFieldMass = dialogViewMass.findViewById(R.id.inputMass);
            EditText editTextMass = inputFieldMass.getEditText();

            if (editTextMass == null) return;

            // Убираем отображение предыдущих ошибок
            TextWatcherUtils.addSimpleTextWatcher(editTextMass, s -> editTextMass.setError(null));

            // Диалог для ввода массы выбранного продукта
            AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.enter_products_mass)
                    .setMessage(product.getName())
                    .setView(dialogViewMass)
                    .setPositiveButton(R.string.ok, null) // временно null!
                    .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                    .create();

            // Выполнение проверок введенного значения и др. действий при нажатии кнопки ОК
            dialog.setOnShowListener(d -> {
                Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(v1 -> {
                    String mass = editTextMass.getText().toString().trim();

                    // Проверка введенного значения
                    if (mass.isEmpty() || mass.length() > 7) {
                        inputFieldMass.setError(context.getString(R.string.error_value));
                    } else {
                        inputFieldMass.setError(null);

                        // Обработка введённого значения
                        // Получаем КБЖУ введённого продукта
                        parser.parseProductDetailsAsync((Activity) context, product, new ProductParseCallback() {
                            @Override
                            public void onSuccess(Product product) {
                                product.setMass(parseFloatSafe(mass));
                                ProductsSelectedManager.add(product);
                            }

                            @Override
                            public void onError(Exception e) {
                                Snackbar.make(v1, "Ошибка: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });

                        // Уничтожаем диалог
                        dialog.dismiss();

                        Snackbar.make(v, "Записан " + product.getName(), Snackbar.LENGTH_LONG).show();
                    }
                });
            });

            // Показать диалог
            dialog.show();

        });
    }

    // Размер списка продуктов
    @Override
    public int getItemCount() {
        return products.size();
    }

    // Класс текущего view
    public static final class ProductFindViewHolder extends RecyclerView.ViewHolder {
        // Фото товара
        private final ImageView imageView;
        // Наименование товара
        private final TextView textView;

        public ProductFindViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            textView = view.findViewById(R.id.nameDish);
        }
    }
}