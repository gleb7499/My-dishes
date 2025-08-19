package com.mydishes.mydishes.Database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.mydishes.mydishes.Database.model.Product;
import com.mydishes.mydishes.Database.model.relations.ProductWithNutrition;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProduct(Product product);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<Product> products);

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId")
    ProductWithNutrition getProductWithNutritionById(long productId);

    @Transaction
    @Query("SELECT * FROM products")
    List<ProductWithNutrition> getAllProductsWithNutrition();

    @Transaction
    @Query("SELECT * FROM products WHERE id IN (:productIds)")
    List<ProductWithNutrition> getProductsWithNutritionByIds(List<Long> productIds);
}
