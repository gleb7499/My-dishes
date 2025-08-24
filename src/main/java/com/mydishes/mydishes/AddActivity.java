package com.mydishes.mydishes;

import static com.mydishes.mydishes.Utils.ViewUtils.applyInsets;

import android.os.Bundle;
import android.os.Handler;
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
import com.mydishes.mydishes.Adapters.ProductFindListAdapter;
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

// Класс экрана добавления блюда (поис продуктов, составления списка продуктов, создание блюда)
public class AddActivity extends AppCompatActivity {

    // private ViewAddedBottomSheet bottomSheet; // Удалено, так как больше не нужно как поле класса

    private final Handler handler = new Handler(); // выполнение отложенного запроса к сайту
    private Runnable searchRunnable; // инструкции отложенного запроса к сайту
    private ProgressBar progressBar; // загрузка результата запроса
    private TextView textViewNothing; // отображение надписи о том, что ничего не найдено
    private RecyclerView addProductsRecycler; // отображение результата поиска (список продуктов)
    private ProductFindListAdapter productFindListAdapter; // адаптер для RecyclerView
    private FloatingActionButton productListButton; // отображение списка выбранных продуктов
    private final Parser parser = new EdostavkaParser(); // объект класса парсера
    private DataRepository dataRepository; // объект класса репозитория

    // Метод уничтожения активити
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Удаляем очередь запроса к сайту, если она осталась в памяти
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
        if (ProductsSelectedManager.size() > 0) {
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(AddActivity.this)
                    .setTitle(R.string.added_ingredients_willnt_be_saved)
                    .setPositiveButton(R.string.ok, (dialog, which) -> ProductsSelectedManager.clear())
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            alertDialog.show();
        }
    }

    // создали активити
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Настройка отображения
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_add);

        // настраиваем RecyclerView
        addProductsRecycler = findViewById(R.id.add_products_recycler);
        addProductsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        // отключаем кнопку отображения элементов списка во время парсинга
        productFindListAdapter = new ProductFindListAdapter(this, new ParsingStateListener() {
            @Override
            public void onParsingStarted() {
                if (productListButton != null) {
                    productListButton.setEnabled(false);
                }
            }

            @Override
            public void onParsingFinished() {
                if (productListButton != null) {
                    productListButton.setEnabled(true);
                }
            }
        });
        addProductsRecycler.setAdapter(productFindListAdapter);

        // настраиваем поисковую строку, ProgressBar и textViewNothing
        SearchBar searchBar = findViewById(R.id.searchBar);
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setupWithSearchBar(searchBar);
        applyInsets(findViewById(R.id.searchLayout), true, false, false, false);
        progressBar = findViewById(R.id.progressBar);
        textViewNothing = findViewById(R.id.textViewNothing);

        // получаем объект для управления БД
        dataRepository = DataRepository.getInstance(this.getApplication());

        // установка слушателя на изменение текста в строке поиска
        TextWatcherUtils.addSimpleTextWatcher(searchView.getEditText(), s -> {
            String query = s.trim();

            // проверка длины запроса
            if (query.length() > 1) {
                progressBar.setVisibility(View.VISIBLE);
                textViewNothing.setVisibility(View.INVISIBLE);
                addProductsRecycler.setVisibility(View.INVISIBLE);
            }

            if (searchRunnable != null) {
                handler.removeCallbacks(searchRunnable); // Отменяем прошлую попытку
            }

            searchRunnable = () -> {
                if (query.length() > 1) { // Не парсим по 1 букве
                    runSearch(query);
                }
            };

            handler.postDelayed(searchRunnable, 500); // задержка после последнего ввода
        });

        // настройка кнопки просмотра выбранных продуктов
        productListButton = findViewById(R.id.productListButton);
        applyInsets(productListButton, false, true, false, true);
        productListButton.setOnClickListener(v -> {
            if (ProductsSelectedManager.size() == 0) {
                Snackbar.make(productListButton, R.string.no_products_selected, Snackbar.LENGTH_SHORT).show();
                return;
            }
            // Создаем нижний лист со списком выбранных продуктов и кнопкой "Добавить"
            ViewAddedBottomSheet viewAddedBottomSheet = new ViewAddedBottomSheet();
            // Убрали setOnConfirmListener
            // показываем нижний лист со списком добавленных блюд
            viewAddedBottomSheet.show(getSupportFragmentManager(), viewAddedBottomSheet.getTag());
        });

        // Слушатель для результата от ViewAddedBottomSheet
        getSupportFragmentManager().setFragmentResultListener(ViewAddedBottomSheet.REQUEST_KEY, this, (requestKey, bundle) -> {
            boolean confirmed = bundle.getBoolean(ViewAddedBottomSheet.BUNDLE_KEY_CONFIRMED);
            if (confirmed) {
                DialogUtils.showEditDishNameDialog(AddActivity.this, null, newDishName -> {
                    Nutrition nutrition = Nutrition.calculate(ProductsSelectedManager.getAll());
                    String photoUri = ""; // Заглушка, если нет логики выбора фото

                    Dish dish = new Dish(newDishName, photoUri, nutrition, ProductsSelectedManager.getAll());

                    dataRepository.insertDishWithDetails(AddActivity.this, dish, new DataRepository.QueryCallBack<>() {
                        @Override
                        public void onSuccess(Long result) { // возврат - ID блюда в БД
                            dish.setId(result);
                            ProductsSelectedManager.clear(); // очистили список
                            // ViewAddedBottomSheet сам себя закрывает после отправки результата
                            AddActivity.this.finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            // ViewAddedBottomSheet сам себя закрывает
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_loading_dishes) + ": " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                });
            }
        });
    }

    // запуск асинхронного поиска списка продуктов по запросу query с обработкой результата
    public void runSearch(String query) {
        parser.findProductsAsync(query, new ProductParseCallback<>() {
            @Override
            public void onParsingStarted() {
                // TODO: Implement onParsingStarted if needed
            }

            @Override
            public void onSuccess(List<Product> products) { // парсинг успешен
                // Обновляем адаптер списка
                progressBar.setVisibility(View.INVISIBLE);
                if (products.isEmpty()) {
                    textViewNothing.setVisibility(View.VISIBLE);
                } else {
                    productFindListAdapter.submitList(products);
                    addProductsRecycler.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) { // ошибка парсинга!
                progressBar.setVisibility(View.INVISIBLE);
                Snackbar.make(productListButton, getString(R.string.error_parser_text) + ": " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onParsingFinished() {
                // TODO: Implement onParsingFinished if needed
            }
        });
    }
}
