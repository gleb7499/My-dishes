package com.mydishes.mydishes.Database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Представляет пищевую ценность (БЖУ и калории) в базе данных.
 */
@Entity(tableName = "nutrition")
public class Nutrition {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public double calories; // ккалории
    public double protein;  // белки
    public double fat;      // жиры
    public double carb;     // углеводы

    /**
     * Пустой конструктор для Room.
     */
    public Nutrition() {
    }

    /**
     * Конструктор для создания объекта Nutrition.
     *
     * @param calories Калорийность.
     * @param protein  Количество белков.
     * @param fat      Количество жиров.
     * @param carb     Количество углеводов.
     */
    public Nutrition(double calories, double protein, double fat, double carb) {
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
    }
}
