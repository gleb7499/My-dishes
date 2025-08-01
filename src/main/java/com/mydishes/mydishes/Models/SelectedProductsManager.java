package com.mydishes.mydishes.Models;

import java.util.ArrayList;
import java.util.List;

public class SelectedProductsManager {

    private static final List<Product> products = new ArrayList<>();

    public static void add(Product product) {
        products.add(product);
    }

    public static void clear() {
        products.clear();
    }

    public static List<Product> getAll() {
        return new ArrayList<>(products); // копия, чтобы нельзя было напрямую мутировать
    }

    public static int size() {
        return products.size();
    }
}
