package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import java.util.Objects;

// Класс-модель представления продукта
public class Product implements Cloneable { // Added implements Cloneable
    private long id; // ID
    private String productURL; // ссылка на страницу продукта сайта парсинга
    private String imageURL; // ссылка на фотографию продукта
    private String name; // наименование продукта
    private Nutrition nutrition; // объект КБЖУ
    private float mass; // масса продукта

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public Nutrition getNutrition() {
        return nutrition;
    }

    public void setNutrition(Nutrition nutrition) {
        this.nutrition = nutrition;
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
                ", nutrition=" + nutrition +
                ", mass=" + mass +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Product product)) return false;
        return Float.compare(mass, product.mass) == 0 && Objects.equals(productURL, product.productURL) && Objects.equals(imageURL, product.imageURL) && Objects.equals(name, product.name) && Objects.equals(nutrition, product.nutrition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productURL, imageURL, name, nutrition, mass);
    }

    // Added clone method
    @NonNull
    @Override
    public Product clone() {
        try {
            Product clonedProduct = (Product) super.clone();
            // Deep copy for Nutrition object
            if (this.nutrition != null) {
                clonedProduct.nutrition = this.nutrition.clone();
            }
            return clonedProduct;
        } catch (CloneNotSupportedException e) {
            // This should not happen since we are Cloneable
            throw new AssertionError();
        }
    }
}
