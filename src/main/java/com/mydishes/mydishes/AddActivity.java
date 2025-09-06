package com.mydishes.mydishes;

import static com.mydishes.mydishes.utils.ViewUtils.applyInsets;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.adapters.ProductFindAdapter;
import com.mydishes.mydishes.database.repository.DataRepository;
import com.mydishes.mydishes.models.Dish;
import com.mydishes.mydishes.models.Nutrition;
import com.mydishes.mydishes.models.Product;
import com.mydishes.mydishes.models.ProductsSelectedManager;
import com.mydishes.mydishes.parser.EdostavkaParser;
import com.mydishes.mydishes.parser.Parser;
import com.mydishes.mydishes.parser.ParsingStateListener;
import com.mydishes.mydishes.parser.ProductParseCallback;
import com.mydishes.mydishes.utils.DialogUtils;
import com.mydishes.mydishes.utils.TextWatcherUtils;
import com.mydishes.mydishes.utils.ViewAddedBottomSheet;

import java.util.List;

/**
 * Активность для добавления нового блюда.
 * Позволяет пользователю искать продукты, выбирать их, указывать название блюда и сохранять его в базу данных.
 * Использует {@link EdostavkaParser} для поиска продуктов и {@link DataRepository} для взаимодействия с БД.
 */
public class AddActivity extends AppCompatActivity {

    // Handler для отложенного выполнения поисковых запросов
    private final Handler handler = new Handler(Looper.getMainLooper());
    // Экземпляр парсера для получения данных о продуктах
    private final Parser parser = new EdostavkaParser();
    // Runnable для поискового запроса, позволяет отменять предыдущие запросы
    private Runnable searchRunnable;
    // Индикатор загрузки, отображается во время поиска продуктов
    private ProgressBar progressBar;
    // Текстовое поле, отображается, если ничего не найдено по запросу
    private TextView textViewNothing;
    // RecyclerView для отображения списка найденных продуктов
    private RecyclerView addProductsRecycler;
    // Адаптер для RecyclerView, управляющий отображением продуктов
    private ProductFindAdapter productFindAdapter;
    // Плавающая кнопка для отображения списка выбранных продуктов и начала процесса добавления блюда
    private FloatingActionButton productListButton;
    // Репозиторий для взаимодействия с базой данных
    private DataRepository dataRepository;
    private static final String TAG = "AddActivity"; // Тег для логов
    // URI выбранного изображения
    private Uri selectedImageUri;

    /**
     * Вызывается при создании активности.
     * Инициализирует UI компоненты, настраивает слушатели и RecyclerView.
     *
     * @param savedInstanceState Если активность пересоздается после предыдущего уничтожения,
     *                           этот Bundle содержит данные, которые она в последний раз предоставила
     *                           в {@link #onSaveInstanceState}. В противном случае это null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Включение отображения "от края до края"
        EdgeToEdge.enable(this);
        // Установка макета для этой активности
        setContentView(R.layout.activity_add);

        // Получение экземпляра DataRepository для работы с базой данных
        dataRepository = DataRepository.getInstance(this.getApplication());

        initRecyclerView();
        initSearchComponents();
        initProductListButton();
        ActivityResultLauncher<Intent> imagePickerLauncher = initImagePickerLauncher();
        initFragmentResultListener(imagePickerLauncher);
    }

    /**
     * Инициализирует RecyclerView для отображения найденных продуктов.
     * Устанавливает LayoutManager и Adapter для RecyclerView.
     * Adapter {@link ProductFindAdapter} получает слушателя {@link ParsingStateListener}
     * для управления доступностью кнопки productListButton во время парсинга.
     */
    private void initRecyclerView() {
        // Инициализация RecyclerView для отображения найденных продуктов
        addProductsRecycler = findViewById(R.id.add_products_recycler);
        addProductsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Инициализация адаптера для RecyclerView
        // Передаем слушателя состояния парсинга для управления доступностью кнопки productListButton
        productFindAdapter = new ProductFindAdapter(this, new ParsingStateListener() {
            @Override
            public void onParsingStarted() {
                // Отключаем кнопку, пока идет парсинг
                if (productListButton != null) {
                    productListButton.setEnabled(false);
                }
            }

            @Override
            public void onParsingFinished() {
                // Включаем кнопку после завершения парсинга
                if (productListButton != null) {
                    productListButton.setEnabled(true);
                }
            }
        });
        addProductsRecycler.setAdapter(productFindAdapter);
    }

