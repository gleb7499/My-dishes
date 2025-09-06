package com.mydishes.mydishes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Абстрактный базовый класс для адаптеров RecyclerView, использующих ListAdapter.
 *
 * @param <T>  Тип элементов в списке.
 * @param <VH> Тип ViewHolder.
 */
public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends ListAdapter<T, VH> {

    /**
     * Конструктор для BaseAdapter.
     *
     * @param diffCallback Объект для расчета различий между списками.
     */
    protected BaseAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    /**
     * Возвращает идентификатор макета для указанного типа представления.
     *
     * @param viewType Тип представления.
     * @return Идентификатор макета.
     */
    @LayoutRes
    protected abstract int getLayoutId(int viewType);

    /**
     * Создает ViewHolder для указанного представления и типа представления.
     *
     * @param itemView Представление элемента.
     * @param viewType Тип представления.
     * @return Созданный ViewHolder.
     */
    protected abstract VH createViewHolder(@NonNull View itemView, int viewType);

    /**
     * Привязывает данные элемента к ViewHolder.
     *
     * @param holder ViewHolder для привязки данных.
     * @param item   Элемент данных для привязки.
     */
    protected abstract void bind(@NonNull VH holder, @NonNull T item);

    /**
     * Вызывается при создании нового ViewHolder.
     *
     * @param parent   Родительская ViewGroup.
     * @param viewType Тип представления.
     * @return Созданный ViewHolder.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Получение LayoutInflater из контекста родительского представления
        View itemView = LayoutInflater.from(parent.getContext())
                // Заполнение макета элемента
                .inflate(getLayoutId(viewType), parent, false);
        // Создание и возврат ViewHolder
        return createViewHolder(itemView, viewType);
    }

    /**
     * Вызывается для отображения данных в указанной позиции.
     *
     * @param holder   ViewHolder для обновления.
     * @param position Позиция элемента в списке.
     */
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // Получение элемента данных по позиции
        T item = getItem(position);
        // Проверка, что элемент не null
        if (item != null) {
            // Привязка данных к ViewHolder
            bind(holder, item);
        }
    }
}
