package com.mydishes.mydishes.utils;

import com.mydishes.mydishes.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductListUpdater {

    /**
     * Обновляет продукт в списке, возвращая новый список с обновленным продуктом.
     *
     * @param currentProducts   Текущий список продуктов.
     * @param productFromBundle Продукт (обычно из Bundle), содержащий ID для поиска и, возможно, старые данные.
     * @param newMass           Новая масса для продукта.
     * @return Новый список продуктов с обновленным элементом или null, если продукт не был найден или входные данные некорректны.
     */
    public static List<Product> updateProductInList(List<Product> currentProducts, Product productFromBundle, float newMass) {
        if (productFromBundle == null || currentProducts == null) {
            // Возвращаем null или пустой список, или исходный список в зависимости от желаемой обработки ошибки
            return null;
        }

        List<Product> newProductList = new ArrayList<>(currentProducts.size());
        boolean productUpdated = false;

        for (Product p_iterator : currentProducts) {
            // Используем ID для идентификации продукта. Это надежнее, чем equals() или name.
            if (p_iterator.equals(productFromBundle)) {
                Product updatedProduct = Product.createProduct(p_iterator); // Клонируем существующий продукт
                updatedProduct.setMass(newMass);             // Обновляем массу у клона
                // Если Nutrition продукта зависит только от его массы и должно быть обновлено
                // на этом этапе, можно добавить сюда:
                // e.g., updatedProduct.setNutrition(Nutrition.calculateForOneProduct(updatedProduct));
                newProductList.add(updatedProduct);          // Добавляем обновленный клон в новый список
                productUpdated = true;
            } else {
                newProductList.add(p_iterator); // Добавляем неизмененный продукт как есть
            }
        }

        return productUpdated ? newProductList : null; // Возвращаем новый список, если было обновление, иначе null (означает, что продукт не найден)
    }
}