    /**
     * Инициализирует компоненты пользовательского интерфейса, связанные с поиском.
     * Настраивает {@link SearchBar} и {@link SearchView}.
     * Применяет отступы к контейнеру поиска для корректного отображения под системными элементами.
     * Инициализирует {@link ProgressBar} для индикации загрузки и {@link TextView} для сообщения "ничего не найдено".
     * Устанавливает слушатель {@link TextWatcherUtils#addSimpleTextWatcher} на поле ввода {@link SearchView}
     * для запуска поиска с задержкой после ввода текста.
     */
    private void initSearchComponents() {
        // Инициализация компонентов поиска: SearchBar, SearchView, ProgressBar, TextViewNothing
        SearchBar searchBar = findViewById(R.id.searchBar);
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setupWithSearchBar(searchBar);
        // Применение отступов для корректного отображения под системными элементами
        applyInsets(findViewById(R.id.searchLayout), true, false, false, false);
        progressBar = findViewById(R.id.progressBar);
        textViewNothing = findViewById(R.id.textViewNothing);

        // Установка слушателя на изменение текста в поле поиска SearchView
        TextWatcherUtils.addSimpleTextWatcher(searchView.getEditText(), s -> {
            String query = s.trim(); // Получаем текст запроса, удаляя пробелы по краям

            // Отображаем ProgressBar и скрываем предыдущие результаты, если запрос достаточно длинный
            if (query.length() > 1) {
                progressBar.setVisibility(View.VISIBLE);
                textViewNothing.setVisibility(View.INVISIBLE);
                addProductsRecycler.setVisibility(View.INVISIBLE);
            }

            // Отменяем предыдущий запланированный поиск, если он есть
            if (searchRunnable != null) {
                handler.removeCallbacks(searchRunnable);
            }

            // Создаем новый Runnable для выполнения поиска
            searchRunnable = () -> {
                // Выполняем поиск только если длина запроса больше 1 символа (для оптимизации)
                if (query.length() > 1) {
                    runSearch(query);
                }
            };

            // Запускаем поиск с задержкой в 500 мс после последнего ввода символа
            handler.postDelayed(searchRunnable, 500);
        });
    }

