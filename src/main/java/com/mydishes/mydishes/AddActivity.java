package com.mydishes.mydishes;

import static com.mydishes.mydishes.Utils.ViewUtils.applyInsets;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.material.textfield.TextInputLayout;
import com.mydishes.mydishes.Adapters.ProductFindListAdapter;
import com.mydishes.mydishes.Database.repository.DataRepository;
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Models.ProductsSelectedManager;
import com.mydishes.mydishes.Parser.EdostavkaParser;
import com.mydishes.mydishes.Parser.Parser;
import com.mydishes.mydishes.Parser.ProductFindCallback;
import com.mydishes.mydishes.Utils.TextWatcherUtils;

import java.util.List;

// Класс экрана добавления блюда (поис продуктов, составления списка продуктов, создание блюда)
public class AddActivity extends AppCompatActivity {

    // Метод уничтожения активити
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Удаляем очередь запроса к сайту, если она осталась в памяти
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
    }

    private final Handler handler = new Handler(); // выполнение отложенного запроса к сайту
    private Runnable searchRunnable; // инструкции отложенного запроса к сайту
    private ProgressBar progressBar; // загрузка результата запроса
    private TextView textViewNothing; // отображение надписи о том, что ничего не найдено
    private RecyclerView addProductsRecycler; // отображение результата поиска (список продуктов)
    private ProductFindListAdapter productFindListAdapter; // адаптер для RecyclerView
    private FloatingActionButton productListButton; // отображение списка выбранных продуктов
    private final Parser parser = new EdostavkaParser(); // объект класса парсера
    private DataRepository dataRepository; // объект класса репозитория


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
        productFindListAdapter = new ProductFindListAdapter(this);
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
            // Создаем нижний лист со списком выбранных продуктов и кнопкой "Добавить"
            ViewAddedFragment bottomSheet = new ViewAddedFragment();

            /* Устанавливаем действие при нажатии кнопки "Добавить" в bottomSheet:
               1) Диалог для ввода названия блюда
               2) Обработать Nutrition
               3) Объект Dish с name, Nutrition, списком продуктов
               4) Добавить объект Dish в централизованный менеджер
            */
            bottomSheet.setOnConfirmListener(() -> {
                // надутие и настройка XML
                View dialogViewName = LayoutInflater.from(this).inflate(R.layout.dialog_input_name, null);
                TextInputLayout inputFieldName = dialogViewName.findViewById(R.id.inputName);
                EditText editTextName = inputFieldName.getEditText();
                if (editTextName == null) return;

                // Диалог для ввода наименования блюда
                AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.enter_products_name)
                        .setView(dialogViewName)
                        .setPositiveButton(R.string.ok, null) // временно null! (см. -> .setOnShowListener)
                        .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                        .create();

                // Отключаем отображение старых ошибок при вводе имени
                TextWatcherUtils.addSimpleTextWatcher(editTextName, s -> {
                    if (inputFieldName.getError() != null) inputFieldName.setError(null);
                });

                // Обработка введенного значения
                dialog.setOnShowListener(d -> dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                    // Обработка введенного значения
                    String dishName = editTextName.getText().toString().trim();

                    // Обработка граничных случаев
                    if (dishName.isEmpty() || dishName.length() > 50) {
                        inputFieldName.setError(getString(R.string.error_value));
                        return;
                    }

                    // Получили итоговые значения КБЖУ для блюда
                    Nutrition nutrition = Nutrition.calculate(ProductsSelectedManager.getAll());

                    // Создали блюдо
                    Dish dish = new Dish(dishName, "", nutrition, ProductsSelectedManager.getAll()); // <- photoUri пока пусто (не реализовано)!

                    dataRepository.insertDishWithDetails(AddActivity.this, dish, new DataRepository.QueryCallBack<>() {
                        @Override
                        public void onSuccess(Long result) { // возврат - ID блюда в БД
                            dish.setId(result);
                            ProductsSelectedManager.clear(); // очистили список
                            // Вышли в родительское активити
                            dialog.dismiss();
                            bottomSheet.dismiss();
                            AddActivity.this.finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            dialog.dismiss(); // Закрываем диалог в случае ошибки
                            bottomSheet.dismiss();
                            Snackbar.make(findViewById(android.R.id.content), "Ошибка! " + e, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }));

                // показываем диалог
                dialog.show();
            });

            // показываем нижний лист со списком добавленных блюд
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

    }

    // запуск асинхронного поиска списка продуктов по запросу query с обработкой результата
    public void runSearch(String query) {
        parser.findProductsAsync(this, query, new ProductFindCallback() {
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
                Snackbar.make(productListButton, "Ошибка! " + e, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
