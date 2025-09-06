package com.mydishes.mydishes.database.repository;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mydishes.mydishes.database.AppDatabase;
import com.mydishes.mydishes.database.dao.DishDao;
import com.mydishes.mydishes.database.dao.NutritionDao;
import com.mydishes.mydishes.database.dao.ProductDao;
import com.mydishes.mydishes.database.model.Dish;
import com.mydishes.mydishes.database.model.DishProductCrossRef;
import com.mydishes.mydishes.database.model.Nutrition;
import com.mydishes.mydishes.database.model.Product;
import com.mydishes.mydishes.database.model.relations.DishWithProductsAndNutrition;
import com.mydishes.mydishes.database.model.relations.ProductWithNutrition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Репозиторий для управления данными, связанными с блюдами, продуктами и их пищевой ценностью в базе данных.
 * Предоставляет методы для добавления, получения, обновления и удаления данных,
 * инкапсулируя логику взаимодействия с DAO (Data Access Objects).
 * Использует ExecutorService для выполнения операций с базой данных в фоновом потоке.
 */
public class DataRepository {
    private static final String TAG = "DataRepository";
    private static volatile DataRepository instance;
    private final DishDao dishDao;
    private final ProductDao productDao;
    private final NutritionDao nutritionDao;
    private final ExecutorService executorService;

