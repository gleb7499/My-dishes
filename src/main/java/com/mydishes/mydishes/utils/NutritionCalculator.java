package com.mydishes.mydishes.utils;

import androidx.annotation.NonNull;

import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.Models.Product;

import org.jetbrains.annotations.Contract;

import java.util.List;

// Класс для преобразований типов и вычислений КБЖУ.
public class NutritionCalculator {

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

    // Преобразует строку в float, безопасно
    public static float parseFloatSafe(@NonNull String text) {
        try {
            return (float) Double.parseDouble(text.replace(",", ".").replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
