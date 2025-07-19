package com.mydishes.mydishes.Models;

import java.util.ArrayList;
import java.util.List;

public class SelectedProductsManager {

    private static final List<Product> products = new ArrayList<>();

    // Интерфейс слушателя
    public interface OnDishProductsChangeListener {
        void onProductsChanged(List<Product> newList);
    }

    private static final List<OnDishProductsChangeListener> listeners = new ArrayList<>();

    public static void add(Product product) {
        products.add(product);
        notifyListeners();
    }

    public static void clear() {
        products.clear();
        notifyListeners();
    }

    public static List<Product> getAll() {
        return new ArrayList<>(products); // копия, чтобы нельзя было напрямую мутировать
    }

    public static int size() {
        return products.size();
    }

    public static void registerListener(OnDishProductsChangeListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public static void unregisterListener(OnDishProductsChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (OnDishProductsChangeListener listener : listeners) {
            listener.onProductsChanged(getAll());
        }
    }
}