    /**
     * Приватный конструктор для реализации паттерна Singleton.
     * Инициализирует DAO и ExecutorService.
     *
     * @param context Контекст приложения, необходимый для получения экземпляра базы данных.
     */
    private DataRepository(Context context) {
        // Получение экземпляра базы данных
        AppDatabase db = AppDatabase.getDatabase(context);
        // Инициализация DAO
        dishDao = db.dishDao();
        productDao = db.productDao();
        nutritionDao = db.nutritionDao();
        // Создание однопоточного исполнителя для асинхронных операций
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Возвращает единственный экземпляр DataRepository (Singleton).
     * Если экземпляр еще не создан, инициализирует его.
     *
     * @param context Контекст приложения.
     * @return Экземпляр DataRepository.
     */
    public static DataRepository getInstance(Context context) {
        // Двойная проверка для потокобезопасной инициализации
        if (instance == null) {
            synchronized (DataRepository.class) {
                if (instance == null) {
                    instance = new DataRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * Конвертирует объект {@link ProductWithNutrition} (связь продукта и его пищевой ценности из БД)
     * в объект модели {@link com.mydishes.mydishes.models.Product}, используемый в приложении.
     *
     * @param pwn Объект {@link ProductWithNutrition}, содержащий данные продукта и его пищевой ценности из БД.
     * @return Адаптированный объект {@link com.mydishes.mydishes.models.Product} для использования в UI или бизнес-логике.
     */
    @NonNull
    private static com.mydishes.mydishes.models.Product getProduct(@NonNull ProductWithNutrition pwn) {
        // Создание нового объекта модели Product
        com.mydishes.mydishes.models.Product appProduct = new com.mydishes.mydishes.models.Product();
        // Копирование основных данных продукта
        appProduct.setId(pwn.product.id);
        appProduct.setName(pwn.product.name);
        appProduct.setProductURL(pwn.product.productURL);
        appProduct.setImageURL(pwn.product.imageURL);
        appProduct.setMass(pwn.product.mass);

        // Проверка наличия данных о пищевой ценности
        if (pwn.nutrition != null) {
            // Создание и заполнение объекта модели Nutrition
            com.mydishes.mydishes.models.Nutrition productNutrition = new com.mydishes.mydishes.models.Nutrition();
            productNutrition.setCalories(pwn.nutrition.calories);
            productNutrition.setProtein(pwn.nutrition.protein);
            productNutrition.setFat(pwn.nutrition.fat);
            productNutrition.setCarb(pwn.nutrition.carb);
            // Установка пищевой ценности для продукта
            appProduct.setNutrition(productNutrition);
        }
        return appProduct;
    }

    /**
     * Адаптирует объект модели {@link com.mydishes.mydishes.models.Nutrition} (используемый в приложении)
     * к сущности {@link Nutrition} (используемой в базе данных).
     *
     * @param originalNutrition Оригинальный объект {@link com.mydishes.mydishes.models.Nutrition} из модели приложения.
     * @return Сущность {@link Nutrition}, готовая для сохранения в базу данных, или null, если originalNutrition равен null.
     */
    private Nutrition adaptNutrition(com.mydishes.mydishes.models.Nutrition originalNutrition) {
        // Проверка на null
        if (originalNutrition == null) return null;
        // Создание и возврат сущности Nutrition для БД
        return new Nutrition(
                originalNutrition.getCalories(),
                originalNutrition.getProtein(),
                originalNutrition.getFat(),
                originalNutrition.getCarb()
        );
    }

    /**
     * Адаптирует объект модели {@link com.mydishes.mydishes.models.Product} (используемый в приложении)
     * к сущности {@link Product} (используемой в базе данных).
     *
     * @param originalProduct Оригинальный объект {@link com.mydishes.mydishes.models.Product} из модели приложения.
     * @param nutritionId     ID связанной пищевой ценности в таблице Nutrition базы данных.
     * @return Сущность {@link Product}, готовая для сохранения в базу данных, или null, если originalProduct равен null.
     */
    private Product adaptProduct(com.mydishes.mydishes.models.Product originalProduct, long nutritionId) {
        // Проверка на null
        if (originalProduct == null) return null;
        // Создание и возврат сущности Product для БД
        return new Product(
                originalProduct.getProductURL(),
                originalProduct.getImageURL(),
                originalProduct.getName(),
                nutritionId, // Связь с пищевой ценностью
                originalProduct.getMass()
        );
    }

    /**
     * Вставляет новое блюдо со всей его детализацией (пищевая ценность блюда, продукты и их пищевая ценность) в базу данных.
     * Операция выполняется асинхронно в фоновом потоке.
     * Результат операции (ID вставленного блюда или ошибка) передается через {@link QueryCallBack}.
     *
     * @param activity       Активность, из которой вызывается метод, для выполнения UI операций в основном потоке.
     * @param originalDish   Объект {@link com.mydishes.mydishes.models.Dish}, содержащий данные нового блюда.
     * @param queryCallBack  Колбэк для получения результата операции.
     */
    public void insertDishWithDetails(Activity activity, com.mydishes.mydishes.models.Dish originalDish, QueryCallBack<Long> queryCallBack) {
        // Создание нового потока для выполнения операции
        new Thread(() -> {
            try {
                // Выполнение вставки в фоновом потоке через ExecutorService и получение Future
                long dishId = executorService.submit(() -> {
                    // Шаг 1: Адаптация и сохранение пищевой ценности самого блюда
                    Nutrition dishNutritionEntity = adaptNutrition(originalDish.getNutrition());
                    long dishNutritionId = 0;
                    if (dishNutritionEntity != null) {
                        dishNutritionId = nutritionDao.insertNutrition(dishNutritionEntity);
                    }

                    // Шаг 2: Адаптация и сохранение основной информации о блюде
                    Dish dishEntity = new Dish(originalDish.getName(), originalDish.getPhotoUri(), dishNutritionId);
                    long savedDishId = dishDao.insertDish(dishEntity);

                    // Шаг 3: Обработка продуктов, если они есть
                    if (originalDish.getProducts() != null) {
                        List<DishProductCrossRef> crossRefs = new ArrayList<>();
                        for (com.mydishes.mydishes.models.Product originalProduct : originalDish.getProducts()) {
                            // Шаг 3.1: Адаптация и сохранение пищевой ценности текущего продукта
                            Nutrition productNutritionEntity = adaptNutrition(originalProduct.getNutrition());
                            long productNutritionId = 0;
                            if (productNutritionEntity != null) {
                                productNutritionId = nutritionDao.insertNutrition(productNutritionEntity);
                            }

                            // Шаг 3.2: Адаптация и сохранение текущего продукта
                            Product productEntity = adaptProduct(originalProduct, productNutritionId);
                            long productId = productDao.insertProduct(productEntity);

                            // Шаг 3.3: Создание записи для таблицы связей "многие-ко-многим"
                            crossRefs.add(new DishProductCrossRef(savedDishId, productId));
                        }
                        // Шаг 3.4: Вставка всех связей в таблицу
                        if (!crossRefs.isEmpty()) {
                            dishDao.insertDishProductCrossRefs(crossRefs);
                        }
                    }
                    return savedDishId; // Возвращение ID сохраненного блюда
                }).get(); // Ожидание завершения операции

                // Передача успешного результата в основной поток
                activity.runOnUiThread(() -> queryCallBack.onSuccess(dishId));
            } catch (Exception e) {
                // Логгирование ошибки и передача ее в основной поток
                Log.e(TAG, "Ошибка при вставке блюда: ", e);
                activity.runOnUiThread(() -> queryCallBack.onError(e));
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Получает блюдо по его идентификатору (ID) вместе со всей связанной информацией:
     * пищевая ценность самого блюда, список продуктов, входящих в блюдо, и пищевая ценность каждого продукта.
     * Адаптирует результат из сущностей БД к модели {@link com.mydishes.mydishes.models.Dish}.
     * Операция выполняется асинхронно.
     *
     * @param dishId ID запрашиваемого блюда.
     * @return {@link Future}, который по завершении будет содержать объект {@link com.mydishes.mydishes.models.Dish}
     *         или null, если блюдо с таким ID не найдено.
     */
    public Future<com.mydishes.mydishes.models.Dish> getDishById(long dishId) {
        // Выполнение запроса в фоновом потоке
        return executorService.submit(() -> {
            // Получение сырых данных из DAO
            DishWithProductsAndNutrition dishDetails = dishDao.getDishWithProductsAndNutrition(dishId);
            // Если блюдо не найдено, вернуть null
            if (dishDetails == null) {
                return null;
            }

            // Создание объекта модели Dish для результата
            com.mydishes.mydishes.models.Dish resultDish = new com.mydishes.mydishes.models.Dish();
            // Копирование основных данных блюда
            resultDish.setId(dishDetails.dish.id); // Установка ID блюда
            resultDish.setName(dishDetails.dish.name);
            resultDish.setPhotoUri(dishDetails.dish.photoUri);

            // Обработка пищевой ценности блюда
            if (dishDetails.dishNutrition != null) {
                com.mydishes.mydishes.models.Nutrition mainNutrition = new com.mydishes.mydishes.models.Nutrition();
                mainNutrition.setId(dishDetails.dishNutrition.id); // Установка ID пищевой ценности
                mainNutrition.setCalories(dishDetails.dishNutrition.calories);
                mainNutrition.setProtein(dishDetails.dishNutrition.protein);
                mainNutrition.setFat(dishDetails.dishNutrition.fat);
                mainNutrition.setCarb(dishDetails.dishNutrition.carb);
                resultDish.setNutrition(mainNutrition);
            }

            // Обработка списка продуктов блюда
            if (dishDetails.products != null && !dishDetails.products.isEmpty()) {
                List<com.mydishes.mydishes.models.Product> resultProducts = new ArrayList<>();
                // Сбор ID всех продуктов для эффективного запроса их пищевой ценности
                List<Long> productIds = dishDetails.products.stream().map(p -> p.id).collect(Collectors.toList());

                // Получение всех продуктов с их КБЖУ одним запросом
                List<ProductWithNutrition> productsWithNutrition = productDao.getProductsWithNutritionByIds(productIds);

                // Создание карты для быстрого доступа к ProductWithNutrition по ID продукта
                Map<Long, ProductWithNutrition> productMap = productsWithNutrition.stream()
                        .collect(Collectors.toMap(pwn -> pwn.product.id, pwn -> pwn));

                // Адаптация каждого продукта из БД в модель приложения
                for (Product dbProduct : dishDetails.products) {
                    ProductWithNutrition pwn = productMap.get(dbProduct.id);
                    if (pwn != null) {
                        com.mydishes.mydishes.models.Product appProduct = getProduct(pwn); // Использование вспомогательного метода
                        resultProducts.add(appProduct);
                    }
                }
                resultDish.setProducts(resultProducts);
            }
            return resultDish; // Возврат полностью собранного объекта Dish
        });
    }

    /**
     * Получает упрощенный список всех блюд (только ID, название, URI фото).
     * Этот метод не загружает продукты или детальную пищевую ценность для каждого блюда,
     * что делает его более эффективным для отображения в списках.
     * Операция выполняется асинхронно.
     *
     * @return {@link Future} со списком объектов {@link com.mydishes.mydishes.models.Dish},
     *         содержащих только основную информацию.
     */
    public Future<List<com.mydishes.mydishes.models.Dish>> getAllDishesSimple() {
        // Выполнение запроса в фоновом потоке
        return executorService.submit(() -> {
            // Получение списка сущностей Dish из DAO
            List<Dish> dbDishes = dishDao.getAllDishesSimple();
            List<com.mydishes.mydishes.models.Dish> resultDishes = new ArrayList<>();
            // Адаптация каждой сущности Dish к модели приложения
            for (Dish dbDish : dbDishes) {
                com.mydishes.mydishes.models.Dish appDish = new com.mydishes.mydishes.models.Dish();
                appDish.setId(dbDish.id);
                appDish.setName(dbDish.name);
                appDish.setPhotoUri(dbDish.photoUri);
                resultDishes.add(appDish);
            }
            return resultDishes; // Возврат списка адаптированных блюд
        });
    }

    /**
     * Получает полный список всех блюд со всей их детализацией (пищевая ценность блюда,
     * список продуктов и их пищевая ценность для каждого продукта).
     * Операция может быть ресурсоемкой при большом количестве блюд.
     * Выполняется асинхронно, результат передается через {@link QueryCallBack}.
     *
     * @param activity      Активность для выполнения UI операций в основном потоке.
     * @param queryCallBack Колбэк для получения результата операции (списка блюд или ошибки).
     */
    public void getAllDishesWithDetails(Activity activity, QueryCallBack<List<com.mydishes.mydishes.models.Dish>> queryCallBack) {
        // Создание нового потока для выполнения операции
        new Thread(() -> {
            try {
                // Получение данных с помощью приватного синхронного метода
                List<com.mydishes.mydishes.models.Dish> dishes = getAllDishesWithDetailsInternal();
                // Передача успешного результата в основной поток
                activity.runOnUiThread(() -> queryCallBack.onSuccess(dishes));
            } catch (Exception e) {
                // Логгирование ошибки и передача ее в основной поток
                Log.e(TAG, "Ошибка при получении всех блюд с деталями: ", e);
                activity.runOnUiThread(() -> queryCallBack.onError(e));
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Внутренний метод для получения полного списка всех блюд с детализацией.
     * Выполняется синхронно в вызывающем потоке (предположительно, фоновом).
     *
     * @return Список объектов {@link com.mydishes.mydishes.models.Dish} со всеми деталями.
     * @throws ExecutionException   Если возникает ошибка во время выполнения задачи в ExecutorService.
     * @throws InterruptedException Если поток был прерван во время ожидания результата.
     */
    private List<com.mydishes.mydishes.models.Dish> getAllDishesWithDetailsInternal() throws ExecutionException, InterruptedException {
        // Выполнение запроса в фоновом потоке через ExecutorService
        return executorService.submit(() -> {
            // Получение всех блюд с продуктами и их пищевой ценностью из DAO
            List<DishWithProductsAndNutrition> allDbDishDetails = dishDao.getAllDishesWithProductsAndNutrition();
            List<com.mydishes.mydishes.models.Dish> resultAppDishes = new ArrayList<>();

            // Получение ID всех продуктов из всех блюд для одного пакетного запроса
            List<Long> allProductIds = allDbDishDetails.stream()
                    .filter(details -> details.products != null)
                    .flatMap(details -> details.products.stream().map(p -> p.id))
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, ProductWithNutrition> productNutritionMap = new HashMap<>();
            if (!allProductIds.isEmpty()) {
                // Получение информации о пищевой ценности для всех уникальных продуктов одним запросом
                List<ProductWithNutrition> productsWithNutritionList = productDao.getProductsWithNutritionByIds(allProductIds);
                // Создание карты для быстрого доступа к ProductWithNutrition по ID продукта
                productNutritionMap = productsWithNutritionList.stream()
                        .collect(Collectors.toMap(pwn -> pwn.product.id, pwn -> pwn));
            }

            // Итерация по каждому блюду из БД и его адаптация
            for (DishWithProductsAndNutrition dishDetails : allDbDishDetails) {
                com.mydishes.mydishes.models.Dish resultDish = new com.mydishes.mydishes.models.Dish();
                resultDish.setId(dishDetails.dish.id);
                resultDish.setName(dishDetails.dish.name);
                resultDish.setPhotoUri(dishDetails.dish.photoUri);

                // Адаптация пищевой ценности самого блюда
                if (dishDetails.dishNutrition != null) {
                    com.mydishes.mydishes.models.Nutrition mainNutrition = new com.mydishes.mydishes.models.Nutrition();
                    mainNutrition.setId(dishDetails.dishNutrition.id);
                    mainNutrition.setCalories(dishDetails.dishNutrition.calories);
                    mainNutrition.setProtein(dishDetails.dishNutrition.protein);
                    mainNutrition.setFat(dishDetails.dishNutrition.fat);
                    mainNutrition.setCarb(dishDetails.dishNutrition.carb);
                    resultDish.setNutrition(mainNutrition);
                }

                // Адаптация продуктов блюда
                if (dishDetails.products != null && !dishDetails.products.isEmpty()) {
                    List<com.mydishes.mydishes.models.Product> resultProducts = new ArrayList<>();
                    for (Product dbProduct : dishDetails.products) {
                        ProductWithNutrition pwn = productNutritionMap.get(dbProduct.id);
                        if (pwn != null) {
                            // Использование вспомогательного метода getProduct для адаптации
                            com.mydishes.mydishes.models.Product appProduct = getProduct(pwn);
                            resultProducts.add(appProduct);
                        }
                    }
                    resultDish.setProducts(resultProducts);
                }
                resultAppDishes.add(resultDish);
            }
            return resultAppDishes; // Возврат списка полностью адаптированных блюд
        }).get(); // Ожидание завершения операции
    }


    /**
     * Удаляет блюдо по его идентификатору (ID) из базы данных.
     * Операция выполняется асинхронно.
     * Благодаря правилам CASCADE DELETE в схеме базы данных, это также приведет к удалению:
     * - Записи Nutrition, связанной с этим блюдом.
     * - Всех записей DishProductCrossRef, связывающих это блюдо с продуктами.
     * Сами сущности Product НЕ будут удалены автоматически этим запросом (если они не связаны только с этим блюдом
     * и настроено каскадное удаление для них при удалении из DishProductCrossRef, что обычно не делается).
     *
     * @param activity      Активность, из которой вызывается метод, для выполнения UI операций.
     * @param dishId        ID блюда, которое необходимо удалить.
     * @param queryCallBack Колбэк для получения результата операции (успех или ошибка).
     *                      В случае успеха onSuccess будет вызван с null (Void).
     *                      В случае ошибки onError будет вызван с исключением.
     */
    public void deleteDishById(Activity activity, long dishId, QueryCallBack<Void> queryCallBack) {
        // Запуск операции в новом потоке
        new Thread(() -> {
            try {
                // Логгирование попытки удаления
                Log.d(TAG, "Попытка удалить блюдо с ID: " + dishId);
                // Выполнение удаления через DAO
                int deletedRows = dishDao.deleteDishById(dishId);
                // Логгирование количества удаленных строк (должно быть 1 при успехе)
                Log.d(TAG, "Строк удалено DAO: " + deletedRows);

                // Проверка результата и вызов соответствующего колбэка в UI потоке
                if (deletedRows > 0) {
                    activity.runOnUiThread(() -> queryCallBack.onSuccess(null));
                } else {
                    // Если ничего не удалено, возможно, блюда с таким ID не существует
                    activity.runOnUiThread(() -> queryCallBack.onError(new Exception("Блюдо с ID " + dishId + " не найдено или уже удалено.")));
                }
            } catch (Exception e) {
                // Логгирование ошибки и передача ее в UI поток
                Log.e(TAG, "Ошибка при удалении блюда с ID: " + dishId, e);
                activity.runOnUiThread(() -> queryCallBack.onError(e));
            }
        }).start();
    }

    /**
     * Обновляет существующее блюдо в базе данных, включая его пищевую ценность и список продуктов.
     * Операция выполняется асинхронно.
     *
     * @param activity      Активность для выполнения UI операций (колбэков).
     * @param dishToUpdate  Объект {@link com.mydishes.mydishes.models.Dish} с обновленной информацией.
     *                      Убедитесь, что ID блюда установлен корректно.
     * @param queryCallBack Колбэк для получения результата операции (успех или ошибка).
     *                      В случае успеха onSuccess будет вызван с null (Void).
     */
    public void updateDish(Activity activity, com.mydishes.mydishes.models.Dish dishToUpdate, QueryCallBack<Void> queryCallBack) {
        // Запуск операции в новом потоке
        new Thread(() -> {
            try {
                // Выполнение в фоновом потоке через ExecutorService
                executorService.submit(() -> {
                    long dishId = dishToUpdate.getId();
                    // Проверка валидности ID блюда
                    if (dishId == 0) {
                        throw new IllegalArgumentException("ID блюда недействителен, обновление невозможно.");
                    }

                    // Шаг 0: Получение текущего состояния блюда для определения ID существующих Nutrition
                    DishWithProductsAndNutrition existingDishContainer = dishDao.getDishWithProductsAndNutrition(dishId);
                    if (existingDishContainer == null || existingDishContainer.dish == null) {
                        throw new IllegalArgumentException("Блюдо с ID " + dishId + " не найдено для обновления.");
                    }
                    long oldDishNutritionId = existingDishContainer.dish.nutritionId;

                    // Шаг 1: Обновление или вставка пищевой ценности самого блюда
                    long finalDishNutritionFk;
                    com.mydishes.mydishes.models.Nutrition appDishNutrition = dishToUpdate.getNutrition();
                    if (appDishNutrition != null) {
                        Nutrition dbDishNutritionEntity = adaptNutrition(appDishNutrition);
                        if (appDishNutrition.getId() != 0) { // Если у модели Nutrition есть ID, обновляем существующую
                            dbDishNutritionEntity.id = appDishNutrition.getId();
                            nutritionDao.updateNutrition(dbDishNutritionEntity);
                            finalDishNutritionFk = appDishNutrition.getId();
                        } else { // Иначе вставляем новую Nutrition
                            finalDishNutritionFk = nutritionDao.insertNutrition(dbDishNutritionEntity);
                            // Если старая Nutrition была, а новой присвоен другой ID, старую можно удалить
                            if (oldDishNutritionId != 0 && oldDishNutritionId != finalDishNutritionFk) {
                                // nutritionDao.deleteNutritionById(oldDishNutritionId); // Требует метода в DAO
                            }
                        }
                    } else { // Если пищевая ценность блюда null, разрываем связь
                        finalDishNutritionFk = 0; // Или другое значение, означающее отсутствие связи
                        // Если раньше была пищевая ценность, ее можно удалить
                        if (oldDishNutritionId != 0) {
                            // nutritionDao.deleteNutritionById(oldDishNutritionId); // Требует метода в DAO
                        }
                    }

                    // Шаг 2: Обновление основной информации о блюде
                    Dish dishEntityForUpdate = new Dish(dishToUpdate.getName(), dishToUpdate.getPhotoUri(), finalDishNutritionFk);
                    dishEntityForUpdate.id = dishId; // Установка ID для обновления существующей записи
                    dishDao.updateDish(dishEntityForUpdate);

                    // Шаг 3: Обновление списка продуктов и их пищевой ценности
                    // Сначала удаляем все существующие связи продуктов для этого блюда
                    dishDao.deleteDishProductCrossRefsByDishId(dishId);
                    // Также можно удалить "осиротевшие" продукты и их Nutrition, если они больше не используются.
                    // Это требует более сложной логики (например, подсчет ссылок).

                    if (dishToUpdate.getProducts() != null && !dishToUpdate.getProducts().isEmpty()) {
                        List<DishProductCrossRef> newCrossRefs = new ArrayList<>();
                        for (com.mydishes.mydishes.models.Product appProduct : dishToUpdate.getProducts()) {
                            long productNutritionFk = 0;

                            // Шаг 3а: Обновление/вставка пищевой ценности продукта
                            com.mydishes.mydishes.models.Nutrition appProductNutrition = appProduct.getNutrition();
                            if (appProductNutrition != null) {
                                Nutrition dbProductNutritionEntity = adaptNutrition(appProductNutrition);
                                if (appProductNutrition.getId() != 0) { // Обновляем существующую Nutrition продукта
                                    dbProductNutritionEntity.id = appProductNutrition.getId();
                                    nutritionDao.updateNutrition(dbProductNutritionEntity);
                                    productNutritionFk = appProductNutrition.getId();
                                } else { // Вставляем новую Nutrition продукта
                                    productNutritionFk = nutritionDao.insertNutrition(dbProductNutritionEntity);
                                }
                            }

                            // Шаг 3б: Обновление/вставка продукта
                            Product dbProductEntity = adaptProduct(appProduct, productNutritionFk);
                            long savedOrUpdatedProductId;
                            if (appProduct.getId() != 0) { // Обновляем существующий продукт
                                dbProductEntity.id = appProduct.getId();
                                productDao.updateProduct(dbProductEntity);
                                savedOrUpdatedProductId = appProduct.getId();
                            } else { // Вставляем новый продукт
                                savedOrUpdatedProductId = productDao.insertProduct(dbProductEntity);
                            }

                            // Шаг 3в: Создание новой связи блюдо-продукт
                            newCrossRefs.add(new DishProductCrossRef(dishId, savedOrUpdatedProductId));
                        }

                        // Шаг 3г: Вставка всех новых связей
                        if (!newCrossRefs.isEmpty()) {
                            dishDao.insertDishProductCrossRefs(newCrossRefs);
                        }
                    }
                    // Если dishToUpdate.getProducts() был null или пуст, все старые связи удалены,
                    // и новые не добавлены, что фактически очищает список продуктов для блюда.

                    return null; // Для Callable<Void>
                }).get(); // Ожидание завершения операции в ExecutorService

                // Передача успешного результата в основной поток
                activity.runOnUiThread(() -> queryCallBack.onSuccess(null));
            } catch (Exception e) {
                // Логгирование ошибки и передача ее в основной поток
                Log.e(TAG, "Ошибка при обновлении блюда с ID: " + (dishToUpdate != null ? dishToUpdate.getId() : "null"), e);
                activity.runOnUiThread(() -> queryCallBack.onError(e));
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Интерфейс для колбэков, используемых при выполнении асинхронных операций с базой данных.
     * Позволяет передавать результаты (успех или ошибка) обратно в вызывающий код,
     * который может затем обновить UI или выполнить другие действия.
     *
     * @param <T> Тип данных, ожидаемый в случае успешного выполнения операции.
     */
    public interface QueryCallBack<T> {
        /**
         * Вызывается при успешном завершении операции.
         *
         * @param result Результат операции.
         */
        void onSuccess(T result);

        /**
         * Вызывается в случае возникновения ошибки во время выполнения операции.
         *
         * @param e Исключение, описывающее ошибку.
         */
        void onError(Exception e);
    }
}
