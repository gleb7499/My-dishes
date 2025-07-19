package com.mydishes.mydishes.Adapters;

import android.app.Activity;
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
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.R;

import java.util.List;

public class ProductSelectedAdapter extends RecyclerView.Adapter<ProductSelectedAdapter.ProductSelectedHolder> {

    private final Context context;
    private final List<Product> currentList;

    public ProductSelectedAdapter(Context context, List<Product> currentList) {
        this.context = context;
        this.currentList = currentList;
    }

    @NonNull
    @Override
    public ProductSelectedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_selected_products, parent, false);
        return new ProductSelectedHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductSelectedHolder holder, int position) {
        Product product = currentList.get(position);

        Glide.with(context)
                .load(product.getImageURL())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.imageView);

        holder.textViewName.setText(product.getName());

        holder.textViewMass.setText(String.format("%s г", product.getMass()));
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    public void submitList(List<Product> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProductDiffCallback(currentList, newList));
        currentList.clear();
        currentList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public static final class ProductSelectedHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textViewName;
        private final TextView textViewMass;

        public ProductSelectedHolder(@NonNull View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            textViewName = view.findViewById(R.id.textViewName);
            textViewMass = view.findViewById(R.id.textViewMass);
        }
    }
}
