package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

// Класс-модель для блюда
public class Dish {
    private String name; // Наименование
    private String photoUri; // Ссылка на фото
    private Nutrition nutrition; // Объект содержания КБЖУ
    private List<Product> products; // Список продуктов текущего блюда

    public Dish(String name, String photoUri, Nutrition nutrition, List<Product> products) {
        this.name = name;
        this.photoUri = photoUri;
        this.nutrition = nutrition;
        this.products = products;
    }

    public Dish() {
        // Empty constructor!
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public Nutrition getNutrition() {
        return nutrition;
    }

    public void setNutrition(Nutrition nutrition) {
        this.nutrition = nutrition;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Dish)) return false;
        Dish dish = (Dish) o;
        return Objects.equals(name, dish.name) && Objects.equals(photoUri, dish.photoUri) && Objects.equals(nutrition, dish.nutrition) && Objects.equals(products, dish.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, photoUri, nutrition, products);
    }

    @NonNull
    @Override
    public String toString() {
        return "Dish{" +
                "name='" + name + '\'' +
                ", photoUri='" + photoUri + '\'' +
                ", nutrition=" + nutrition +
                ", products=" + products +
                '}';
    }
}
