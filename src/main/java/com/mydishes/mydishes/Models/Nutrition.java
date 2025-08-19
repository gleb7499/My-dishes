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
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Список продуктов не может быть пустым");
        }

        double kcal = 0, protein = 0, fat = 0, carbs = 0, mass = 0;
        Nutrition result = new Nutrition();

        // Посчитаем КБЖУ на всю массу продуктов
        for (Product p : products) {
            Nutrition n = p.getNutrition();
            if (n == null) {
                throw new IllegalArgumentException("Пищевая ценность продукта " + p.getName() + " не может быть null");
            }
            float currentProductMass = p.getMass();

            mass += currentProductMass;

            // Вычисляем коэффициент масштабирования один раз для каждого продукта
            double scaleFactor = currentProductMass / 100.0;

            // КБЖУ всей массы текущего продукта
            kcal += n.getCalories() * scaleFactor;
            protein += n.getProtein() * scaleFactor;
            fat += n.getFat() * scaleFactor;
            carbs += n.getCarb() * scaleFactor;
        }

        if (mass == 0) {
            throw new IllegalArgumentException("Общая масса продуктов не может быть равна нулю");
        }

        result.setCalories(round(100.0 * kcal / mass));
        result.setProtein(round(100.0 * protein / mass));
        result.setFat(round(100.0 * fat / mass));
        result.setCarb(round(100.0 * carbs / mass));

        // Результат -> новый объект Nutrition со значениями КБЖУ блюда
        return result;
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