    /**
     * Инициализирует плавающую кнопку (FloatingActionButton) для просмотра списка выбранных продуктов.
     * Применяет отступы для корректного отображения кнопки.
     * Устанавливает слушатель нажатия:
     * - Если список выбранных продуктов пуст ({@link ProductsSelectedManager#size()} == 0),
     * показывает {@link Snackbar} с сообщением {@link R.string#no_products_selected}.
     * - В противном случае, создает и отображает {@link ViewAddedBottomSheet} со списком выбранных продуктов.
     */
    private void initProductListButton() {
        // Настройка плавающей кнопки для просмотра выбранных продуктов
        productListButton = findViewById(R.id.productListButton);
        // Применение отступов для корректного отображения кнопки
        applyInsets(productListButton, false, true, false, true);
        productListButton.setOnClickListener(v -> {
            // Если нет выбранных продуктов, показываем Snackbar
            if (ProductsSelectedManager.size() == 0) {
                Snackbar.make(productListButton, R.string.no_products_selected, BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }
            // Создаем и показываем BottomSheet со списком выбранных продуктов
            ViewAddedBottomSheet viewAddedBottomSheet = new ViewAddedBottomSheet();
            viewAddedBottomSheet.show(getSupportFragmentManager(), viewAddedBottomSheet.getTag());
        });
    }

    /**
     * Инициализирует и возвращает {@link ActivityResultLauncher} для выбора изображения из галереи.
     * Регистрирует контракт {@link ActivityResultContracts.StartActivityForResult} для получения результата
     * от активности выбора изображения.
     * В колбэке результата:
     * - Если изображение успешно выбрано (resultCode == RESULT_OK и данные присутствуют):
     * - Получает URI изображения.
     * - Пытается получить постоянное разрешение на чтение URI с помощью {@link #getContentResolver()#takePersistableUriPermission}.
     * - В случае успеха или контролируемого сбоя (с показом Snackbar), сохраняет URI.
     * - Отображает диалог {@link DialogUtils#showEditDishNameDialog} для ввода имени блюда, передавая URI изображения.
     * - Если изображение не выбрано или выбор отменен, отображает диалог для ввода имени блюда без изображения.
     *
     * @return сконфигурированный {@link ActivityResultLauncher} для выбора изображения.
     */
    private ActivityResultLauncher<Intent> initImagePickerLauncher() {
        // Инициализация ActivityResultLauncher для выбора изображения
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                        Log.d(TAG, "Successfully took persistable URI permission for: " + imageUri);
                        selectedImageUri = imageUri;
                    } catch (SecurityException e) {
                        Log.e(TAG, "Failed to take persistable URI permission for " + imageUri, e);
                        // Опционально: можно скопировать файл в локальное хранилище как fallback
                        // selectedImageUri = copyFileToAppStorage(imageUri); // Пример
                        // Пока просто используем оригинальный URI, который может работать или не работать
                        selectedImageUri = imageUri;
                        Snackbar.make(findViewById(android.R.id.content), R.string.error_persisting_image_permission, BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
                DialogUtils.showEditDishNameDialog(AddActivity.this, null, newDishName -> saveDishWithImage(newDishName, selectedImageUri));
            } else {
                DialogUtils.showEditDishNameDialog(AddActivity.this, null, newDishName -> saveDishWithImage(newDishName, null)); // Если изображение не выбрано или отменено
            }
        });
    }

    /**
     * Инициализирует слушателя результата фрагмента {@link ViewAddedBottomSheet}.
     * Регистрирует {@link androidx.fragment.app.FragmentManager#setFragmentResultListener}
     * для получения данных от {@link ViewAddedBottomSheet} по ключу {@link ViewAddedBottomSheet#REQUEST_KEY}.
     * Когда результат получен (пользователь подтвердил добавление блюда в BottomSheet):
     * - Проверяет флаг подтверждения {@link ViewAddedBottomSheet#BUNDLE_KEY_CONFIRMED}.
     * - Если подтверждено, показывает {@link MaterialAlertDialogBuilder} с предложением установить фото на блюдо:
     * - При нажатии "Да": запускает выбор изображения через {@link Intent#ACTION_OPEN_DOCUMENT},
     * используя переданный {@code imagePickerLauncher}.
     * - При нажатии "Нет": пропускает выбор фото и сразу показывает диалог ввода имени блюда
     * {@link DialogUtils#showEditDishNameDialog}.
     *
     * @param imagePickerLauncher {@link ActivityResultLauncher} для запуска выбора изображения.
     */
    private void initFragmentResultListener(ActivityResultLauncher<Intent> imagePickerLauncher) {
        // Регистрация слушателя для получения результата от ViewAddedBottomSheet (когда пользователь подтверждает добавление блюда)
        getSupportFragmentManager().setFragmentResultListener(ViewAddedBottomSheet.REQUEST_KEY, this, (requestKey, bundle) -> {
            boolean confirmed = bundle.getBoolean(ViewAddedBottomSheet.BUNDLE_KEY_CONFIRMED);
            if (confirmed) {
                new MaterialAlertDialogBuilder(AddActivity.this)
                        .setTitle(R.string.set_dish_photo_title) // "Установить фото на блюдо?"
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            // Пользователь нажал "Да", запускаем выбор изображения
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("image/*");
                            // Добавляем флаги для получения разрешений на чтение URI
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            imagePickerLauncher.launch(intent);
                        }) // Пользователь нажал "Нет", пропускаем выбор фото и сразу показываем диалог ввода имени
                        .setNegativeButton(R.string.no, (dialog, which) -> DialogUtils.showEditDishNameDialog(AddActivity.this, null, newDishName -> saveDishWithImage(newDishName, null)))
                        .show();
            }
        });
    }

    private void saveDishWithImage(String dishName, Uri imageUri) {
        // Рассчитываем общую пищевую ценность выбранных продуктов
        Nutrition nutrition = Product.calculate(ProductsSelectedManager.getAll());
        String photoUriString = (imageUri != null) ? imageUri.toString() : "";

        // Создаем объект Dish с введенным названием, рассчитанной пищевой ценностью и списком продуктов
        Dish dish = new Dish(dishName, photoUriString, nutrition, ProductsSelectedManager.getAll());

        // Сохраняем блюдо в базу данных через DataRepository
        dataRepository.insertDishWithDetails(AddActivity.this, dish, new DataRepository.QueryCallBack<>() {
            @Override
            public void onSuccess(Long result) {
                // При успешном сохранении, присваиваем блюду полученный ID
                dish.setId(result);
                // Очищаем список выбранных продуктов
                ProductsSelectedManager.clear();
                // Завершаем текущую активность (AddActivity)
                AddActivity.this.finish();
            }

            @Override
            public void onError(Exception e) {
                // При ошибке сохранения, показываем Snackbar с сообщением об ошибке
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_loading_dishes) + ": " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }


    /**
     * Выполняет асинхронный поиск продуктов по заданному запросу.
     * Использует {@link Parser#findProductsAsync} для получения данных.
     * Обновляет UI в зависимости от результата поиска (отображает список продуктов, сообщение "ничего не найдено" или ошибку).
     *
     * @param query Строка поискового запроса.
     */
    public void runSearch(String query) {
        // Вызов асинхронного метода поиска у парсера
        parser.findProductsAsync(query, new ProductParseCallback<>() {
            @Override
            public void onParsingStarted() {
                // Метод обратного вызова при начале парсинга (можно добавить логику, если необходимо)
                // Например, дополнительно управлять состоянием UI
            }

            @Override
            public void onSuccess(List<Product> products) {
                // Метод обратного вызова при успешном завершении парсинга
                // Скрываем ProgressBar
                progressBar.setVisibility(View.INVISIBLE);
                // Если список продуктов пуст, показываем сообщение "ничего не найдено"
                if (products.isEmpty()) {
                    textViewNothing.setVisibility(View.VISIBLE);
                    addProductsRecycler.setVisibility(View.INVISIBLE);
                } else {
                    // Иначе, обновляем адаптер RecyclerView новыми данными и показываем список
                    productFindAdapter.submitList(products);
                    addProductsRecycler.setVisibility(View.VISIBLE);
                    textViewNothing.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                // Метод обратного вызова при ошибке парсинга
                // Скрываем ProgressBar
                progressBar.setVisibility(View.INVISIBLE);
                // Показываем Snackbar с сообщением об ошибке
                Snackbar.make(productListButton, getString(R.string.error_parser_text) + ": " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
            }

            @Override
            public void onParsingFinished() {
                // Метод обратного вызова по завершению парсинга (успех или ошибка)
                // Можно добавить логику, если необходимо, например, обновить доступность кнопок
            }
        });
    }

    /**
     * Вызывается при уничтожении активности.
     * Очищает колбэки для Handler и проверяет, есть ли несохраненные выбранные продукты.
     * Если есть, показывает диалог с предупреждением.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Удаляем все запланированные Runnable из Handler, чтобы избежать утечек или нежелательного выполнения
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
        // Проверяем, остались ли выбранные продукты, которые не были сохранены
        if (ProductsSelectedManager.size() > 0) {
            // Если да, показываем диалог с предупреждением
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(AddActivity.this)
                    .setTitle(R.string.added_ingredients_willnt_be_saved) // "Добавленные ингредиенты не будут сохранены!"
                    .setPositiveButton(R.string.ok, (dialog, which) -> ProductsSelectedManager.clear()) // Кнопка "ОК" очищает список
                    .setNegativeButton(R.string.cancel, null) // Кнопка "Отмена" ничего не делает
                    .create();
            alertDialog.show();
        }
    }
}
