package com.mydishes.mydishes.database.model.relations;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.mydishes.mydishes.database.model.Dish;
import com.mydishes.mydishes.database.model.DishProductCrossRef;
import com.mydishes.mydishes.database.model.Nutrition;
import com.mydishes.mydishes.database.model.Product;

import java.util.List;

/**
 * Представляет блюдо вместе с его пищевой ценностью и списком продуктов (также с их пищевой ценностью).
 * Используется для комплексных запросов к базе данных.
 */
public class DishWithProductsAndNutrition {
    /**
     * Встроенный объект Dish.
     */
    @Embedded
    public Dish dish;

    /**
     * Связанный объект Nutrition для самого блюда.
     * Связь осуществляется через поле nutritionId в Dish и id в Nutrition.
     */
    @Relation(
            parentColumn = "nutritionId", // из Dish
            entityColumn = "id"          // из Nutrition
    )
    public Nutrition dishNutrition; // КБЖУ самого блюда

    /**
     * Список продуктов, входящих в состав блюда.
     * Связь многие-ко-многим через таблицу DishProductCrossRef.
     * Пищевую ценность каждого продукта (ProductWithNutrition) нужно будет загружать отдельно
     * или обрабатывать в DataRepository.
     */
    @Relation(
            parentColumn = "id", // Dish.id
            entityColumn = "id", // Product.id
            associateBy = @Junction(
                    value = DishProductCrossRef.class,
                    parentColumn = "dishId",
                    entityColumn = "productId"
            ),
            entity = Product.class // Указываем, что это список Product
    )
    // Мы не можем напрямую вложить ProductWithNutrition сюда из-за ограничений Room
    // Сначала получим продукты, а их КБЖУ нужно будет загружать отдельно или через ProductDao
    public List<Product> products; // Список продуктов для блюда

    // Чтобы полностью соответствовать вашему исходному Dish классу, 
    // вам нужно будет преобразовать List<Product> в List<ProductWithNutrition>
    // в DataRepository после получения данных.
}
