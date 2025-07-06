package com.mydishes.mydishes.Models;

import java.util.ArrayList;
import java.util.List;

public class DishesManager {
    private static final List<Dish> DISH_LIST = new ArrayList<>();

    public static void add(Dish dish) {
        DISH_LIST.add(dish);
    }

    public static Dish get(int i) {
        return DISH_LIST.get(i);
    }

    public static void clear() {
        DISH_LIST.clear();
    }

    public static int size() {
        return DISH_LIST.size();
    }

    public static boolean isEmpty() {
        return DISH_LIST.isEmpty();
    }

    public static final class Dish {
        private String url;
        private String image;
        private String name;
        private short calories;
        private short protein;
        private short fat;
        private short carb;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public short getCalories() {
            return calories;
        }

        public void setCalories(short calories) {
            this.calories = calories;
        }

        public short getProtein() {
            return protein;
        }

        public void setProtein(short protein) {
            this.protein = protein;
        }

        public short getFat() {
            return fat;
        }

        public void setFat(short fat) {
            this.fat = fat;
        }

        public short getCarb() {
            return carb;
        }

        public void setCarb(short carb) {
            this.carb = carb;
        }
    }
}
