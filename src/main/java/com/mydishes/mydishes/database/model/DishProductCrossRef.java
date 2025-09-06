package com.mydishes.mydishes.database.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/**
 * Представляет перекрестную ссылку для связи многие-ко-многим
 * между таблицами 'dishes' и 'products'.
 */
@Entity(tableName = "dish_product_cross_ref",
        primaryKeys = {"dishId", "productId"},
        foreignKeys = {
                @ForeignKey(entity = Dish.class,
                        parentColumns = "id",
                        childColumns = "dishId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Product.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index(value = "dishId"), @Index(value = "productId")}
)
public class DishProductCrossRef {
    public long dishId;    // Внешний ключ для Dish
    public long productId; // Внешний ключ для Product

    /**
     * Конструктор для создания объекта DishProductCrossRef.
     *
     * @param dishId    Идентификатор блюда.
     * @param productId Идентификатор продукта.
     */
    public DishProductCrossRef(long dishId, long productId) {
        this.dishId = dishId;
        this.productId = productId;
    }
}
