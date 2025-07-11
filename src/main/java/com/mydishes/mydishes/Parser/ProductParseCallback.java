package com.mydishes.mydishes.Parser;

import com.mydishes.mydishes.Models.ProductsManager;

public interface ProductParseCallback {
    void onSuccess(ProductsManager.Product result);

    void onError(Exception e);
}
