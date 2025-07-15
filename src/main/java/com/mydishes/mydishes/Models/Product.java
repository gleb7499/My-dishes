package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import java.util.Objects;


public class Product {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Float.compare(calories, product.calories) == 0 && Float.compare(protein, product.protein) == 0 && Float.compare(fat, product.fat) == 0 && Float.compare(carb, product.carb) == 0 && Float.compare(mass, product.mass) == 0 && Objects.equals(productURL, product.productURL) && Objects.equals(imageURL, product.imageURL) && Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productURL, imageURL, name, calories, protein, fat, carb, mass);
    }
}
