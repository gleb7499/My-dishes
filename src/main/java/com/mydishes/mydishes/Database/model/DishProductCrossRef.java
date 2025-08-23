package com.mydishes.mydishes.Database.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

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
    public long dishId;
    public long productId;

    public DishProductCrossRef(long dishId, long productId) {
        this.dishId = dishId;
        this.productId = productId;
    }
}
