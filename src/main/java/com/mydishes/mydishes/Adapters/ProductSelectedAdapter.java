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
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.R;

import java.util.List;
import java.util.Objects;

// Класс-адаптер для демонстрации выбранных продуктых для нового блюда
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
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_selected_product, parent, false);
        return new ProductSelectedHolder(view);
    }

    // Обрабатываем текущий view
    @Override
    public void onBindViewHolder(@NonNull ProductSelectedHolder holder, int position) {
        // Получаем продукт
        Product product = currentList.get(position);

        // Установили фото
        Glide.with(context)
                .load(product.getImageURL())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.imageView);

        // Установили наименование
        holder.textViewName.setText(product.getName());

        // Установили массу
        holder.textViewMass.setText(String.format("%s г", product.getMass()));
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    // Обновление списка с учетом предыдущего содержимого
    public void submitList(List<Product> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new GenericDiffCallback<>(
                        currentList,                         // старый список
                        newList,                             // новый список
                        (oldProduct, newProduct) -> Objects.equals(oldProduct.getName(), newProduct.getName()), // сравнение ID
                        Product::equals                           // сравнение содержимого
                )
        );
        currentList.clear();
        currentList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    // Класс текущего элементов view
    public static final class ProductSelectedHolder extends RecyclerView.ViewHolder {
        // Фото продукта
        private final ImageView imageView;
        // Наименование продукта
        private final TextView textViewName;
        // Масса продукта
        private final TextView textViewMass;

        public ProductSelectedHolder(@NonNull View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            textViewName = view.findViewById(R.id.nameDish);
            textViewMass = view.findViewById(R.id.textViewMass);
        }
    }
}
