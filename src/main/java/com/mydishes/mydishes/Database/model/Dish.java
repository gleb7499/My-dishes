package com.mydishes.mydishes.Database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Представляет блюдо в базе данных.
 */
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

    /**
     * Пустой конструктор для Room.
     */
    public Dish() {
    }

    /**
     * Конструктор для создания объекта Dish.
     *
     * @param name        Название блюда.
     * @param photoUri    URI фотографии блюда.
     * @param nutritionId Идентификатор пищевой ценности.
     */
    public Dish(String name, String photoUri, long nutritionId) {
        this.name = name;
        this.photoUri = photoUri;
        this.nutritionId = nutritionId;
    }
}
