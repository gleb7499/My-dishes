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

import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.R;

import java.util.List;
import java.util.Objects;

// Класс-адаптер для отображения списка блюд из продуктов
public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {

    private final Context context;
    private final List<Dish> dishes;

    // Конструктор
    public DishAdapter(Context context, List<Dish> dishes) {
        this.context = context;
        this.dishes = dishes;
    }

    // Обновление списка с учетом предыдущего содержимого
    public void submitList(List<Dish> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new com.mydishes.mydishes.Utils.GenericDiffCallback<Dish>(
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
        // Заполнение view! (срать тут)
        
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
