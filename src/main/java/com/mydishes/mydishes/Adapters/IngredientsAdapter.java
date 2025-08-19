package com.mydishes.mydishes.Adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.R;

import java.text.DecimalFormat;
import java.util.Objects;

public class IngredientsAdapter extends BaseAdapter<Product, IngredientsAdapter.IngredientsViewHolder> {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public IngredientsAdapter() {
        super(new ProductDiffCallback());
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.list_item_product; // Используем ID макета для каждого элемента
    }

    @Override
    protected IngredientsViewHolder createViewHolder(@NonNull View itemView, int viewType) {
        return new IngredientsViewHolder(itemView);
    }

    @Override
    protected void bind(@NonNull IngredientsViewHolder holder, @NonNull Product product) {
        holder.bind(product);
    }

    public static class IngredientsViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productMass;

        public IngredientsViewHolder(@NonNull View view) {
            super(view);
            productImage = view.findViewById(R.id.productImage);
            productName = view.findViewById(R.id.productName);
            productMass = view.findViewById(R.id.productMass);
        }

        public void bind(@NonNull final Product product) {
            productName.setText(product.getName());

            String massText = decimalFormat.format(product.getMass()) + " г";
            productMass.setText(massText);

            Glide.with(itemView.getContext())
                    .load(product.getImageURL())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(productImage);
        }
    }

    private static class ProductDiffCallback extends DiffUtil.ItemCallback<Product> {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.equals(newItem);
        }
    }
}
