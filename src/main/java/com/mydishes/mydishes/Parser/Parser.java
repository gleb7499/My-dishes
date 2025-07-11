package com.mydishes.mydishes.Parser;

import com.mydishes.mydishes.Models.ProductsManager;

public abstract class Parser {
    protected static final int MAX_RESULTS = 25; // Лимит на количество объектов

    public abstract void findProducts(String query) throws Exception;

    public void findProductsAsync(String query, ProductFindCallback productFindCallback) {
        new Thread(() -> {
            try {
                findProducts(query);
                productFindCallback.onSuccess();
            } catch (Exception e) {
                productFindCallback.onError(e);
            }
        }).start();
    }

    public abstract ProductsManager.Product parseProductDetails(ProductsManager.Product product) throws Exception;

    public void parseProductDetailsAsync(ProductsManager.Product product, ProductParseCallback productParseCallback) {
        new Thread(() -> {
            try {
                ProductsManager.Product result = parseProductDetails(product);
                productParseCallback.onSuccess(result);
            } catch (Exception e) {
                productParseCallback.onError(e);
            }
        }).start();
    }
}
