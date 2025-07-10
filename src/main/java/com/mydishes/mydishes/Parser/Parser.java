package com.mydishes.mydishes.Parser;

import com.mydishes.mydishes.Models.ProductsManager;

public abstract class Parser {
    protected static final int MAX_RESULTS = 25; // Лимит на количество объектов


    public abstract void findProducts(String query) throws Exception;
    public abstract ProductsManager.Product parseProductDetails(ProductsManager.Product product) throws Exception;

}
