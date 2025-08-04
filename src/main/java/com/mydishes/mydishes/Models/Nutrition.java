package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import java.util.Objects;

// Класс-модель представления КБЖУ блюда/продукта
public class Nutrition {
    private double calories; // ккалории
    private double protein; // белки
    private double fat; // жиры
    private double carb; // углеводы

    public Nutrition(double calories, double protein, double fat, double carb) {
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
    }

    public Nutrition() {
        // Empty constructor!
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getCarb() {
        return carb;
    }

    public void setCarb(double carb) {
        this.carb = carb;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Nutrition)) return false;
        Nutrition nutrition = (Nutrition) o;
        return Double.compare(calories, nutrition.calories) == 0 && Double.compare(protein, nutrition.protein) == 0 && Double.compare(fat, nutrition.fat) == 0 && Double.compare(carb, nutrition.carb) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(calories, protein, fat, carb);
    }

    @NonNull
    @Override
    public String toString() {
        return "Nutrition{" +
                "calories=" + calories +
                ", protein=" + protein +
                ", fat=" + fat +
                ", carb=" + carb +
                '}';
    }
}
