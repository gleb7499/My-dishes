package com.mydishes.mydishes.utils;

import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.Models.Product;

import java.util.List;

public class DishCalculator {

    public static Dish calculate(String dishName, String photoUri, List<Product> products) {
        double kcal = 0, protein = 0, fat = 0, carbs = 0;

        for (Product p : products) {
            double ratio = p.getMass() / 100.0;
            Nutrition n = p.getNutrition();

            kcal    += n.getCalories() * ratio;
            protein += n.getProtein() * ratio;
            fat     += n.getFat() * ratio;
            carbs   += n.getCarb() * ratio;
        }

        Nutrition nutrition = new Nutrition(round(kcal), round(protein), round(fat), round(carbs));

        return new Dish(dishName, photoUri, nutrition, products);
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

}

