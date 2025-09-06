package com.mydishes.mydishes.database.model.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.mydishes.mydishes.database.model.Nutrition;
import com.mydishes.mydishes.database.model.Product;

/**
 * Представляет продукт вместе с его пищевой ценностью.
 * Используется для запросов, объединяющих таблицы Product и Nutrition.
 */
public class ProductWithNutrition {
    /**
     * Встроенный объект Product.
     */
    @Embedded
    public Product product;

    /**
     * Связанный объект Nutrition.
     * Связь осуществляется через поле nutritionId в Product и id в Nutrition.
     */
    @Relation(
            parentColumn = "nutritionId",
            entityColumn = "id"
    )
    public Nutrition nutrition;
}
