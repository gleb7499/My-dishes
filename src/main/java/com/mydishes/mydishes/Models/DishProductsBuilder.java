package com.mydishes.mydishes.Models;

import java.util.ArrayList;
import java.util.List;

public class DishProductsBuilder {
    private final static List<Product> PRODUCT_LIST = new ArrayList<>();

    public static boolean add(Product product) {
        return PRODUCT_LIST.add(product);
    }

    public static Product get(int i) {
        return PRODUCT_LIST.get(i);
    }

    public static int size() {
        return PRODUCT_LIST.size();
    }
}
