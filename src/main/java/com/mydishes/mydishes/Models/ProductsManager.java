package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ProductsManager {
    private static final List<Product> PRODUCT_LIST = new ArrayList<>();

    public static void add(Product product) {
        PRODUCT_LIST.add(product);
    }

    public static Product get(int i) {
        return PRODUCT_LIST.get(i);
    }

    public static void clear() {
        PRODUCT_LIST.clear();
    }

    public static int size() {
        return PRODUCT_LIST.size();
    }

    public static boolean isEmpty() {
        return PRODUCT_LIST.isEmpty();
    }

    public static final class Product {
        private String productURL;
        private String imageURL;
        private String name;
        private float calories;
        private float protein;
        private float fat;
        private float carb;
        private float mass;

        public String getProductURL() {
            return productURL;
        }

        public void setProductURL(String productURL) {
            this.productURL = productURL;
        }

        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getCalories() {
            return calories;
        }

        public void setCalories(float calories) {
            this.calories = calories;
        }

        public float getProtein() {
            return protein;
        }

        public void setProtein(float protein) {
            this.protein = protein;
        }

        public float getFat() {
            return fat;
        }

        public void setFat(float fat) {
            this.fat = fat;
        }

        public float getCarb() {
            return carb;
        }

        public void setCarb(float carb) {
            this.carb = carb;
        }

        public float getMass() {
            return mass;
        }

        public void setMass(float mass) {
            this.mass = mass;
        }

        @NonNull
        @Override
        public String toString() {
            return "Product{" +
                    "productURL='" + productURL + '\'' +
                    ", imageURL='" + imageURL + '\'' +
                    ", name='" + name + '\'' +
                    ", calories=" + calories +
                    ", protein=" + protein +
                    ", fat=" + fat +
                    ", carb=" + carb +
                    ", mass=" + mass +
                    '}';
        }
    }
}
