package com.mydishes.mydishes.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.R;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

// Класс-адаптер для отображения списка блюд из продуктов
public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.DishViewHolder> {

    private final Context context;
    private final List<Dish> dishes;

    // Конструктор
    public DishesAdapter(Context context, List<Dish> dishes) {
        this.context = context;
        this.dishes = dishes;
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

    // Управление данными текущей view
    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        // Заполнение view!
        Dish currentDish = dishes.get(position);
        Nutrition nutrition = currentDish.getNutrition();

        holder.nameDish.setText(currentDish.getName());

        if (nutrition != null) {
            holder.calories.setText(String.format(Locale.getDefault(), "%.2f", nutrition.getCalories()));
            holder.protein.setText(String.format(Locale.getDefault(), "%.2f", nutrition.getProtein()));
            holder.fat.setText(String.format(Locale.getDefault(), "%.2f", nutrition.getFat()));
            holder.carb.setText(String.format(Locale.getDefault(), "%.2f", nutrition.getCarb()));
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
            imageView = itemView.findViewById(R.id.imageView);
            nameDish = itemView.findViewById(R.id.nameDish);
            calories = itemView.findViewById(R.id.textViewCalories);
            protein = itemView.findViewById(R.id.textViewProteins);
            fat = itemView.findViewById(R.id.textViewFats);
            carb = itemView.findViewById(R.id.textViewCarbs);
        }
    }
}
