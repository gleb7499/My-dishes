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

public class DataRepository {
    private static final String TAG = "DataRepository";
    private final DishDao dishDao;
    private final ProductDao productDao;
    private final NutritionDao nutritionDao;
    private final ExecutorService executorService;

    public DataRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        dishDao = db.dishDao();
        productDao = db.productDao();
        nutritionDao = db.nutritionDao();
        executorService = Executors.newSingleThreadExecutor(); // Для простоты, можно использовать более сложный пул
    }

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

    // Адаптер для вашего класса Nutrition
    private Nutrition adaptNutrition(com.mydishes.mydishes.Models.Nutrition originalNutrition) {
        if (originalNutrition == null) return null;
        return new Nutrition(
                originalNutrition.getCalories(),
                originalNutrition.getProtein(),
                originalNutrition.getFat(),
                originalNutrition.getCarb()
        );
    }

    // Адаптер для вашего класса Product (без КБЖУ)
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

    public Future<Long> insertDishWithDetails(com.mydishes.mydishes.Models.Dish originalDish) {
        return executorService.submit(() -> {
            // 1. Сохранить КБЖУ блюда
            Nutrition dishNutritionEntity = adaptNutrition(originalDish.getNutrition());
            long dishNutritionId = nutritionDao.insertNutrition(dishNutritionEntity);

            // 2. Сохранить блюдо
            Dish dishEntity = new Dish(originalDish.getName(), originalDish.getPhotoUri(), dishNutritionId);
            long dishId = dishDao.insertDish(dishEntity);

            // 3. Сохранить продукты и их КБЖУ, и создать связи
            if (originalDish.getProducts() != null) {
                List<DishProductCrossRef> crossRefs = new ArrayList<>();
                for (com.mydishes.mydishes.Models.Product originalProduct : originalDish.getProducts()) {
                    Nutrition productNutritionEntity = adaptNutrition(originalProduct.getNutrition());
                    long productNutritionId = nutritionDao.insertNutrition(productNutritionEntity);

                    Product productEntity = adaptProduct(originalProduct, productNutritionId);
                    long productId = productDao.insertProduct(productEntity);

                    crossRefs.add(new DishProductCrossRef(dishId, productId));
                }
                if (!crossRefs.isEmpty()) {
                    dishDao.insertDishProductCrossRefs(crossRefs);
                }
            }
            return dishId;
        });
    }

    // Получить Dish со всей структурой, адаптированный к вашим исходным моделям
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

    // Получить все блюда (простая версия, для списков)
    public Future<List<com.mydishes.mydishes.Models.Dish>> getAllDishesSimple() {
        return executorService.submit(() -> {
            List<Dish> dbDishes = dishDao.getAllDishesSimple();
            List<com.mydishes.mydishes.Models.Dish> resultDishes = new ArrayList<>();
            for (Dish dbDish : dbDishes) {
                com.mydishes.mydishes.Models.Dish appDish = new com.mydishes.mydishes.Models.Dish();
                appDish.setName(dbDish.name);
                appDish.setPhotoUri(dbDish.photoUri);
                // ID можно добавить, если он есть в вашей модели com.mydishes.mydishes.Models.Dish
                // и если он нужен в списке.
                // Nutrition и Products здесь не загружаются для экономии.
                resultDishes.add(appDish);
            }
            return resultDishes;
        });
    }

    // Дополнительно: получение всех блюд с полной информацией
    // Может быть ресурсоемко, если блюд много
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

    // Пример использования (вызывать из ViewModel или Activity/Fragment через AsyncTask/LiveData/Coroutines)
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
