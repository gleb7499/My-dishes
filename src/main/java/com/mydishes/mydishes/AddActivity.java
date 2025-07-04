package com.mydishes.mydishes;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.Parser.EdostavkaParser;

public class AddActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private Runnable searchRunnable;

    private void setMargins(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.leftMargin = insets.left;
            mlp.bottomMargin = insets.bottom;
            mlp.rightMargin = insets.right;
            mlp.topMargin = insets.top;
            v.setLayoutParams(mlp);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);

        SearchBar searchBar = findViewById(R.id.searchBar);
        SearchView searchView = findViewById(R.id.searchView);
        setMargins(findViewById(R.id.main));

        searchView.setupWithSearchBar(searchBar);

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable); // Отменяем прошлую попытку
                }

                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (query.length() > 1) { // Не парсим по 1 букве
                        runSearch(query);
                    }
                };

                handler.postDelayed(searchRunnable, 700); // задержка после последнего ввода
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void runSearch(String query) {
        new Thread(() -> {
            EdostavkaParser.find(query); // твой парсинг
            runOnUiThread(() -> {
                Snackbar.make(findViewById(android.R.id.content), "Найдено: " + EdostavkaParser.getDishesList().size(), Snackbar.LENGTH_SHORT).show();

                // Здесь можно сразу обновлять адаптер списка, если есть
            });
        }).start();
    }
}
