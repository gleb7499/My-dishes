package com.mydishes.mydishes.adapters;

import static com.mydishes.mydishes.utils.ViewUtils.parseFloatSafe;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.models.Product;
import com.mydishes.mydishes.utils.DialogUtils;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Адаптер для отображения списка ингредиентов в RecyclerView.
 */
public class IngredientsAdapter extends BaseAdapter<Product, IngredientsAdapter.IngredientsViewHolder> {
    // Ключ для FragmentResultListener для получения результата из диалога
    public static final String REQUEST_KEY = "ingredientsAdapterRequestKey";
    // Ключ для передачи объекта Product в Bundle
    public static final String BUNDLE_KEY_PRODUCT = "productKey";
    // Ключ для передачи новой массы в Bundle
    public static final String BUNDLE_KEY_NEW_MASS = "newMassKey";
    // Формат для отображения числовых значений с двумя знаками после запятой
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    // FragmentManager для отображения диалоговых окон
    private FragmentManager parentFragmentManager;

    /**
     * Конструктор для IngredientsAdapter.
     *
     * @param fragmentManager FragmentManager для управления диалоговыми окнами.
     */
    public IngredientsAdapter(FragmentManager fragmentManager) {
        super(new ProductDiffCallback());
        this.parentFragmentManager = fragmentManager;
    }

    /**
     * Возвращает идентификатор макета для элемента списка.
     *
     * @param viewType Тип представления (не используется в данном адаптере).
     * @return Идентификатор макета R.layout.list_item_product.
     */
    @Override
    protected int getLayoutId(int viewType) {
        // Используем макет для каждого элемента списка
        return R.layout.list_item_product;
    }

    /**
     * Создает новый ViewHolder для элемента списка.
     *
     * @param itemView Представление элемента списка.
     * @param viewType Тип представления (не используется в данном адаптере).
     * @return Новый экземпляр IngredientsViewHolder.
     */
    @Override
    protected IngredientsViewHolder createViewHolder(@NonNull View itemView, int viewType) {
        // Создаем и возвращаем ViewHolder для элемента списка
        return new IngredientsViewHolder(itemView);
    }

    /**
     * Привязывает данные ингредиента к ViewHolder.
     *
     * @param holder  ViewHolder для привязки данных.
     * @param product Объект Product для отображения.
     */
    @Override
    protected void bind(@NonNull IngredientsViewHolder holder, @NonNull Product product) {
        // Установка названия продукта
        holder.productName.setText(product.getName());

        // Форматирование и установка массы продукта
        String massText = decimalFormat.format(product.getMass()) + " г";
        holder.productMass.setText(massText);

        // Загрузка изображения продукта с помощью Glide
        Glide.with(holder.itemView.getContext())
                .load(product.getImageURL())
                .placeholder(R.drawable.placeholder) // Изображение-заглушка на время загрузки
                .error(R.drawable.error_image) // Изображение при ошибке загрузки
                .into(holder.productImage);

        // Установка слушателя кликов для открытия диалога изменения массы
        holder.itemView.setOnClickListener(v -> DialogUtils.showInputMassDialog(holder.itemView.getContext(), product.getName(), massStr -> {
            // Преобразование строки массы в число
            float mass = parseFloatSafe(massStr);
            // Создание Bundle для передачи результата
            Bundle bundleResult = new Bundle();
            bundleResult.putParcelable(BUNDLE_KEY_PRODUCT, Product.createProduct(product)); // Клонируем продукт, чтобы избежать изменения оригинала
            bundleResult.putFloat(BUNDLE_KEY_NEW_MASS, mass);
            // Отправка результата родительскому компоненту через FragmentManager
            parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleResult);
        }));
    }

    /**
     * ViewHolder для отображения информации об ингредиенте.
     */
    public static class IngredientsViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productMass;

        /**
         * Конструктор для IngredientsViewHolder.
         *
         * @param view Представление элемента списка.
         */
        public IngredientsViewHolder(@NonNull View view) {
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
            // Сравниваем продукты по имени
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
