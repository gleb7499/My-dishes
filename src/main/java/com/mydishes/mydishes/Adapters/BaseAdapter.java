package com.mydishes.mydishes.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends ListAdapter<T, VH> {

    protected BaseAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    @LayoutRes
    protected abstract int getLayoutId(int viewType);

    protected abstract VH createViewHolder(@NonNull View itemView, int viewType);

    protected abstract void bind(@NonNull VH holder, @NonNull T item);

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutId(viewType), parent, false);
        return createViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        T item = getItem(position);
        if (item != null) {
            bind(holder, item);
        }
    }
}
