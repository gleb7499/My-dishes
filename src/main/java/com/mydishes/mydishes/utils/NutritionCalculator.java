package com.mydishes.mydishes.utils;

import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.Models.Product;

import java.util.List;

public class NutritionCalculator {

    public static Nutrition calculate(List<Product> products) {
        double kcal = 0, protein = 0, fat = 0, carbs = 0;

        for (Product p : products) {
            double ratio = p.getMass() / 100.0;
            Nutrition n = p.getNutrition();

            kcal    += n.getCalories() * ratio;
            protein += n.getProtein() * ratio;
            fat     += n.getFat() * ratio;
            carbs   += n.getCarb() * ratio;
        }

        return new Nutrition(round(kcal), round(protein), round(fat), round(carbs));
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

    /**
     * Преобразует строку в float, безопасно.
     */
    public static float parseFloatSafe(String text) {
        try {
            return (float) Double.parseDouble(text.replace(",", ".").replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}

