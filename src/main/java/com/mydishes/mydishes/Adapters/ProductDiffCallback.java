package com.mydishes.mydishes.Adapters;

import androidx.recyclerview.widget.DiffUtil;

import com.mydishes.mydishes.Models.Product;

import java.util.List;

public class ProductDiffCallback extends DiffUtil.Callback {

    private final List<Product> oldList;
    private final List<Product> newList;

    public ProductDiffCallback(List<Product> oldList, List<Product> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getProductURL().equals(newList.get(newItemPosition).getProductURL());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
