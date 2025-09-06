package com.mydishes.mydishes.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mydishes.mydishes.database.model.Nutrition;

import java.util.List;

@Dao
public interface NutritionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNutrition(Nutrition nutrition);

    @Query("SELECT * FROM nutrition")
    List<Nutrition> getAllNutrition();

    @Query("SELECT * FROM nutrition WHERE id = :nutritionId")
    Nutrition getNutritionById(long nutritionId);

    @Update
    void updateNutrition(Nutrition nutrition);

    @Query("DELETE FROM nutrition WHERE id = :nutritionId")
    void deleteNutritionById(long nutritionId);
}
