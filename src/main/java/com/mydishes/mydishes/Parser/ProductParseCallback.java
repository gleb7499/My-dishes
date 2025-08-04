package com.mydishes.mydishes.Parser;

import com.mydishes.mydishes.Models.Product;

// Интерфейс обратного вызова для обработки результатов парсинга КБЖУ конкретного продукта с сайта
public interface ProductParseCallback {
    // Успешный парсинг, передаем Product с заполненными КБЖУ
    void onSuccess(Product result);

    // Ошибка парсинга, передаем ошибку
    void onError(Exception e);
}
