package com.mydishes.mydishes.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.mydishes.mydishes.database.model.Dish;
import com.mydishes.mydishes.database.model.DishProductCrossRef;
import com.mydishes.mydishes.database.model.relations.DishWithProductsAndNutrition;

import java.util.List;

@Dao
public interface DishDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertDish(Dish dish);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDishProductCrossRef(DishProductCrossRef crossRef);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDishProductCrossRefs(List<DishProductCrossRef> crossRefs);

    @Transaction
    @Query("SELECT * FROM dishes")
    List<DishWithProductsAndNutrition> getAllDishesWithProductsAndNutrition();

    @Transaction
    @Query("SELECT * FROM dishes WHERE id = :dishId")
    DishWithProductsAndNutrition getDishWithProductsAndNutrition(long dishId);

    // Получить все Dish (без вложенных данных, если нужно только список названий, например)
    @Query("SELECT * FROM dishes")
    List<Dish> getAllDishesSimple();

    @Query("DELETE FROM dishes WHERE id = :dishId")
    int deleteDishById(long dishId);

    @Update
    void updateDish(Dish dish);

    @Query("DELETE FROM dish_product_cross_ref WHERE dishId = :dishId")
    void deleteDishProductCrossRefsByDishId(long dishId);
}
