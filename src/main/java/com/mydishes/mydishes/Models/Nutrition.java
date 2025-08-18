package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.List;
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

    // Вычисляет итоговое значение КБЖУ для блюда по списку продуктов
    @NonNull
    @Contract("_ -> new")
    public static Nutrition calculate(@NonNull List<Product> products) {
        double kcal = 0, protein = 0, fat = 0, carbs = 0;

        for (Product p : products) {
            double ratio = p.getMass() / 100.0;
            Nutrition n = p.getNutrition();

            kcal += n.getCalories() * ratio;
            protein += n.getProtein() * ratio;
            fat += n.getFat() * ratio;
            carbs += n.getCarb() * ratio;
        }

        // Результат -> новый объект Nutrition со значениями КБЖУ блюда
        return new Nutrition(round(kcal), round(protein), round(fat), round(carbs));
    }

    // Метод для корректного округления значения
    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Nutrition nutrition)) return false;
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
