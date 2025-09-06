package com.mydishes.mydishes.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.models.Dish;
import com.mydishes.mydishes.models.Nutrition;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Адаптер для отображения списка блюд в RecyclerView.
 */
public class DishesAdapter extends BaseAdapter<Dish, DishesAdapter.DishViewHolder> {

    // Формат для отображения числовых значений с двумя знаками после запятой
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    // Слушатель для обработки кликов по элементам списка
    private final OnDishActionClickListener onDishActionClickListener;

    /**
     * Конструктор для DishesAdapter.
     *
     * @param listener Слушатель для обработки кликов по элементам списка.
     */
    public DishesAdapter(OnDishActionClickListener listener) {
        super(new DishDiffCallback());
        this.onDishActionClickListener = listener;
    }

    /**
     * Возвращает идентификатор макета для элемента списка.
     *
     * @param viewType Тип представления (не используется в данном адаптере).
     * @return Идентификатор макета R.layout.list_item_dish.
     */
    @Override
    protected int getLayoutId(int viewType) {
        // Возвращаем макет для каждого элемента списка
        return R.layout.list_item_dish;
    }

    /**
     * Создает новый ViewHolder для элемента списка.
     *
     * @param itemView Представление элемента списка.
     * @param viewType Тип представления (не используется в данном адаптере).
     * @return Новый экземпляр DishViewHolder.
     */
    @Override
    protected DishViewHolder createViewHolder(@NonNull View itemView, int viewType) {
        // Создаем и возвращаем ViewHolder для элемента списка
        return new DishViewHolder(itemView);
    }

    /**
     * Привязывает данные блюда к ViewHolder.
     *
     * @param holder ViewHolder для привязки данных.
     * @param item   Объект Dish для отображения.
     */
    @Override
    protected void bind(@NonNull DishViewHolder holder, @NonNull Dish item) {
        // Привязываем данные блюда к ViewHolder
        holder.bind(item);

        // Устанавливаем слушатель кликов на элемент списка
        holder.itemView.setOnClickListener(v -> {
            if (onDishActionClickListener != null) {
                // Вызываем метод слушателя при клике на блюдо
                onDishActionClickListener.onDishClick(item);
            }
        });
    }

    /**
     * Интерфейс для обработки кликов по элементам списка блюд.
     */
    @FunctionalInterface
    public interface OnDishActionClickListener {
        /**
         * Вызывается при клике на блюдо в списке.
         *
         * @param dish Объект Dish, по которому кликнули.
         */
        void onDishClick(Dish dish);
    }

    /**
     * ViewHolder для отображения информации о блюде.
     */
    public static class DishViewHolder extends RecyclerView.ViewHolder {
        ImageView dishImage;
        TextView dishName;
        TextView calories;
        TextView protein;
        TextView fat;
        TextView carb;

        /**
         * Конструктор для DishViewHolder.
         *
         * @param itemView Представление элемента списка.
         */
        public DishViewHolder(@NonNull View itemView) {
            super(itemView);

            // Инициализация View-компонентов
            dishImage = itemView.findViewById(R.id.dishImage);
            dishName = itemView.findViewById(R.id.dishName);
            calories = itemView.findViewById(R.id.textViewCaloriesValue);
            protein = itemView.findViewById(R.id.textViewProteinValue);
            fat = itemView.findViewById(R.id.textViewFatValue);
            carb = itemView.findViewById(R.id.textViewCarbValue);
        }

        /**
         * Привязывает данные блюда к View-компонентам ViewHolder.
         *
         * @param dish Объект Dish для отображения.
         */
        void bind(@NonNull Dish dish) {
            // Установка названия блюда
            dishName.setText(dish.getName());

            // Получение информации о питательной ценности
            Nutrition nutrition = dish.getNutrition();

            // Проверка наличия информации о питательной ценности
            if (nutrition != null) {
                // Установка значений питательной ценности
                calories.setText(decimalFormat.format(nutrition.getCalories()));
                protein.setText(decimalFormat.format(nutrition.getProtein()));
                fat.setText(decimalFormat.format(nutrition.getFat()));
                carb.setText(decimalFormat.format(nutrition.getCarb()));
            } else {
                // Установка "N/A" если информация отсутствует
                calories.setText("N/A");
                protein.setText("N/A");
                fat.setText("N/A");
                carb.setText("N/A");
            }

            // Загрузка изображения блюда с помощью Glide
            Glide.with(itemView.getContext())
                    .load(dish.getPhotoUri())
                    .placeholder(R.drawable.placeholder) // Изображение-заглушка на время загрузки
                    .error(R.drawable.error_image) // Изображение при ошибке загрузки
                    .into(dishImage);
        }
    }

    /**
     * Callback для DiffUtil для сравнения элементов списка блюд.
     */
    private static class DishDiffCallback extends DiffUtil.ItemCallback<Dish> {
        /**
         * Проверяет, являются ли два элемента одним и тем же элементом.
         *
         * @param oldItem Старый элемент.
         * @param newItem Новый элемент.
         * @return True, если элементы являются одним и тем же, иначе false.
         */
        @Override
        public boolean areItemsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
            // Сравниваем ID блюд
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        /**
         * Проверяет, имеют ли два элемента одинаковое содержимое.
         *
         * @param oldItem Старый элемент.
         * @param newItem Новый элемент.
         * @return True, если содержимое элементов одинаково, иначе false.
         */
        @Override
        public boolean areContentsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
            // Сравниваем объекты Dish на полное равенство
            return oldItem.equals(newItem);
        }
    }
}
