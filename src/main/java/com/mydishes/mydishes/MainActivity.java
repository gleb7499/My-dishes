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
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.DishesManager;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

// Главное окно приложения (отображение списка созданных блюд)
public class MainActivity extends AppCompatActivity {

    private DishesAdapter adapter;

    @Override
    protected void onResume() {
        super.onResume();
        adapter.submitList(DishesManager.getAll());
    }

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
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new DishesAdapter(this, new ArrayList<>(), new DishesAdapter.OnDishActionClickListener() {
            // В вашем Activity или Fragment, который реализует DishesAdapter.OnDishActionClickListener
            @Override
            public void onDishClick(Dish dish) {
                if (dish == null) return; // Добавим проверку на null

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_sheet_dish_details,
                        findViewById(R.id.bottom_sheet_dish_details_container), // Используйте ID корневого элемента из XML
                        false);
                bottomSheetDialog.setContentView(bottomSheetView);

                ImageView dishImage = bottomSheetView.findViewById(R.id.bottom_sheet_dish_image);
                TextView dishNameTextView = bottomSheetView.findViewById(R.id.bottom_sheet_dish_name); // Изменено имя переменной
                RecyclerView ingredientsRecyclerView = bottomSheetView.findViewById(R.id.bottom_sheet_ingredients_recycler_view);
                TextView ingredientsTitleTextView = bottomSheetView.findViewById(R.id.bottom_sheet_ingredients_title); // Для скрытия/показа

                dishNameTextView.setText(dish.getName());
                Glide.with(MainActivity.this)
                        .load(dish.getPhotoUri())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .into(dishImage);

                List<Product> ingredients = dish.getProducts();
                if (ingredients != null && !ingredients.isEmpty()) {
                    ingredientsTitleTextView.setVisibility(View.VISIBLE);
                    ingredientsRecyclerView.setVisibility(View.VISIBLE);
                    IngredientsAdapter ingredientsAdapter = new IngredientsAdapter(MainActivity.this, ingredients);
                    ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    ingredientsRecyclerView.setAdapter(ingredientsAdapter);
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

    }

    private void startAddActivity(View v) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }
}