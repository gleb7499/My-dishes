package com.mydishes.mydishes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.mydishes.mydishes.Utils.ViewUtils;

// Главное окно приложения (отображение списка созданных блюд)
public class MainActivity extends AppCompatActivity {
    // Создание активити
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // настройка активити
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_main);

        LinearLayout linearLayout = findViewById(R.id.linear_layout);
        RecyclerView recyclerView = findViewById(R.id.add_products_recycler);
        ImageButton imageButton = findViewById(R.id.addButton);

        ViewUtils.applyInsets(linearLayout, true, false, false, false);

        imageButton.setOnClickListener(this::startAddActivity);

    }

    private void startAddActivity(View v) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }
}