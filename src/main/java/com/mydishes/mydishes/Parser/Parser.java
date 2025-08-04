package com.mydishes.mydishes.Parser;

import android.app.Activity;

import com.mydishes.mydishes.Models.Product;

import java.util.List;

// Абстрактный класс-парсер. Представляет функционал для запуска парсинга в асинхронном режиме и обработки результата.
// В том числе обрабатывает ошибки классов наследников.
// Если планируется добавить еще классы парсинга для других сайтов (где 1 сайт = 1 класс), необходимо
// наследовать новый класс от данного и переопределить два метода, реализующих логику парсинга.
public abstract class Parser {
    protected static final int MAX_RESULTS = 25; // Лимит на количество объектов

    // Абстрактный метод (Поиск списка продуктов на сайте по запросу String query)
    public abstract List<Product> findProducts(String query) throws Exception;

    // Асинхронный запуск findProducts с обработкой результата.
    // После парсинга вызывается метод класса наследника интерфейса ProductFindCallback в главном потоке (для этого и нужна Activity):
    //     - onSuccess -- в случае успеха парсинга с передачей готового Product
    //     - onError   -- при ошибке парсинга с передачей ошибки
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

    // Абстрактный метод (Считывание КБЖУ со страницы конкретного продукта)
    public abstract Product parseProductDetails(Product product) throws Exception;

    // Работает так же, как предыдущий асинхронный парсинг, только для метода parseProductDetails
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
