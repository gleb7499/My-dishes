package com.mydishes.mydishes.adapters;

import static com.mydishes.mydishes.utils.ViewUtils.parseFloatSafe;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.models.Product;
import com.mydishes.mydishes.models.ProductsSelectedManager;
import com.mydishes.mydishes.parser.EdostavkaParser;
import com.mydishes.mydishes.parser.Parser;
import com.mydishes.mydishes.parser.ParsingStateListener;
import com.mydishes.mydishes.parser.ProductParseCallback;
import com.mydishes.mydishes.utils.DialogUtils;

import java.util.Objects;

/**
 * Адаптер для отображения списка найденных продуктов в RecyclerView.
 * Позволяет пользователю выбрать продукт, указать его массу и добавить в список выбранных.
 */
public class ProductFindAdapter extends BaseAdapter<Product, ProductFindAdapter.ProductFindViewHolder> {

    private final Context context;
    private final ParsingStateListener parsingStateListener;
    private final Parser parser = new EdostavkaParser(); // Используется для получения детальной информации о продукте

    /**
     * Конструктор для ProductFindAdapter.
     *
     * @param activity Контекст Activity, необходимый для отображения диалогов и Glide.
     * @param listener Слушатель состояния парсинга.
     */
    public ProductFindAdapter(@NonNull Activity activity, ParsingStateListener listener) {
        super(new ProductDiffCallback());
        this.context = activity;
        this.parsingStateListener = listener;
    }

    /**
     * Возвращает идентификатор макета для элемента списка.
     *
     * @param viewType Тип представления (не используется в данном адаптере).
     * @return Идентификатор макета R.layout.list_item_product.
     */
    @Override
    protected int getLayoutId(int viewType) {
        // Используем стандартный макет для элемента списка продуктов
        return R.layout.list_item_product;
    }

    /**
     * Создает новый ViewHolder для элемента списка.
     *
     * @param itemView Представление элемента списка.
     * @param viewType Тип представления (не используется в данном адаптере).
     * @return Новый экземпляр ProductFindViewHolder.
     */
    @Override
    protected ProductFindViewHolder createViewHolder(@NonNull View itemView, int viewType) {
        // Создаем и возвращаем ViewHolder для элемента списка
        return new ProductFindViewHolder(itemView);
    }

    /**
     * Привязывает данные продукта к ViewHolder и устанавливает обработчик клика.
     *
     * @param holder  ViewHolder для привязки данных.
     * @param product Объект Product для отображения.
     */
    @Override
    protected void bind(@NonNull ProductFindViewHolder holder, @NonNull Product product) {
        // Загрузка изображения продукта с помощью Glide
        Glide.with(context) // Используем context из конструктора адаптера
                .load(product.getImageURL())
                .placeholder(R.drawable.placeholder) // Изображение-заглушка на время загрузки
                .error(R.drawable.error_image) // Изображение при ошибке загрузки
                .into(holder.productImage);

        // Установка наименования продукта
        holder.productName.setText(product.getName());

        // Скрытие текстового поля массы, так как она будет введена пользователем
        holder.productMass.setVisibility(View.GONE);

        // Установка слушателя кликов для открытия диалога ввода массы
        holder.itemView.setOnClickListener(v -> {
            DialogUtils.showInputMassDialog(context, product.getName(), massStr -> {
                // Асинхронный парсинг деталей продукта (КБЖУ) после ввода массы
                parser.parseProductDetailsAsync(product, new ProductParseCallback<>() {
                    @Override
                    public void onParsingStarted() {
                        // Уведомление слушателя о начале парсинга
                        if (parsingStateListener != null) {
                            parsingStateListener.onParsingStarted();
                        }
                    }

                    @Override
                    public void onSuccess(Product parsedProduct) {
                        // Установка введенной массы для распарсенного продукта
                        parsedProduct.setMass(parseFloatSafe(massStr));
                        // Добавление продукта в менеджер выбранных продуктов
                        ProductsSelectedManager.add(parsedProduct);
                        // Отображение Snackbar с подтверждением добавления
                        Snackbar.make(holder.itemView, "Записан " + parsedProduct.getName(), BaseTransientBottomBar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Snackbar snackbar = Snackbar.make(holder.itemView, "Ошибка парсинга: " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                        snackbar.show();
                    }

                    @Override
                    public void onParsingFinished() {
                        // Уведомление слушателя о завершении парсинга
                        if (parsingStateListener != null) {
                            parsingStateListener.onParsingFinished();
                        }
                    }
                });
            });
        });
    }

    /**
     * ViewHolder для отображения информации о найденном продукте.
     */
    public static final class ProductFindViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productMass; // Хотя и скрыто, оно присутствует в макете

        /**
         * Конструктор для ProductFindViewHolder.
         *
         * @param view Представление элемента списка.
         */
        public ProductFindViewHolder(View view) {
            super(view);
            // Инициализация View-компонентов
            productImage = view.findViewById(R.id.productImage);
            productName = view.findViewById(R.id.productName);
            productMass = view.findViewById(R.id.productMass);
        }
    }

    /**
     * Callback для DiffUtil для сравнения элементов списка продуктов.
     */
    private static class ProductDiffCallback extends DiffUtil.ItemCallback<Product> {
        /**
         * Проверяет, являются ли два элемента одним и тем же элементом.
         *
         * @param oldItem Старый элемент.
         * @param newItem Новый элемент.
         * @return True, если элементы являются одним и тем же (сравниваются по имени), иначе false.
         */
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            // Сравниваем продукты по имени, так как ID может быть не уникальным до парсинга деталей
            return Objects.equals(oldItem.getName(), newItem.getName());
        }

        /**
         * Проверяет, имеют ли два элемента одинаковое содержимое.
         *
         * @param oldItem Старый элемент.
         * @param newItem Новый элемент.
         * @return True, если содержимое элементов одинаково, иначе false.
         */
        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            // Сравниваем объекты Product на полное равенство
            return oldItem.equals(newItem);
        }
    }
}
