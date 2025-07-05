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
import com.mydishes.mydishes.Models.DishesManager;
import com.mydishes.mydishes.R;

public class RecyclerViewDishesAdapter extends RecyclerView.Adapter<RecyclerViewDishesAdapter.DishesViewHolder> {

    private final Context context;

    public RecyclerViewDishesAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public DishesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_dishes, parent, false);
        return new DishesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishesViewHolder holder, int position) {
        DishesManager.Dish dish = DishesManager.get(position);

        Glide.with(context)
                .load(dish.getImage())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.imageView);

        holder.textView.setText(dish.getName());
    }

    @Override
    public int getItemCount() {
        return DishesManager.size();
    }

    public static final class DishesViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;

        public DishesViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            textView = view.findViewById(R.id.textView);
        }


    }
}