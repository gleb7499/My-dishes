package com.mydishes.mydishes.Database.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mydishes.mydishes.Database.AppDatabase;
import com.mydishes.mydishes.Database.dao.DishDao;
import com.mydishes.mydishes.Database.dao.NutritionDao;
import com.mydishes.mydishes.Database.dao.ProductDao;
import com.mydishes.mydishes.Database.model.Dish;
import com.mydishes.mydishes.Database.model.DishProductCrossRef;
import com.mydishes.mydishes.Database.model.Nutrition;
import com.mydishes.mydishes.Database.model.Product;
import com.mydishes.mydishes.Database.model.relations.DishWithProductsAndNutrition;
import com.mydishes.mydishes.Database.model.relations.ProductWithNutrition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Репозиторий для управления данными, связанными с блюдами, продуктами и их пищевой ценностью.
 * Обеспечивает абстракцию над источниками данных (в данном случае, Room базу данных).
 */
public class DataRepository {
    private static final String TAG = "DataRepository";
    private static volatile DataRepository instance;
    private final DishDao dishDao;
    private final ProductDao productDao;
    private final NutritionDao nutritionDao;
    private final ExecutorService executorService;

    /**
     * Приватный конструктор репозитория.
     *
     * @param application Контекст приложения для инициализации базы данных.
     */
    private DataRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        dishDao = db.dishDao();
        productDao = db.productDao();
        nutritionDao = db.nutritionDao();
        executorService = Executors.newSingleThreadExecutor(); // Для простоты, можно использовать более сложный пул
    }

    /**
     * Возвращает единственный экземпляр DataRepository.
     *
     * @param application Контекст приложения.
     * @return Экземпляр DataRepository.
     */
    public static DataRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (DataRepository.class) {
                if (instance == null) {
                    instance = new DataRepository(application);
                }
            }
        }
        return instance;
    }

    /**
     * Приватный вспомогательный метод.
     * Конвертирует объект {@link ProductWithNutrition} из базы данных
     * в объект модели {@link com.mydishes.mydishes.Models.Product}.
     * @param pwn Объект {@link ProductWithNutrition} из БД.
     * @return Адаптированный объект {@link com.mydishes.mydishes.Models.Product}.
     */
    @NonNull
    private static com.mydishes.mydishes.Models.Product getProduct(@NonNull ProductWithNutrition pwn) {
        com.mydishes.mydishes.Models.Product appProduct = new com.mydishes.mydishes.Models.Product();
        appProduct.setName(pwn.product.name);
        appProduct.setProductURL(pwn.product.productURL);
        appProduct.setImageURL(pwn.product.imageURL);
        appProduct.setMass(pwn.product.mass);

        if (pwn.nutrition != null) {
            com.mydishes.mydishes.Models.Nutrition productNutrition = new com.mydishes.mydishes.Models.Nutrition();
            productNutrition.setCalories(pwn.nutrition.calories);
            productNutrition.setProtein(pwn.nutrition.protein);
            productNutrition.setFat(pwn.nutrition.fat);
            productNutrition.setCarb(pwn.nutrition.carb);
            appProduct.setNutrition(productNutrition);
        }
        return appProduct;
    }

    /**
     * Приватный вспомогательный метод.
     * Адаптирует объект модели {@link com.mydishes.mydishes.Models.Nutrition}
     * к сущности базы данных {@link Nutrition}.
     * @param originalNutrition Оригинальный объект {@link com.mydishes.mydishes.Models.Nutrition}.
     * @return Сущность {@link Nutrition} для сохранения в БД.
     */
    private Nutrition adaptNutrition(com.mydishes.mydishes.Models.Nutrition originalNutrition) {
        if (originalNutrition == null) return null;
        return new Nutrition(
                originalNutrition.getCalories(),
                originalNutrition.getProtein(),
                originalNutrition.getFat(),
                originalNutrition.getCarb()
        );
    }

    /**
     * Приватный вспомогательный метод.
     * Адаптирует объект модели {@link com.mydishes.mydishes.Models.Product}
     * к сущности базы данных {@link Product}.
     * @param originalProduct Оригинальный объект {@link com.mydishes.mydishes.Models.Product}.
     * @param nutritionId ID связанной пищевой ценности в БД.
     * @return Сущность {@link Product} для сохранения в БД.
     */
    private Product adaptProduct(com.mydishes.mydishes.Models.Product originalProduct, long nutritionId) {
        if (originalProduct == null) return null;
        return new Product(
                originalProduct.getProductURL(),
                originalProduct.getImageURL(),
                originalProduct.getName(),
                nutritionId,
                originalProduct.getMass()
        );
    }

    /**
     * Сохраняет блюдо со всей его детализацией (пищевая ценность блюда, продукты и их пищевая ценность) в базу данных.
     * Выполняется асинхронно.
     * @param originalDish Объект {@link com.mydishes.mydishes.Models.Dish} для сохранения.
     * @return {@link Future} с ID сохраненного блюда.
     */
    public Future<Long> insertDishWithDetails(com.mydishes.mydishes.Models.Dish originalDish) {
        // Выполняем операцию в отдельном потоке
        return executorService.submit(() -> {
            // 1. Адаптируем и сохраняем пищевую ценность самого блюда
            Nutrition dishNutritionEntity = adaptNutrition(originalDish.getNutrition());
            long dishNutritionId = nutritionDao.insertNutrition(dishNutritionEntity); // Метод DAO для вставки Nutrition и получения его ID

            // 2. Адаптируем и сохраняем основную информацию о блюде, связывая его с ID пищевой ценности
            Dish dishEntity = new Dish(originalDish.getName(), originalDish.getPhotoUri(), dishNutritionId);
            long dishId = dishDao.insertDish(dishEntity); // Метод DAO для вставки Dish и получения его ID

            // 3. Если у блюда есть продукты, обрабатываем каждый из них
            if (originalDish.getProducts() != null) {
                List<DishProductCrossRef> crossRefs = new ArrayList<>();
                for (com.mydishes.mydishes.Models.Product originalProduct : originalDish.getProducts()) {
                    // 3.1 Адаптируем и сохраняем пищевую ценность текущего продукта
                    Nutrition productNutritionEntity = adaptNutrition(originalProduct.getNutrition());
                    long productNutritionId = nutritionDao.insertNutrition(productNutritionEntity);

                    // 3.2 Адаптируем и сохраняем текущий продукт, связывая его с ID его пищевой ценности
                    Product productEntity = adaptProduct(originalProduct, productNutritionId);
                    long productId = productDao.insertProduct(productEntity); // Метод DAO для вставки Product и получения его ID

                    // 3.3 Создаем запись для таблицы связей "многие-ко-многим" между блюдом и продуктом
                    crossRefs.add(new DishProductCrossRef(dishId, productId));
                }
                // 3.4 Если были созданы связи, вставляем их всех разом в таблицу связей
                if (!crossRefs.isEmpty()) {
                    dishDao.insertDishProductCrossRefs(crossRefs); // Метод DAO для массовой вставки связей
                }
            }
            // Возвращаем ID сохраненного блюда
            return dishId;
        });
    }

    /**
     * Получает блюдо по его ID вместе со всей связанной информацией (пищевая ценность блюда,
     * список продуктов и их пищевая ценность).
     * Адаптирует результат к модели {@link com.mydishes.mydishes.Models.Dish}.
     * Выполняется асинхронно.
     * @param dishId ID запрашиваемого блюда.
     * @return {@link Future} с объектом {@link com.mydishes.mydishes.Models.Dish} или null, если блюдо не найдено.
     */
    public Future<com.mydishes.mydishes.Models.Dish> getDishById(long dishId) {
        return executorService.submit(() -> {
            DishWithProductsAndNutrition dishDetails = dishDao.getDishWithProductsAndNutrition(dishId);
            if (dishDetails == null) {
                return null;
            }

            // Адаптируем обратно к вашей модели com.mydishes.mydishes.Models.Dish
            com.mydishes.mydishes.Models.Dish resultDish = new com.mydishes.mydishes.Models.Dish();
            resultDish.setName(dishDetails.dish.name);
            resultDish.setPhotoUri(dishDetails.dish.photoUri);

            if (dishDetails.dishNutrition != null) {
                com.mydishes.mydishes.Models.Nutrition mainNutrition = new com.mydishes.mydishes.Models.Nutrition();
                mainNutrition.setCalories(dishDetails.dishNutrition.calories);
                mainNutrition.setProtein(dishDetails.dishNutrition.protein);
                mainNutrition.setFat(dishDetails.dishNutrition.fat);
                mainNutrition.setCarb(dishDetails.dishNutrition.carb);
                resultDish.setNutrition(mainNutrition);
            }

            if (dishDetails.products != null && !dishDetails.products.isEmpty()) {
                List<com.mydishes.mydishes.Models.Product> resultProducts = new ArrayList<>();
                List<Long> productIds = dishDetails.products.stream().map(p -> p.id).collect(Collectors.toList());

                // Получаем все продукты с их КБЖУ за один запрос к ProductDao
                List<ProductWithNutrition> productsWithNutrition = productDao.getProductsWithNutritionByIds(productIds);

                // Создаем Map для быстрого доступа к ProductWithNutrition по ID продукта
                java.util.Map<Long, ProductWithNutrition> productMap = productsWithNutrition.stream()
                        .collect(Collectors.toMap(pwn -> pwn.product.id, pwn -> pwn));

                for (Product dbProduct : dishDetails.products) {
                    ProductWithNutrition pwn = productMap.get(dbProduct.id);
                    if (pwn != null) {
                        com.mydishes.mydishes.Models.Product appProduct = getProduct(pwn);
                        resultProducts.add(appProduct);
                    }
                }
                resultDish.setProducts(resultProducts);
            }
            return resultDish;
        });
    }

    /**
     * Получает список всех блюд с основной информацией (id, название, URI фото).
     * Не загружает продукты или детальную пищевую ценность для экономии ресурсов.
     * Предназначен для отображения в списках.
     * Выполняется асинхронно.
     * @return {@link Future} со списком объектов {@link com.mydishes.mydishes.Models.Dish}.
     */
    public Future<List<com.mydishes.mydishes.Models.Dish>> getAllDishesSimple() {
        return executorService.submit(() -> {
            List<Dish> dbDishes = dishDao.getAllDishesSimple();
            List<com.mydishes.mydishes.Models.Dish> resultDishes = new ArrayList<>();
            for (Dish dbDish : dbDishes) {
                com.mydishes.mydishes.Models.Dish appDish = new com.mydishes.mydishes.Models.Dish();
                appDish.setId(dbDish.id);
                appDish.setName(dbDish.name);
                appDish.setPhotoUri(dbDish.photoUri);
                resultDishes.add(appDish);
            }
            return resultDishes;
        });
    }

    /**
     * Получает список всех блюд со всей их детализацией (пищевая ценность блюда,
     * список продуктов и их пищевая ценность).
     * Может быть ресурсоемким при большом количестве блюд.
     * Выполняется асинхронно.
     * @return {@link Future} со списком объектов {@link com.mydishes.mydishes.Models.Dish}.
     */
    public Future<List<com.mydishes.mydishes.Models.Dish>> getAllDishesWithDetails() {
        return executorService.submit(() -> {
            List<DishWithProductsAndNutrition> allDbDishDetails = dishDao.getAllDishesWithProductsAndNutrition();
            List<com.mydishes.mydishes.Models.Dish> resultAppDishes = new ArrayList<>();

            for (DishWithProductsAndNutrition dishDetails : allDbDishDetails) {
                com.mydishes.mydishes.Models.Dish resultDish = new com.mydishes.mydishes.Models.Dish();
                resultDish.setName(dishDetails.dish.name);
                resultDish.setPhotoUri(dishDetails.dish.photoUri);

                if (dishDetails.dishNutrition != null) {
                    com.mydishes.mydishes.Models.Nutrition mainNutrition = new com.mydishes.mydishes.Models.Nutrition();
                    mainNutrition.setCalories(dishDetails.dishNutrition.calories);
                    mainNutrition.setProtein(dishDetails.dishNutrition.protein);
                    mainNutrition.setFat(dishDetails.dishNutrition.fat);
                    mainNutrition.setCarb(dishDetails.dishNutrition.carb);
                    resultDish.setNutrition(mainNutrition);
                }

                if (dishDetails.products != null && !dishDetails.products.isEmpty()) {
                    List<com.mydishes.mydishes.Models.Product> resultProducts = new ArrayList<>();
                    List<Long> productIds = dishDetails.products.stream().map(p -> p.id).collect(Collectors.toList());
                    List<ProductWithNutrition> productsWithNutrition = productDao.getProductsWithNutritionByIds(productIds);

                    java.util.Map<Long, ProductWithNutrition> productMap = productsWithNutrition.stream()
                            .collect(Collectors.toMap(pwn -> pwn.product.id, pwn -> pwn));

                    for (Product dbProduct : dishDetails.products) {
                        ProductWithNutrition pwn = productMap.get(dbProduct.id);
                        if (pwn != null) {
                            com.mydishes.mydishes.Models.Product appProduct = getProduct(pwn);
                            resultProducts.add(appProduct);
                        }
                    }
                    resultDish.setProducts(resultProducts);
                }
                resultAppDishes.add(resultDish);
            }
            return resultAppDishes;
        });
    }

    /**
     * Пример использования методов репозитория.
     * Демонстрирует сохранение и последующее получение блюда.
     * <br> Внимание: {@link Future#get()} является блокирующим вызовом и не должен использоваться в UI потоке.
     * @param myDishToSave Объект {@link com.mydishes.mydishes.Models.Dish} для демонстрации сохранения.
     */
    public void exampleUsage(com.mydishes.mydishes.Models.Dish myDishToSave) {
        Future<Long> futureDishId = insertDishWithDetails(myDishToSave);
        try {
            Long savedDishId = futureDishId.get(); // Блокирующий вызов, используйте осторожно!
            Log.d(TAG, "Dish saved with ID: " + savedDishId);

            if (savedDishId != null) {
                Future<com.mydishes.mydishes.Models.Dish> futureRetrievedDish = getDishById(savedDishId);
                com.mydishes.mydishes.Models.Dish retrievedDish = futureRetrievedDish.get(); // Блокирующий
                if (retrievedDish != null) {
                    Log.d(TAG, "Retrieved Dish: " + retrievedDish.getName());
                    if (retrievedDish.getProducts() != null) {
                        Log.d(TAG, "Products count: " + retrievedDish.getProducts().size());
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error operating with database", e);
            Thread.currentThread().interrupt(); // Restore interruption status
        }
    }
}
