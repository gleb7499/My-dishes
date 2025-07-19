package com.mydishes.mydishes;

import static com.mydishes.mydishes.utils.ViewUtils.applyInsets;

import android.os.Bundle;
import android.os.Handler;
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
import com.mydishes.mydishes.Adapters.ProductFindListAdapter;
import com.mydishes.mydishes.Adapters.ProductSelectedAdapter;
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
    private ProgressBar progressBar;
    private TextView textViewNothing;
    private RecyclerView addProductsRecycler;
    private ProductFindListAdapter productFindListAdapter;
    private RecyclerView selectedProductsRecycler;
    private ProductSelectedAdapter productSelectedAdapter;
    private final Parser parser = new EdostavkaParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_add);

        progressBar = findViewById(R.id.progressBar);
        textViewNothing = findViewById(R.id.textViewNothing);
        addProductsRecycler = findViewById(R.id.add_products_recycler);

        addProductsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        productFindListAdapter = new ProductFindListAdapter(this, new ArrayList<>());
        addProductsRecycler.setAdapter(productFindListAdapter);

        SearchBar searchBar = findViewById(R.id.searchBar);
        SearchView searchView = findViewById(R.id.searchView);

        searchView.setupWithSearchBar(searchBar);

        applyInsets(findViewById(R.id.searchLayout), true, false, false, false);

        TextWatcherUtils.addSimpleTextWatcher(searchView.getEditText(), s -> {
            String query = s.trim();

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


        FloatingActionButton productListButton = findViewById(R.id.productListButton);

        applyInsets(productListButton, false, true, false, true);

        productListButton.setOnClickListener(v -> {
            ViewAddedFragment bottomSheet = new ViewAddedFragment();
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
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
                    productFindListAdapter.submitList(products);
                    addProductsRecycler.setVisibility(View.VISIBLE);
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
