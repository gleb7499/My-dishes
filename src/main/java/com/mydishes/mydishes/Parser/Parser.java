package com.mydishes.mydishes.Parser;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.mydishes.mydishes.Models.Product;

import java.util.List;

/**
 * Абстрактный класс для парсинга данных о продуктах.
 * <p>
 * Предоставляет базовую функциональность для асинхронного поиска продуктов и
 * получения детальной информации о продукте. Класс предназначен для наследования.
 * Наследники должны реализовать методы {@link #findProducts(String)} и
 * {@link #parseProductDetails(Product)} для конкретного источника данных (например, веб-сайта).
 * </p>
 */
public abstract class Parser {
    /**
     * Максимальное количество результатов, возвращаемых при поиске продуктов.
     */
    protected static final int MAX_RESULTS = 25;

    /**
     * Абстрактный метод для поиска списка продуктов по заданному запросу.
     * <p>
     * Этот метод должен быть реализован в классах-наследниках для выполнения
     * поиска продуктов на конкретном веб-сайте или другом источнике данных.
     * </p>
     *
     * @param query Поисковый запрос.
     * @return Список найденных продуктов ({@link Product}).
     * @throws Exception Если во время парсинга произошла ошибка.
     */
    public abstract List<Product> findProducts(String query) throws Exception;

    /**
     * Асинхронно ищет список продуктов по заданному запросу.
     * <p>
     * Уведомляет о стадиях и результате парсинга через {@link ProductParseCallback}.
     * Контракт вызова колбэка:
     * <ol>
     *     <li>{@link ProductParseCallback#onParsingStarted()} вызывается немедленно перед началом асинхронной операции.</li>
     *     <li>Затем, в основном потоке, вызывается либо {@link ProductParseCallback#onSuccess(Object)} с полученным списком продуктов при успехе,
     *         либо {@link ProductParseCallback#onError(Exception)} при ошибке.</li>
     *     <li>{@link ProductParseCallback#onParsingFinished()} вызывается в основном потоке после {@code onSuccess} или {@code onError}.</li>
     * </ol>
     * </p>
     *
     * @param query    Поисковый запрос.
     * @param callback Колбэк для уведомления о результате парсинга.
     */
    public void findProductsAsync(String query, @NonNull ProductParseCallback<List<Product>> callback) {
        callback.onParsingStarted();
        new Thread(() -> {
            try {
                List<Product> products = findProducts(query);
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onSuccess(products);
                    callback.onParsingFinished();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError(e);
                    callback.onParsingFinished();
                });
            }
        }).start();
    }

    /**
     * Абстрактный метод для парсинга детальной информации о продукте (например, КБЖУ).
     * <p>
     * Этот метод должен быть реализован в классах-наследниках для извлечения
     * подробной информации о конкретном продукте с веб-сайта или другого источника данных.
     * </p>
     *
     * @param product Продукт ({@link Product}), для которого необходимо получить детали.
     * @return Обновленный объект {@link Product} с детальной информацией.
     * @throws Exception Если во время парсинга произошла ошибка.
     */
    public abstract Product parseProductDetails(Product product) throws Exception;

    /**
     * Асинхронно парсит детальную информацию о продукте (например, КБЖУ).
     * <p>
     * Уведомляет о стадиях и результате парсинга через {@link ProductParseCallback}.
     * Контракт вызова колбэка:
     * <ol>
     *     <li>{@link ProductParseCallback#onParsingStarted()} вызывается немедленно перед началом асинхронной операции.</li>
     *     <li>Затем, в основном потоке, вызывается либо {@link ProductParseCallback#onSuccess(Object)} с обновленным продуктом при успехе,
     *         либо {@link ProductParseCallback#onError(Exception)} при ошибке.</li>
     *     <li>{@link ProductParseCallback#onParsingFinished()} вызывается в основном потоке после {@code onSuccess} или {@code onError}.</li>
     * </ol>
     * </p>
     *
     * @param product  Продукт ({@link Product}), для которого необходимо получить детали.
     * @param callback Колбэк для уведомления о результате парсинга.
     */
    public void parseProductDetailsAsync(Product product, @NonNull ProductParseCallback<Product> callback) {
        callback.onParsingStarted();
        new Thread(() -> {
            try {
                Product result = parseProductDetails(product);
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onSuccess(result);
                    callback.onParsingFinished();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError(e);
                    callback.onParsingFinished();
                });
            }
        }).start();
    }
}
