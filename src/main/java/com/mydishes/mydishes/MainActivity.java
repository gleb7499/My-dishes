package com.mydishes.mydishes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
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
    private RecyclerView recyclerView; // Сделаем recyclerView полем класса для доступа из onSwiped

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
        recyclerView = findViewById(R.id.add_products_recycler); // Присваиваем полю класса
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // обработка нажатия на элемент списка
        adapter = new DishesAdapter(dish -> {
            if (dish == null) return;

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
            View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(
                    R.layout.bottom_sheet_dish_details,
                    findViewById(R.id.bottom_sheet_dish_details_container),
                    false
            );
            bottomSheetDialog.setContentView(bottomSheetView);

            ImageView dishImage = bottomSheetView.findViewById(R.id.bottom_sheet_dish_image);
            TextView dishNameTextView = bottomSheetView.findViewById(R.id.bottom_sheet_dish_name); // Изменено имя переменной во избежание конфликта
            RecyclerView ingredientsRecyclerView = bottomSheetView.findViewById(R.id.bottom_sheet_ingredients_recycler_view);
            TextView ingredientsTitleTextView = bottomSheetView.findViewById(R.id.bottom_sheet_ingredients_title);

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
                IngredientsAdapter ingredientsAdapter = new IngredientsAdapter();
                ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                ingredientsRecyclerView.setAdapter(ingredientsAdapter);
                ingredientsAdapter.submitList(new ArrayList<>(ingredients));
            } else {
                ingredientsTitleTextView.setVisibility(View.GONE);
                ingredientsRecyclerView.setVisibility(View.GONE);
            }

            bottomSheetDialog.show();
        });

        // установка адаптера
        recyclerView.setAdapter(adapter);

        // обработка свайпа по элементу списка
        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // кнопка добавления нового блюда
        ImageButton addButton = findViewById(R.id.addButton);
        ViewUtils.applyInsets(linearLayout, true, false, false, false);
        addButton.setOnClickListener(this::startAddActivity);

        // объект для работы с БД
        dataRepository = DataRepository.getInstance(getApplication());
    }

    // получаем экземпляр ItemTouchHelper для обработки свайпа и его действий
    @NonNull
    private ItemTouchHelper getItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;  // Для перетаскивания (drag&drop). Если не нужно — возвращаем false
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION || position >= adapter.getCurrentList().size()) {
                    return; // Проверка валидности позиции
                }
                final Dish dish = adapter.getCurrentList().get(position);

                AlertDialog alertDialog = new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle(R.string.delete)
                        .setMessage(getString(R.string.delete_dish_confirmation, dish.getName())) // Используем форматированную строку для подтверждения
                        .setNegativeButton(R.string.cancel, (d, w) -> {
                            d.dismiss();
                            adapter.notifyItemChanged(position); // Возвращаем элемент на место
                        })
                        .setPositiveButton(R.string.ok, (dialog, which) -> dataRepository.deleteDishById(MainActivity.this, dish.getId(), new DataRepository.QueryCallBack<>() {
                            @Override
                            public void onSuccess(Void result) {
                                loadDishesFromDb(); // Обновляем список после успешного удаления
                            }

                            @Override
                            public void onError(Exception e) {
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_deleting_dish) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                adapter.notifyItemChanged(position); // Возвращаем элемент на место в случае ошибки
                            }
                        })).create();
                alertDialog.show();
            }
        };

        // привязываем свайп к RecyclerView один раз здесь
        return new ItemTouchHelper(simpleCallback);
    }

    private void loadDishesFromDb() {
        dataRepository.getAllDishesWithDetails(this, new DataRepository.QueryCallBack<>() {
            @Override
            public void onSuccess(List<Dish> result) {
                if (adapter != null) { // Добавлена проверка на null для adapter
                    adapter.submitList(result);
                }
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_loading_dishes) + " " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void startAddActivity(View v) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }
}
