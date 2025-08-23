package com.mydishes.mydishes.Database.model.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.mydishes.mydishes.Database.model.Nutrition;
import com.mydishes.mydishes.Database.model.Product;

public class ProductWithNutrition {
    @Embedded
    public Product product;

    @Relation(
            parentColumn = "nutritionId",
            entityColumn = "id"
    )
    public Nutrition nutrition;
}
