package com.mydishes.mydishes.Adapters;

import static com.mydishes.mydishes.Utils.ViewUtils.parseFloatSafe;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.Utils.DialogUtils;

import java.text.DecimalFormat;
import java.util.Objects;

public class IngredientsAdapter extends BaseAdapter<Product, IngredientsAdapter.IngredientsViewHolder> {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    public static final String REQUEST_KEY = "ingredientsAdapterRequestKey";
    public static final String BUNDLE_KEY_PRODUCT = "productKey"; // New key for Product
    public static final String BUNDLE_KEY_NEW_MASS = "newMassKey"; // New key for new mass

    private FragmentManager parentFragmentManager; // Store FragmentManager

    public IngredientsAdapter(FragmentManager fragmentManager) { // Constructor to receive FragmentManager
        super(new ProductDiffCallback());
        this.parentFragmentManager = fragmentManager;
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
        holder.productName.setText(product.getName());

        String massText = decimalFormat.format(product.getMass()) + " г";
        holder.productMass.setText(massText);

        Glide.with(holder.itemView.getContext())
                .load(product.getImageURL())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.productImage);

        holder.itemView.setOnClickListener(v -> DialogUtils.showInputMassDialog(holder.itemView.getContext(), product.getName(), massStr -> {
            float mass = parseFloatSafe(massStr);
            // Отправляем результат родительскому компоненту
            Bundle bundleResult = new Bundle();
            bundleResult.putParcelable(BUNDLE_KEY_PRODUCT, product.clone());
            bundleResult.putFloat(BUNDLE_KEY_NEW_MASS, mass);
            parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleResult);
        }));
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
