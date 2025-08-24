package com.mydishes.mydishes.Adapters;

import static com.mydishes.mydishes.Utils.ViewUtils.parseFloatSafe;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Models.ProductsSelectedManager;
import com.mydishes.mydishes.Parser.EdostavkaParser;
import com.mydishes.mydishes.Parser.Parser;
import com.mydishes.mydishes.Parser.ParsingStateListener;
import com.mydishes.mydishes.Parser.ProductParseCallback;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.Utils.DialogUtils;

import java.util.Objects;

public class ProductFindAdapter extends BaseAdapter<Product, ProductFindAdapter.ProductFindViewHolder> {

    private final Context context;
    private final ParsingStateListener parsingStateListener;
    private final Parser parser = new EdostavkaParser();

    public ProductFindAdapter(@NonNull Activity activity, ParsingStateListener listener) {
        super(new ProductDiffCallback());
        this.context = activity;
        this.parsingStateListener = listener;
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.list_item_product;
    }

    @Override
    protected ProductFindViewHolder createViewHolder(@NonNull View itemView, int viewType) {
        return new ProductFindViewHolder(itemView);
    }

    @Override
    protected void bind(@NonNull ProductFindViewHolder holder, @NonNull Product product) {
        // Установили фото продукта
        Glide.with(context) // Используем context из конструктора адаптера
                .load(product.getImageURL())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.productImage);

        // Установили наименование продукта
        holder.productName.setText(product.getName());

        // здесь масса продукта пока что не задана
        holder.productMass.setVisibility(View.GONE);

        // Создаем диалог с вводом массы и обрабатываем его логику
        holder.itemView.setOnClickListener(v -> {
            DialogUtils.showInputMassDialog(context, product.getName(), massStr -> {
                // Получаем КБЖУ продукта с сайта
                parser.parseProductDetailsAsync(product, new ProductParseCallback<>() {
                    @Override
                    public void onParsingStarted() {
                        if (parsingStateListener != null) {
                            parsingStateListener.onParsingStarted();
                        }
                    }

                    @Override
                    public void onSuccess(Product parsedProduct) {
                        parsedProduct.setMass(parseFloatSafe(massStr)); // устанавливаем массу
                        ProductsSelectedManager.add(parsedProduct);
                        Snackbar.make(holder.itemView, "Записан " + parsedProduct.getName(), Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Snackbar.make(holder.itemView, "Ошибка парсинга: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onParsingFinished() {
                        if (parsingStateListener != null) {
                            parsingStateListener.onParsingFinished();
                        }
                    }
                });
            });
        });
    }

    public static final class ProductFindViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productMass;

        public ProductFindViewHolder(View view) {
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
