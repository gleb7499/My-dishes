package com.mydishes.mydishes.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

// Класс-адаптер для отображения списка блюд из продуктов
public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.DishViewHolder> {

    private final Context context;
    private final List<Dish> dishes;
    private final DecimalFormat decimalFormat;
    private final OnDishActionClickListener actionClickListener; // Интерфейс для обработки нажатий

    // Конструктор
    public DishesAdapter(Context context, List<Dish> dishes, OnDishActionClickListener listener) {
        this.context = context;
        this.dishes = dishes;
        this.decimalFormat = new DecimalFormat("#.#"); // Формат: #.# означает необязательный знак после запятой
        this.decimalFormat.setRoundingMode(RoundingMode.HALF_UP); // Устанавливаем режим округления
        this.actionClickListener = listener;
    }

    // Управление данными текущей view
    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        // Заполнение view!
        Dish currentDish = dishes.get(position);
        Nutrition nutrition = currentDish.getNutrition();

        holder.nameDish.setText(currentDish.getName());

        if (nutrition != null) {
            holder.calories.setText(decimalFormat.format(nutrition.getCalories()));
            holder.protein.setText(decimalFormat.format(nutrition.getProtein()));
            holder.fat.setText(decimalFormat.format(nutrition.getFat()));
            holder.carb.setText(decimalFormat.format(nutrition.getCarb()));
        } else {
            // Handle cases where nutrition information might be missing
            holder.calories.setText("N/A");
            holder.protein.setText("N/A");
            holder.fat.setText("N/A");
            holder.carb.setText("N/A");
        }

        // Load image using Glide
        Glide.with(context)
                .load(currentDish.getPhotoUri())
                .placeholder(R.drawable.placeholder) // Optional: a placeholder image
                .error(R.drawable.error_image) // Optional: an error image
                .into(holder.imageView);

        // Устанавливаем слушатель долгого нажатия
        holder.itemView.setOnLongClickListener(v -> {
            if (actionClickListener != null) {
                showPopupMenu(holder.itemView, currentDish);
            }
            return true; // Возвращаем true, чтобы показать, что событие обработано
        });

        // Устанавливаем слушатель обычного нажатия
        holder.itemView.setOnClickListener(v -> {
            if (actionClickListener != null) {
                actionClickListener.onDishClick(currentDish);
            }
        });
    }

    // Обновление списка с учетом предыдущего содержимого
    public void submitList(List<Dish> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new GenericDiffCallback<>(
                        dishes,                         // старый список
                        newItems,                       // новый список
                        (oldDish, newDish) -> Objects.equals(oldDish.getName(), newDish.getName()), // сравнение ID
                        Dish::equals                           // сравнение содержимого
                )
        );
        dishes.clear();
        dishes.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    // Создается view для RecyclerView
    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_dish, parent, false);
        return new DishViewHolder(view);
    }

    // Метод для отображения PopupMenu
    private void showPopupMenu(View view, Dish dish) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.dish_context_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_dish) {
                if (actionClickListener != null) {
                    actionClickListener.onEditClick(dish);
                }
                return true;
            } else if (itemId == R.id.action_delete_dish) {
                if (actionClickListener != null) {
                    actionClickListener.onDeleteClick(dish);
                }
                return true;
            }
            return false;
        });
        popup.show();
    }

    // Интерфейс для обработки действий с элементом списка
    public interface OnDishActionClickListener {
        void onDishClick(Dish dish); // Для обычного нажатия

        void onEditClick(Dish dish);

        void onDeleteClick(Dish dish);
    }

    // Размер списка блюд
    @Override
    public int getItemCount() {
        return dishes.size();
    }

    // Класс текущего view
    public static final class DishViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView; // фото блюда

        private final TextView nameDish; // наименование блюда

        private final TextView calories; // ккалории блюда (на 100 гр.)

        private final TextView protein; // белок блюда (на 100 гр.)

        private final TextView fat; // жиры блюда (на 100 гр.)
        private final TextView carb; // углеводы блюда (на 100 гр.)

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewDish);
            nameDish = itemView.findViewById(R.id.nameDish);
            calories = itemView.findViewById(R.id.textViewCaloriesValue);
            protein = itemView.findViewById(R.id.textViewProteinValue);
            fat = itemView.findViewById(R.id.textViewFatValue);
            carb = itemView.findViewById(R.id.textViewCarbValue);
        }
    }
}
