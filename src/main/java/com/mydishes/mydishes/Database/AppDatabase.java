package com.mydishes.mydishes.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mydishes.mydishes.Database.dao.DishDao;
import com.mydishes.mydishes.Database.dao.NutritionDao;
import com.mydishes.mydishes.Database.dao.ProductDao;
import com.mydishes.mydishes.Database.model.Dish;
import com.mydishes.mydishes.Database.model.DishProductCrossRef;
import com.mydishes.mydishes.Database.model.Nutrition;
import com.mydishes.mydishes.Database.model.Product;

@Database(entities = {Dish.class, Nutrition.class, Product.class, DishProductCrossRef.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String dishesDatabaseName = "dishesDatabase";
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, dishesDatabaseName)
                            // .fallbackToDestructiveMigration() // Если нужно, для простоты при изменении схемы
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract DishDao dishDao();

    public abstract NutritionDao nutritionDao();

    public abstract ProductDao productDao();
}
