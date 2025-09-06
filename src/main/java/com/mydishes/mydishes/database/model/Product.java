package com.mydishes.mydishes.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Представляет продукт в базе данных.
 */
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

    /**
     * Пустой конструктор для Room.
     */
    public Product() {
    }

    /**
     * Конструктор для создания объекта Product.
     *
     * @param productURL  URL страницы продукта.
     * @param imageURL    URL изображения продукта.
     * @param name        Название продукта.
     * @param nutritionId Идентификатор пищевой ценности.
     * @param mass        Масса продукта.
     */
    public Product(String productURL, String imageURL, String name, long nutritionId, float mass) {
        this.productURL = productURL;
        this.imageURL = imageURL;
        this.name = name;
        this.nutritionId = nutritionId;
        this.mass = mass;
    }
}
