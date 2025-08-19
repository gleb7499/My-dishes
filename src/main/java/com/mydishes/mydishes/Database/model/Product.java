package com.mydishes.mydishes.Database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "products",
        foreignKeys = @ForeignKey(entity = Nutrition.class,
                parentColumns = "id",
                childColumns = "nutritionId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "nutritionId")})
public class Product {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String productURL; // ссылка на страницу продукта сайта парсинга
    public String imageURL;   // ссылка на фотографию продукта
    public String name;       // наименование продукта

    @ColumnInfo(name = "nutritionId")
    public long nutritionId;  // Внешний ключ для Nutrition

    public float mass;       // масса продукта

    // Пустой конструктор для Room
    public Product() {
    }

    // Конструктор для удобства (без ID, так как он autoGenerate)
    public Product(String productURL, String imageURL, String name, long nutritionId, float mass) {
        this.productURL = productURL;
        this.imageURL = imageURL;
        this.name = name;
        this.nutritionId = nutritionId;
        this.mass = mass;
    }
}
