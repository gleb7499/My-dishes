package com.mydishes.mydishes.Models;

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
        private short calories;
        private short protein;
        private short fat;
        private short carb;

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
