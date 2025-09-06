package com.mydishes.mydishes.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Objects;

// Класс-модель представления продукта
public class Product implements Parcelable { // Removed implements Cloneable
    private long id; // ID
    private String productURL; // ссылка на страницу продукта сайта парсинга
    private String imageURL; // ссылка на фотографию продукта
    private String name; // наименование продукта
    private Nutrition nutrition; // объект КБЖУ
    private float mass; // масса продукта

    public static final Creator<Product> CREATOR = new Creator<>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public Product() {
        // Default constructor
    }

    protected Product(@NonNull Parcel in) {
        id = in.readLong();
        productURL = in.readString();
        imageURL = in.readString();
        name = in.readString();
        nutrition = readParcelableCompat(in, Nutrition.class);
        mass = in.readFloat();
    }

    @SuppressWarnings("deprecation")
    private static <T extends Parcelable> T readParcelableCompat(Parcel in, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return in.readParcelable(clazz.getClassLoader(), clazz);
        } else {
            return in.readParcelable(clazz.getClassLoader());
        }
    }


    // Factory method
    @NonNull
    public static Product createProduct(@NonNull Product originalProduct) {
        Product newProduct = new Product();
        newProduct.setId(originalProduct.getId());
        newProduct.setProductURL(originalProduct.getProductURL());
        newProduct.setImageURL(originalProduct.getImageURL());
        newProduct.setName(originalProduct.getName());
        if (originalProduct.getNutrition() != null) {
            // Assuming Nutrition also has a factory method or a copy constructor
            newProduct.setNutrition(Nutrition.createNutrition(originalProduct.getNutrition()));
        }
        newProduct.setMass(originalProduct.getMass());
        return newProduct;
    }

    // Вычисляет итоговое значение КБЖУ для блюда по списку продуктов
    @NonNull
    public static Nutrition calculate(@NonNull final List<Product> products) {
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Список продуктов не может быть пустым!");
        }

        double kcal = 0;
        double protein = 0;
        double fat = 0;
        double carbs = 0;
        double mass = 0;
        Nutrition result = new Nutrition();

        // Посчитаем КБЖУ на всю массу продуктов
        for (Product p : products) {
            Nutrition n = p.getNutrition();
            if (n == null) {
                throw new IllegalArgumentException("Пищевая ценность продукта " + p.getName() + " не может быть null");
            }
            float currentProductMass = p.getMass();

            mass += currentProductMass;

            // Вычисляем коэффициент масштабирования один раз для каждого продукта
            double scaleFactor = currentProductMass / 100.0;

            // КБЖУ всей массы текущего продукта
            kcal += n.getCalories() * scaleFactor;
            protein += n.getProtein() * scaleFactor;
            fat += n.getFat() * scaleFactor;
            carbs += n.getCarb() * scaleFactor;
        }

        if (mass == 0) {
            throw new IllegalArgumentException("Общая масса продуктов не может быть равна нулю");
        }

        result.setCalories(round(100.0 * kcal / mass));
        result.setProtein(round(100.0 * protein / mass));
        result.setFat(round(100.0 * fat / mass));
        result.setCarb(round(100.0 * carbs / mass));

        // Результат -> новый объект Nutrition со значениями КБЖУ блюда
        return result;
    }

    // Метод для корректного округления значения
    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(productURL);
        dest.writeString(imageURL);
        dest.writeString(name);
        dest.writeParcelable(nutrition, flags);
        dest.writeFloat(mass);
    }
}
