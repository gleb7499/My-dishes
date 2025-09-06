package com.mydishes.mydishes.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

// Класс-модель представления КБЖУ блюда/продукта
public class Nutrition implements Parcelable { // Removed implements Cloneable
    private long id; // ID
    private double calories; // ккалории
    private double protein; // белки
    private double fat; // жиры
    private double carb; // углеводы

    public Nutrition(double calories, double protein, double fat, double carb) {
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
    }

    public Nutrition() {
        // Empty constructor!
    }

    public static final Creator<Nutrition> CREATOR = new Creator<>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public Nutrition createFromParcel(Parcel in) {
            return new Nutrition(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public Nutrition[] newArray(int size) {
            return new Nutrition[size];
        }
    };

    protected Nutrition(@NonNull Parcel in) {
        id = in.readLong();
        calories = in.readDouble();
        protein = in.readDouble();
        fat = in.readDouble();
        carb = in.readDouble();
    }

    // Factory method
    @NonNull
    public static Nutrition createNutrition(@NonNull Nutrition originalNutrition) {
        Nutrition newNutrition = new Nutrition();
        newNutrition.setId(originalNutrition.getId());
        newNutrition.setCalories(originalNutrition.getCalories());
        newNutrition.setProtein(originalNutrition.getProtein());
        newNutrition.setFat(originalNutrition.getFat());
        newNutrition.setCarb(originalNutrition.getCarb());
        return newNutrition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getCarb() {
        return carb;
    }

    public void setCarb(double carb) {
        this.carb = carb;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Nutrition nutrition)) return false;
        return Double.compare(calories, nutrition.calories) == 0 && Double.compare(protein, nutrition.protein) == 0 && Double.compare(fat, nutrition.fat) == 0 && Double.compare(carb, nutrition.carb) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(calories, protein, fat, carb);
    }

    @NonNull
    @Override
    public String toString() {
        return "Nutrition{" +
                "calories=" + calories +
                ", protein=" + protein +
                ", fat=" + fat +
                ", carb=" + carb +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeDouble(calories);
        dest.writeDouble(protein);
        dest.writeDouble(fat);
        dest.writeDouble(carb);
    }
}
