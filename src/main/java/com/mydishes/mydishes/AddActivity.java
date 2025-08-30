package com.mydishes.mydishes;

import static com.mydishes.mydishes.Utils.ViewUtils.applyInsets;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.Adapters.ProductFindAdapter;
import com.mydishes.mydishes.Database.repository.DataRepository;
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Models.ProductsSelectedManager;
import com.mydishes.mydishes.Parser.EdostavkaParser;
import com.mydishes.mydishes.Parser.Parser;
import com.mydishes.mydishes.Parser.ParsingStateListener;
import com.mydishes.mydishes.Parser.ProductParseCallback;
import com.mydishes.mydishes.Utils.DialogUtils;
import com.mydishes.mydishes.Utils.TextWatcherUtils;
import com.mydishes.mydishes.Utils.ViewAddedBottomSheet;

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
        // Установка прозрачного цвета для навигационной панели и строки состояния
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        // Установка макета для этой активности
        setContentView(R.layout.activity_add);

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

        // Инициализация компонентов поиска: SearchBar, SearchView, ProgressBar, TextViewNothing
        SearchBar searchBar = findViewById(R.id.searchBar);
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setupWithSearchBar(searchBar);
        // Применение отступов для корректного отображения под системными элементами
        applyInsets(findViewById(R.id.searchLayout), true, false, false, false);
        progressBar = findViewById(R.id.progressBar);
        textViewNothing = findViewById(R.id.textViewNothing);

        // Получение экземпляра DataRepository для работы с базой данных
        dataRepository = DataRepository.getInstance(this.getApplication());

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

        // Настройка плавающей кнопки для просмотра выбранных продуктов
        productListButton = findViewById(R.id.productListButton);
        // Применение отступов для корректного отображения кнопки
        applyInsets(productListButton, false, true, false, true);
        productListButton.setOnClickListener(v -> {
            // Если нет выбранных продуктов, показываем Snackbar
            if (ProductsSelectedManager.size() == 0) {
                Snackbar.make(productListButton, R.string.no_products_selected, Snackbar.LENGTH_SHORT).show();
                return;
            }
            // Создаем и показываем BottomSheet со списком выбранных продуктов
            ViewAddedBottomSheet viewAddedBottomSheet = new ViewAddedBottomSheet();
            viewAddedBottomSheet.show(getSupportFragmentManager(), viewAddedBottomSheet.getTag());
        });

        // Регистрация слушателя для получения результата от ViewAddedBottomSheet (когда пользователь подтверждает добавление блюда)
        getSupportFragmentManager().setFragmentResultListener(ViewAddedBottomSheet.REQUEST_KEY, this, (requestKey, bundle) -> {
            boolean confirmed = bundle.getBoolean(ViewAddedBottomSheet.BUNDLE_KEY_CONFIRMED);
            // Если пользователь подтвердил действие в BottomSheet
            if (confirmed) {
                // Показываем диалог для ввода названия нового блюда
                DialogUtils.showEditDishNameDialog(AddActivity.this, null, newDishName -> {
                    // Рассчитываем общую пищевую ценность выбранных продуктов
                    Nutrition nutrition = Nutrition.calculate(ProductsSelectedManager.getAll());
                    // TODO: Реализовать логику выбора или генерации URI для фото блюда
                    String photoUri = ""; // Временная заглушка

                    // Создаем объект Dish с введенным названием, рассчитанной пищевой ценностью и списком продуктов
                    Dish dish = new Dish(newDishName, photoUri, nutrition, ProductsSelectedManager.getAll());

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
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_loading_dishes) + ": " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                });
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
                Snackbar.make(productListButton, getString(R.string.error_parser_text) + ": " + e.getMessage(), Snackbar.LENGTH_LONG).show();
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
