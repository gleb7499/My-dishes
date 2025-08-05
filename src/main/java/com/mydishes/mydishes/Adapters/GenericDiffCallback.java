package com.mydishes.mydishes.Utils;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;
import java.util.function.BiPredicate;

// Обобщённый класс для сравнения двух списков значений
// Основное применение — сравнение элементов в RecyclerView при обновлении данных
// Позволяет переиспользовать один и тот же класс для разных типов данных (например, Product, Dish и др.)
// Принимает логику сравнения элементов через функции:
// - areItemsTheSame — определяет, считаются ли элементы одним и тем же объектом
// - areContentsTheSame — определяет, изменилось ли содержимое объекта
public class GenericDiffCallback<T> extends DiffUtil.Callback {

    private final List<T> oldList;
    private final List<T> newList;
    private final BiPredicate<T, T> areItemsTheSame;
    private final BiPredicate<T, T> areContentsTheSame;

    public GenericDiffCallback(List<T> oldList, List<T> newList,
                               BiPredicate<T, T> areItemsTheSame,
                               BiPredicate<T, T> areContentsTheSame) {
        this.oldList = oldList;
        this.newList = newList;
        this.areItemsTheSame = areItemsTheSame;
        this.areContentsTheSame = areContentsTheSame;
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
        return areItemsTheSame.test(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return areContentsTheSame.test(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }
}
