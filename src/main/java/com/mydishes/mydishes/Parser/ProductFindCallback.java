package com.mydishes.mydishes.Parser;

import com.mydishes.mydishes.Models.Product;

import java.util.List;

// Интерфейс обратного вызовано для обработки результатов парсинга списка продуктов с сайта
public interface ProductFindCallback {
    // Успешный парсинг, передаем список считанных продуктов
    void onSuccess(List<Product> products);

    // Ошибка парсинга, передаем ошибку
    void onError(Exception e);
}
