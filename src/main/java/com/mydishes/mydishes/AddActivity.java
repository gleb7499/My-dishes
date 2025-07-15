package com.mydishes.mydishes;

import static com.mydishes.mydishes.utils.ViewUtils.applyInsets;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.Adapters.RecyclerViewDishesAdapter;
import com.mydishes.mydishes.Models.DishProductsBuilder;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Parser.EdostavkaParser;
import com.mydishes.mydishes.Parser.Parser;
import com.mydishes.mydishes.Parser.ProductFindCallback;
import com.mydishes.mydishes.utils.TextWatcherUtils;

import java.util.ArrayList;
import java.util.List;

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
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textViewNothing;
    private RecyclerViewDishesAdapter adapter;
    private final Parser parser = new EdostavkaParser();
    private final List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_add);

        progressBar = findViewById(R.id.progressBar);
        textViewNothing = findViewById(R.id.textViewNothing);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new RecyclerViewDishesAdapter(this, products);
        recyclerView.setAdapter(adapter);

        SearchBar searchBar = findViewById(R.id.searchBar);
        SearchView searchView = findViewById(R.id.searchView);

        searchView.setupWithSearchBar(searchBar);

        applyInsets(findViewById(R.id.searchLayout), true, false, false, false);

        TextWatcherUtils.addSimpleTextWatcher(searchView.getEditText(), s -> {
            String query = s.trim();

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
        });

        FloatingActionButton productListButton = findViewById(R.id.productListButton);

        applyInsets(productListButton, false, true, false, true);

        productListButton.setOnClickListener(v -> {
            for (int i = 0; i < DishProductsBuilder.size(); ++i) {
                Log.d("DishProductsBuilder", DishProductsBuilder.get(i).toString());
            }
        });
    }

    public void runSearch(String query) {
        parser.findProductsAsync(this, query, new ProductFindCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                // Обновляем адаптер списка
                progressBar.setVisibility(View.INVISIBLE);
                if (products.isEmpty()) {
                    textViewNothing.setVisibility(View.VISIBLE);
                } else {
                    adapter.submitList(products);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Snackbar.make(findViewById(android.R.id.content), "Ошибка! " + e, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
