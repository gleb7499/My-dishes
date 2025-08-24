package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

// Класс-менеджер управления единым списком выбранных продуктов для текущего блюда
public class ProductsSelectedManager {

    private static final List<Product> products = new ArrayList<>();

    public static void add(Product product) {
        products.add(product);
    }

    public static void clear() {
        products.clear();
    }

    @NonNull
    @Contract(" -> new")
    public static List<Product> getAll() {
        return new ArrayList<>(products); // копия, чтобы нельзя было напрямую мутировать
    }

    public static int size() {
        return products.size();
    }

    public static void remove(Product product) {
        products.remove(product);
    }

    public static void updateMass(@NonNull Product product, float mass) {
        product.setMass(mass);
    }
}
