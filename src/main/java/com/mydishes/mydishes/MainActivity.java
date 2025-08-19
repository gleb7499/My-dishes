package com.mydishes.mydishes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mydishes.mydishes.Adapters.DishesAdapter;
import com.mydishes.mydishes.Adapters.IngredientsAdapter;
import com.mydishes.mydishes.Database.repository.DataRepository;
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

// Главное окно приложения (отображение списка созданных блюд)
public class MainActivity extends AppCompatActivity {

    private DishesAdapter adapter;
    private DataRepository dataRepository;

    @Override
    protected void onResume() {
        super.onResume();
        loadDishesFromDb();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Настройка активити
        EdgeToEdge.enable(this);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_main);

        LinearLayout linearLayout = findViewById(R.id.linear_layout);
        RecyclerView recyclerView = findViewById(R.id.add_products_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new DishesAdapter(new DishesAdapter.OnDishActionClickListener() {
            @Override
            public void onDishClick(Dish dish) {
                if (dish == null) return;

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(
                        R.layout.bottom_sheet_dish_details,
                        findViewById(R.id.bottom_sheet_dish_details_container),
                        false
                );
                bottomSheetDialog.setContentView(bottomSheetView);

                ImageView dishImage = bottomSheetView.findViewById(R.id.bottom_sheet_dish_image);
                TextView dishName = bottomSheetView.findViewById(R.id.bottom_sheet_dish_name);
                RecyclerView ingredientsRecyclerView = bottomSheetView.findViewById(R.id.bottom_sheet_ingredients_recycler_view);
                TextView ingredientsTitleTextView = bottomSheetView.findViewById(R.id.bottom_sheet_ingredients_title);

                dishName.setText(dish.getName());

                Glide.with(MainActivity.this)
                        .load(dish.getPhotoUri())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .into(dishImage);

                List<Product> ingredients = dish.getProducts();
                if (ingredients != null && !ingredients.isEmpty()) {
                    ingredientsTitleTextView.setVisibility(View.VISIBLE);
                    ingredientsRecyclerView.setVisibility(View.VISIBLE);
                    IngredientsAdapter ingredientsAdapter = new IngredientsAdapter();
                    ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    ingredientsRecyclerView.setAdapter(ingredientsAdapter);
                    ingredientsAdapter.submitList(new ArrayList<>(ingredients));
                } else {
                    ingredientsTitleTextView.setVisibility(View.GONE);
                    ingredientsRecyclerView.setVisibility(View.GONE);
                }

                bottomSheetDialog.show();
            }

            @Override
            public void onEditClick(Dish dish) {
                Log.d("DishesAdapter", "Edit clicked for dish: " + dish.getName());
            }

            @Override
            public void onDeleteClick(Dish dish) {
                Log.d("DishesAdapter", "Delete clicked for dish: " + dish.getName());
            }
        });

        recyclerView.setAdapter(adapter);

        ImageButton imageButton = findViewById(R.id.addButton);
        ViewUtils.applyInsets(linearLayout, true, false, false, false);
        imageButton.setOnClickListener(this::startAddActivity);

        // --- Новый код для загрузки данных из базы ---
        dataRepository = DataRepository.getInstance(getApplication());

        loadDishesFromDb();

    }

    private void loadDishesFromDb() {
        new Thread(() -> {
            try {
                List<Dish> dishes = dataRepository.getAllDishesWithDetails().get();
                runOnUiThread(() -> adapter.submitList(dishes));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startAddActivity(View v) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }
}
