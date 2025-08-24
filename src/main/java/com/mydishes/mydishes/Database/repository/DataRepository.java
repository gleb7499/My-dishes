package com.mydishes.mydishes.Database.repository;

import android.app.Activity;
import android.content.Context;
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
import java.util.Map;
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
     * Приватный вспомогательный метод.
     * Конвертирует объект {@link ProductWithNutrition} из базы данных
     * в объект модели {@link com.mydishes.mydishes.Models.Product}.
     *
     * @param pwn Объект {@link ProductWithNutrition} из БД.
     * @return Адаптированный объект {@link com.mydishes.mydishes.Models.Product}.
     */
    @NonNull
    private static com.mydishes.mydishes.Models.Product getProduct(@NonNull ProductWithNutrition pwn) {
        com.mydishes.mydishes.Models.Product appProduct = new com.mydishes.mydishes.Models.Product();
        appProduct.setId(pwn.product.id);
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
     * Приватный конструктор репозитория.
     *
     * @param context Контекст приложения для инициализации базы данных.
     */
    private DataRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        dishDao = db.dishDao();
        productDao = db.productDao();
        nutritionDao = db.nutritionDao();
        executorService = Executors.newSingleThreadExecutor(); // Для простоты, можно использовать более сложный пул
    }

    /**
     * Возвращает единственный экземпляр DataRepository.
     *
     * @param context Контекст приложения.
     * @return Экземпляр DataRepository.
     */
    public static DataRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (DataRepository.class) {
                if (instance == null) {
                    instance = new DataRepository(context);
                }
            }
        }
        return instance;
    }

    /**
     * Приватный вспомогательный метод.
     * Адаптирует объект модели {@link com.mydishes.mydishes.Models.Nutrition}
     * к сущности базы данных {@link Nutrition}.
     *
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
     *
     * @param originalProduct Оригинальный объект {@link com.mydishes.mydishes.Models.Product}.
     * @param nutritionId     ID связанной пищевой ценности в БД.
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
     *
     * @param originalDish Объект {@link com.mydishes.mydishes.Models.Dish} для сохранения.
     * @return {@link Future} с ID сохраненного блюда.
     */
    public void insertDishWithDetails(Activity activity, com.mydishes.mydishes.Models.Dish originalDish, QueryCallBack<Long> queryCallBack) {
        new Thread(() -> {
            try {
                long dishId = executorService.submit(() -> {
                    // 1. Адаптируем и сохраняем пищевую ценность самого блюда
                    Nutrition dishNutritionEntity = adaptNutrition(originalDish.getNutrition());
                    long dishNutritionId = nutritionDao.insertNutrition(dishNutritionEntity); // Метод DAO для вставки Nutrition и получения его ID

                    // 2. Адаптируем и сохраняем основную информацию о блюде, связывая его с ID пищевой ценности
                    Dish dishEntity = new Dish(originalDish.getName(), originalDish.getPhotoUri(), dishNutritionId);
                    long savedDishId = dishDao.insertDish(dishEntity); // Метод DAO для вставки Dish и получения его ID

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
                            crossRefs.add(new DishProductCrossRef(savedDishId, productId));
                        }
                        // 3.4 Если были созданы связи, вставляем их всех разом в таблицу связей
                        if (!crossRefs.isEmpty()) {
                            dishDao.insertDishProductCrossRefs(crossRefs); // Метод DAO для массовой вставки связей
                        }
                    }
                    return savedDishId;
                }).get();

                activity.runOnUiThread(() -> queryCallBack.onSuccess(dishId));
            } catch (Exception e) {
                activity.runOnUiThread(() -> queryCallBack.onError(e));
            }
        }).start();
    }

    /**
     * Получает блюдо по его ID вместе со всей связанной информацией (пищевая ценность блюда,
     * список продуктов и их пищевая ценность).
     * Адаптирует результат к модели {@link com.mydishes.mydishes.Models.Dish}.
     * Выполняется асинхронно.
     *
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
     *
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
     *
     * @return {@link Future} со списком объектов {@link com.mydishes.mydishes.Models.Dish}.
     */
    public void getAllDishesWithDetails(Activity activity, QueryCallBack<List<com.mydishes.mydishes.Models.Dish>> queryCallBack) {
        new Thread(() -> {
            try {
                List<com.mydishes.mydishes.Models.Dish> dishes = getAllDishesWithDetails();
                activity.runOnUiThread(() -> queryCallBack.onSuccess(dishes));
            } catch (Exception e) {
                activity.runOnUiThread(() -> queryCallBack.onError(e));
            }
        }).start();
    }

    private List<com.mydishes.mydishes.Models.Dish> getAllDishesWithDetails() throws ExecutionException, InterruptedException {
        return executorService.submit(() -> {
            List<DishWithProductsAndNutrition> allDbDishDetails = dishDao.getAllDishesWithProductsAndNutrition();
            List<com.mydishes.mydishes.Models.Dish> resultAppDishes = new ArrayList<>();

            for (DishWithProductsAndNutrition dishDetails : allDbDishDetails) {
                com.mydishes.mydishes.Models.Dish resultDish = new com.mydishes.mydishes.Models.Dish();
                resultDish.setId(dishDetails.dish.id);
                resultDish.setName(dishDetails.dish.name);
                resultDish.setPhotoUri(dishDetails.dish.photoUri);

                if (dishDetails.dishNutrition != null) {
                    com.mydishes.mydishes.Models.Nutrition mainNutrition = new com.mydishes.mydishes.Models.Nutrition();
                    mainNutrition.setId(dishDetails.dishNutrition.id);
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

                    Map<Long, ProductWithNutrition> productMap = productsWithNutrition.stream()
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
        }).get();
    }

    /**
     * Deletes a dish by its ID.
     * This operation is performed asynchronously.
     * Due to CASCADE delete rules in the database schema, this will also remove:
     * - The Nutrition entry associated with this Dish.
     * - All DishProductCrossRef entries linking this Dish to Products.
     * Product entities themselves will NOT be deleted.
     *
     * @param activity      The activity context for UI thread operations (e.g., callbacks).
     * @param dishId        The ID of the dish to delete.
     * @param queryCallBack Callback to be invoked on success or error.
     *                      OnSuccess will be called with null (Void).
     *                      OnError will be called with the encountered exception.
     */
    public void deleteDishById(Activity activity, long dishId, QueryCallBack<Void> queryCallBack) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Attempting to delete dish with ID: " + dishId);
                int deletedRows = dishDao.deleteDishById(dishId);
                Log.d(TAG, "Rows deleted by DAO: " + deletedRows);

                if (deletedRows > 0) {
                    activity.runOnUiThread(() -> queryCallBack.onSuccess(null));
                } else {
                    activity.runOnUiThread(() -> queryCallBack.onError(new Exception("Dish with ID " + dishId + " not found or already deleted.")));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting dish with ID: " + dishId, e);
                activity.runOnUiThread(() -> queryCallBack.onError(e));
            }
        }).start();
    }

    /**
     * Updates an existing dish in the database, including its nutrition and product list.
     * This operation is performed asynchronously.
     *
     * @param activity      The activity context for UI thread operations (e.g., callbacks).
     * @param dishToUpdate  The {@link com.mydishes.mydishes.Models.Dish} object with updated information.
     *                      Ensure the ID of the dish is correctly set.
     * @param queryCallBack Callback to be invoked on success or error.
     *                      OnSuccess will be called with null (Void).
     *                      OnError will be called with the encountered exception.
     */
    public void updateDish(Activity activity, com.mydishes.mydishes.Models.Dish dishToUpdate, QueryCallBack<Void> queryCallBack) {
        new Thread(() -> {
            try {
                executorService.submit(() -> { // Submit a Callable for background execution
                    long dishId = dishToUpdate.getId();
                    if (dishId == 0) {
                        throw new Exception("Dish ID is invalid, cannot update.");
                    }

                    // Confirm dish exists and get its current nutritionId for potential cleanup
                    DishWithProductsAndNutrition existingDishContainer = dishDao.getDishWithProductsAndNutrition(dishId);
                    if (existingDishContainer == null || existingDishContainer.dish == null) {
                        throw new Exception("Dish with ID " + dishId + " not found for update.");
                    }
                    // long oldDishNutritionId = existingDishContainer.dish.nutritionId; // Store if needed for explicit deletion

                    // 1. Handle Dish's own Nutrition
                    long finalDishNutritionFk;
                    if (dishToUpdate.getNutrition() != null) {
                        com.mydishes.mydishes.Models.Nutrition appDishNutrition = dishToUpdate.getNutrition();
                        Nutrition dbDishNutritionEntity = adaptNutrition(appDishNutrition);

                        if (appDishNutrition.getId() != 0) { // Model has ID, update existing Nutrition
                            dbDishNutritionEntity.id = appDishNutrition.getId();
                            nutritionDao.updateNutrition(dbDishNutritionEntity); // Предполагается, что этот метод существует в NutritionDao
                            finalDishNutritionFk = appDishNutrition.getId();
                        } else { // Model has no ID, insert new Nutrition
                            finalDishNutritionFk = nutritionDao.insertNutrition(dbDishNutritionEntity);
                        }
                    } else { // Nutrition is null in the model, so unlink dish's nutrition
                        finalDishNutritionFk = 0; // 0 или другое значение, означающее отсутствие связи
                        // Опционально: если oldDishNutritionId != 0, можно удалить "осиротевшую" запись Nutrition
                        // nutritionDao.deleteNutritionById(oldDishNutritionId); // Потребует этого метода в NutritionDao
                    }

                    // 2. Update Dish entity (name, photoUri, and link to its nutrition)
                    Dish dishEntityForUpdate = new Dish(dishToUpdate.getName(), dishToUpdate.getPhotoUri(), finalDishNutritionFk);
                    dishEntityForUpdate.id = dishId; // Важно для Room/JPA для определения обновляемой записи
                    dishDao.updateDish(dishEntityForUpdate);

                    // 3. Handle Products and their Nutrition
                    // Сначала удаляем все существующие связи продуктов для этого блюда
                    dishDao.deleteDishProductCrossRefsByDishId(dishId); // Предполагается, что этот метод существует в DishDao

                    if (dishToUpdate.getProducts() != null && !dishToUpdate.getProducts().isEmpty()) {
                        List<DishProductCrossRef> newCrossRefs = new ArrayList<>();
                        for (com.mydishes.mydishes.Models.Product appProduct : dishToUpdate.getProducts()) {
                            long productNutritionFk = 0; // По умолчанию продукт без пищевой ценности

                            // 3a. Handle Product's Nutrition
                            if (appProduct.getNutrition() != null) {
                                com.mydishes.mydishes.Models.Nutrition appProductNutrition = appProduct.getNutrition();
                                Nutrition dbProductNutritionEntity = adaptNutrition(appProductNutrition);

                                if (appProductNutrition.getId() != 0) { // Модель пищевой ценности продукта имеет ID
                                    dbProductNutritionEntity.id = appProductNutrition.getId();
                                    nutritionDao.updateNutrition(dbProductNutritionEntity); // Предполагается в NutritionDao
                                    productNutritionFk = appProductNutrition.getId();
                                } else { // Модель пищевой ценности продукта не имеет ID, вставляем как новую
                                    productNutritionFk = nutritionDao.insertNutrition(dbProductNutritionEntity);
                                }
                            }

                            // 3b. Handle Product entity
                            Product dbProductEntity = adaptProduct(appProduct, productNutritionFk);
                            long savedOrUpdatedProductId;

                            if (appProduct.getId() != 0) { // Модель продукта имеет ID, обновляем существующий продукт
                                dbProductEntity.id = appProduct.getId();
                                productDao.updateProduct(dbProductEntity); // Предполагается в ProductDao
                                savedOrUpdatedProductId = appProduct.getId();
                            } else { // Модель продукта не имеет ID, вставляем как новый продукт
                                savedOrUpdatedProductId = productDao.insertProduct(dbProductEntity);
                            }

                            // 3c. Create cross-reference
                            newCrossRefs.add(new DishProductCrossRef(dishId, savedOrUpdatedProductId));
                        }

                        // 3d. Insert all new cross-references
                        if (!newCrossRefs.isEmpty()) {
                            dishDao.insertDishProductCrossRefs(newCrossRefs);
                        }
                    }
                    // Если dishToUpdate.getProducts() был null или пуст, все старые связи удалены,
                    // и новые не добавлены, фактически очищая список продуктов для блюда.

                    return null; // Значение для Callable<Void>
                }).get(); // Блокирует поток new Thread() до завершения submit или выброса исключения

                activity.runOnUiThread(() -> queryCallBack.onSuccess(null));
            } catch (Exception e) {
                Log.e(TAG, "Error updating dish with ID: " + (dishToUpdate != null ? dishToUpdate.getId() : "null"), e);
                activity.runOnUiThread(() -> queryCallBack.onError(e));
            }
        }).start();
    }

    public interface QueryCallBack<T> {
        void onSuccess(T result);

        void onError(Exception e);
    }
}
