package com.mydishes.mydishes.Parser;

import android.app.Activity;

import com.mydishes.mydishes.Models.Product;

import java.util.List;

public abstract class Parser {
    protected static final int MAX_RESULTS = 25; // Лимит на количество объектов

    public abstract List<Product> findProducts(String query) throws Exception;

    public void findProductsAsync(Activity activity, String query, ProductFindCallback productFindCallback) {
        new Thread(() -> {
            try {
                List<Product> products = findProducts(query);
                activity.runOnUiThread(() -> productFindCallback.onSuccess(products));
            } catch (Exception e) {
                activity.runOnUiThread(() -> productFindCallback.onError(e));
            }
        }).start();
    }

    public abstract Product parseProductDetails(Product product) throws Exception;

    public void parseProductDetailsAsync(Activity activity, Product product, ProductParseCallback productParseCallback) {
        new Thread(() -> {
            try {
                Product result = parseProductDetails(product);
                activity.runOnUiThread(() -> productParseCallback.onSuccess(result));
            } catch (Exception e) {
                activity.runOnUiThread(() -> productParseCallback.onError(e));
            }
        }).start();
    }
}
