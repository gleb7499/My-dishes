package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

// Класс-менеджер управления единым списком выбранных продуктов для текущего блюда
public class ProductsSelectedManager {

    private static List<Product> products = new ArrayList<>();

    public static void add(@NonNull Product product) {
        products.add(product);
    }

    public static void clear() {
        products.clear();
    }

    @NonNull
    @Contract(" -> new")
    public static List<Product> getAll() {
        return new ArrayList<>(products); // Возвращает новую оболочку списка, содержащую управляемые экземпляры продуктов
    }

    public static int size() {
        return products.size();
    }

    public static void setAll(List<Product> updatedList) {
        products = new ArrayList<>(updatedList);
    }

    public static void remove(Product product) {
        products.remove(product); // Зависит от product.equals() или равенства ссылок
    }
}
