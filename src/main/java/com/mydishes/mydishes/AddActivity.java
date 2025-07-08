package com.mydishes.mydishes;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.Adapters.RecyclerViewDishesAdapter;
import com.mydishes.mydishes.Models.ProductsManager;
import com.mydishes.mydishes.Parser.EdostavkaParser;
import com.mydishes.mydishes.utils.ViewUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddActivity extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
    }

    private final Handler handler = new Handler();
    private Runnable searchRunnable;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private TextView textViewNothing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_add);

        SearchBar searchBar = findViewById(R.id.searchBar);
        SearchView searchView = findViewById(R.id.searchView);
        progressBar = findViewById(R.id.progressBar);
        textViewNothing = findViewById(R.id.textViewNothing);
        recyclerView = findViewById(R.id.recyclerView);

        searchView.setupWithSearchBar(searchBar);

        ViewUtils.applyInsets(findViewById(R.id.searchLayout), true, false, false, false);

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.length() > 1) {
                    progressBar.setVisibility(View.VISIBLE);
                    textViewNothing.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
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
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    public void runSearch(String query) {
        executor.submit(() -> {
            try {
                EdostavkaParser.find(query); // фоновый парсинг
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Snackbar.make(findViewById(android.R.id.content), "Ошибка! " + e, Snackbar.LENGTH_LONG).show();
                });
                return; // выходим, чтобы не показывать "Найдено: ..."
            }

            runOnUiThread(() -> {
                // Обновляем адаптер списка
                recyclerView.setAdapter(new RecyclerViewDishesAdapter(this));
                progressBar.setVisibility(View.INVISIBLE);
                if (ProductsManager.isEmpty()) {
                    textViewNothing.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        });
    }
}
