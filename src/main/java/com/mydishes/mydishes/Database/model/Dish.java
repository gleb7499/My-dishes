package com.mydishes.mydishes.Database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "dishes",
        foreignKeys = @ForeignKey(entity = Nutrition.class,
                parentColumns = "id",
                childColumns = "nutritionId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "nutritionId")})
public class Dish {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;     // Наименование
    public String photoUri; // Ссылка на фото

    @ColumnInfo(name = "nutritionId")
    public long nutritionId; // Внешний ключ для Nutrition

    // Пустой конструктор для Room
    public Dish() {
    }

    // Конструктор для удобства (без ID, так как он autoGenerate)
    public Dish(String name, String photoUri, long nutritionId) {
        this.name = name;
        this.photoUri = photoUri;
        this.nutritionId = nutritionId;
    }
}
