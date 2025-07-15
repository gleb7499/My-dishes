package com.mydishes.mydishes.Parser;

import com.mydishes.mydishes.Models.Product;

import java.util.List;

public interface ProductFindCallback {
    void onSuccess(List<Product> products);

    void onError(Exception e);
}
