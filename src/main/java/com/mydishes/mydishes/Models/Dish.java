package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Класс-модель для блюда
public class Dish implements Cloneable {
    private long id; // ID
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        if (!(o instanceof Dish dish)) return false;
        return id == dish.id && Objects.equals(name, dish.name) && Objects.equals(photoUri, dish.photoUri) && Objects.equals(nutrition, dish.nutrition) && Objects.equals(products, dish.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, photoUri, nutrition, products);
    }

    @NonNull
    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", photoUri='" + photoUri + '\'' +
                ", nutrition=" + nutrition +
                ", products=" + products +
                '}';
    }

    @NonNull
    @Override
    public Dish clone() {
        try {
            Dish clonedDish = (Dish) super.clone();
            clonedDish.id = this.id; // long is primitive, direct copy is fine
            clonedDish.name = this.name; // String is immutable
            clonedDish.photoUri = this.photoUri; // String is immutable

            // Deep copy for Nutrition
            if (this.nutrition != null) {
                clonedDish.nutrition = this.nutrition.clone();
            }

            // Deep copy for List<Product>
            if (this.products != null) {
                clonedDish.products = new ArrayList<>();
                for (Product product : this.products) {
                    if (product != null) {
                        clonedDish.products.add(product.clone());
                    } else {
                        clonedDish.products.add(null);
                    }
                }
            }
            return clonedDish;
        } catch (CloneNotSupportedException e) {
            // This should not happen since we are implementing Cloneable
            throw new AssertionError();
        }
    }
}
