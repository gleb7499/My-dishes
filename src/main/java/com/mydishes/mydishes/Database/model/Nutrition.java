package com.mydishes.mydishes.Database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "nutrition")
public class Nutrition {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public double calories; // ккалории
    public double protein;  // белки
    public double fat;      // жиры
    public double carb;     // углеводы

    // Пустой конструктор для Room
    public Nutrition() {
    }

    // Конструктор для удобства
    public Nutrition(double calories, double protein, double fat, double carb) {
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
    }
}
