package com.mydishes.mydishes.models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс-менеджер управления единым списком выбранных продуктов для текущего блюда.
 * Этот класс предоставляет статические методы для управления списком продуктов.
 */
public class ProductsSelectedManager {

    private static List<Product> products = new ArrayList<>();

    /**
     * Добавляет продукт в список выбранных продуктов.
     *
     * @param product Продукт для добавления. Не должен быть null.
     */
    public static void add(@NonNull Product product) {
        products.add(product);
    }

    /**
     * Очищает список выбранных продуктов.
     */
    public static void clear() {
        products.clear();
    }

    /**
     * Возвращает копию списка всех выбранных продуктов.
     *
     * @return Новый список, содержащий все выбранные продукты.
     */
    @NonNull
    @Contract(" -> new")
    public static List<Product> getAll() {
        return new ArrayList<>(products); // Возвращает новую оболочку списка, содержащую управляемые экземпляры продуктов
    }

    /**
     * Заменяет текущий список выбранных продуктов новым списком.
     * @param updatedList Новый список продуктов.
     */
    public static void setAll(List<Product> updatedList) {
        products = new ArrayList<>(updatedList);
    }

    /**
     * Возвращает количество выбранных продуктов.
     *
     * @return Количество продуктов в списке.
     */
    public static int size() {
        return products.size();
    }

    /**
     * Удаляет указанный продукт из списка выбранных продуктов.
     * Удаление зависит от реализации метода {@code equals()} класса {@code Product}.
     * @param product Продукт для удаления.
     */
    public static void remove(Product product) {
        products.remove(product); // Зависит от product.equals() или равенства ссылок
    }
}
