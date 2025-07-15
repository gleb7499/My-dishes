package com.mydishes.mydishes.Parser;

import com.mydishes.mydishes.Models.Product;

public interface ProductParseCallback {
    void onSuccess(Product result);

    void onError(Exception e);
}
