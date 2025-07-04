package com.mydishes.mydishes.Models;

public class Dishes {
    private String url;
    private String image;
    private String name;
    private short calories;
    private short protein;
    private short fat;
    private short carb;

    public Dishes(String url, String image, String name, short calories, short protein, short fat, short carb) {
        this.url = url;
        this.image = image;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
    }

    public Dishes() {
    }

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
