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

/**
 * Основной класс базы данных приложения, использующий Room Persistence Library.
 * Определяет сущности базы данных, версию и предоставляет доступ к DAO (Data Access Objects).
 * Реализован как Singleton для обеспечения единственного экземпляра базы данных во всем приложении.
 * <p>
 * Сущности, включенные в базу данных:
 * <ul>
 *     <li>{@link Dish} - представляет блюдо.</li>
 *     <li>{@link Nutrition} - представляет пищевую ценность (КБЖУ).</li>
 *     <li>{@link Product} - представляет продукт/ингредиент.</li>
 *     <li>{@link DishProductCrossRef} - представляет связующую таблицу для отношения "многие-ко-многим" между блюдами и продуктами.</li>
 * </ul>
 * Версия базы данных: 1. `exportSchema` установлено в `false` для отключения экспорта схемы в JSON файл,
 * что обычно полезно для более сложных проектов с миграциями, но не обязательно для этого примера.
 */
@Database(entities = {Dish.class, Nutrition.class, Product.class, DishProductCrossRef.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Имя файла базы данных
    private static final String DATABASE_NAME = "dishes_database";
    // Переменная для хранения единственного экземпляра AppDatabase (Singleton)
    private static volatile AppDatabase INSTANCE;

    /**
     * Возвращает единственный экземпляр {@link AppDatabase}.
     * Если экземпляр еще не создан, он будет инициализирован потокобезопасным образом.
     *
     * @param context Контекст приложения. Используется {@link Context#getApplicationContext()} для предотвращения утечек памяти.
     * @return Единственный экземпляр {@link AppDatabase}.
     */
    public static AppDatabase getDatabase(final Context context) {
        // Первая проверка (без блокировки) для производительности
        if (INSTANCE == null) {
            // Блок синхронизации для потокобезопасного создания экземпляра
            synchronized (AppDatabase.class) {
                // Вторая проверка (внутри блока синхронизации)
                if (INSTANCE == null) {
                    // Создание экземпляра базы данных с использованием Room.databaseBuilder
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            // Важно: fallbackToDestructiveMigration() удалит и пересоздаст базу данных при изменении версии.
                            // Это простое решение для разработки, но для продакшена требуются стратегии миграции.
                            // .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        // Возврат существующего или только что созданного экземпляра
        return INSTANCE;
    }

    /**
     * Предоставляет доступ к Data Access Object (DAO) для операций с сущностью {@link Dish}.
     *
     * @return Экземпляр {@link DishDao}.
     */
    public abstract DishDao dishDao();

    /**
     * Предоставляет доступ к Data Access Object (DAO) для операций с сущностью {@link Nutrition}.
     *
     * @return Экземпляр {@link NutritionDao}.
     */
    public abstract NutritionDao nutritionDao();

    /**
     * Предоставляет доступ к Data Access Object (DAO) для операций с сущностью {@link Product}.
     *
     * @return Экземпляр {@link ProductDao}.
     */
    public abstract ProductDao productDao();
}
