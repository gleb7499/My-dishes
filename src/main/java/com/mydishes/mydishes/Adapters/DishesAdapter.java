package com.mydishes.mydishes.Adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.R;

import java.text.DecimalFormat;
import java.util.Objects;

public class DishesAdapter extends BaseAdapter<Dish, DishesAdapter.DishViewHolder> {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private final OnDishActionClickListener onDishActionClickListener;

    public DishesAdapter(OnDishActionClickListener listener) {
        super(new DishDiffCallback());
        this.onDishActionClickListener = listener;
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.recycler_view_dish;
    }

    @Override
    protected DishViewHolder createViewHolder(@NonNull View itemView, int viewType) {
        return new DishViewHolder(itemView);
    }

    @Override
    protected void bind(@NonNull DishViewHolder holder, @NonNull Dish item) {
        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (onDishActionClickListener != null) {
                onDishActionClickListener.onDishClick(item);
            }
        });
    }

    @FunctionalInterface
    public interface OnDishActionClickListener {
        void onDishClick(Dish dish);
    }

    // ViewHolder remains largely the same, but takes DecimalFormat and uses it
    public static class DishViewHolder extends RecyclerView.ViewHolder {
        ImageView dishImage;
        TextView dishName;
        TextView calories;
        TextView protein;
        TextView fat;
        TextView carb;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);

            dishImage = itemView.findViewById(R.id.dishImage);
            dishName = itemView.findViewById(R.id.dishName);
            calories = itemView.findViewById(R.id.textViewCaloriesValue);
            protein = itemView.findViewById(R.id.textViewProteinValue);
            fat = itemView.findViewById(R.id.textViewFatValue);
            carb = itemView.findViewById(R.id.textViewCarbValue);
        }

        void bind(@NonNull Dish dish) {
            dishName.setText(dish.getName());

            Nutrition nutrition = dish.getNutrition();

            if (nutrition != null) {
                calories.setText(decimalFormat.format(nutrition.getCalories()));
                protein.setText(decimalFormat.format(nutrition.getProtein()));
                fat.setText(decimalFormat.format(nutrition.getFat()));
                carb.setText(decimalFormat.format(nutrition.getCarb()));
            } else {
                calories.setText("N/A");
                protein.setText("N/A");
                fat.setText("N/A");
                carb.setText("N/A");
            }

            Glide.with(itemView.getContext())
                    .load(dish.getPhotoUri())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(dishImage);
        }
    }

    private static class DishDiffCallback extends DiffUtil.ItemCallback<Dish> {
        @Override
        public boolean areItemsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
            return oldItem.equals(newItem);
        }
    }
}
