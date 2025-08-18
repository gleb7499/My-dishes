package com.mydishes.mydishes.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.R;

import java.text.DecimalFormat;
import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder> {

    private final Context context;
    private final List<Product> ingredients;
    private final DecimalFormat decimalFormat; // Для форматирования массы

    public IngredientsAdapter(Context context, List<Product> ingredients) {
        this.context = context;
        this.ingredients = ingredients;
        // Инициализируем DecimalFormat для отображения массы без лишних нулей
        this.decimalFormat = new DecimalFormat("#.##"); // Отобразит до 2 знаков после запятой, если они есть
        this.decimalFormat.setRoundingMode(java.math.RoundingMode.HALF_UP);
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_product, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Product ingredient = ingredients.get(position);
        holder.ingredientName.setText(ingredient.getName());
        // Форматируем массу, чтобы убрать лишние нули, например, 100.0 -> 100, 100.50 -> 100.5
        String massString = decimalFormat.format(ingredient.getMass()) + " г";
        holder.ingredientQuantity.setText(massString);

        Glide.with(context)
                .load(ingredient.getImageURL()) // Предполагается, что Product.getImageURL() возвращает URL изображения
                .placeholder(R.drawable.placeholder) // Опционально: изображение-заполнитель
                .error(R.drawable.error_image) // Опционально: изображение при ошибке загрузки
                .into(holder.ingredientImage);
    }

    @Override
    public int getItemCount() {
        return ingredients == null ? 0 : ingredients.size();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        ImageView ingredientImage;
        TextView ingredientName;
        TextView ingredientQuantity;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientImage = itemView.findViewById(R.id.imageView); // Updated ID
            ingredientName = itemView.findViewById(R.id.nameDish); // Updated ID
            ingredientQuantity = itemView.findViewById(R.id.textViewMass); // Updated ID
        }
    }
}
