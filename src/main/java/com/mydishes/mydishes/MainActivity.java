package com.mydishes.mydishes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.Adapters.DishesAdapter;
import com.mydishes.mydishes.Database.repository.DataRepository;
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Utils.DishDetailsBottomSheet;
import com.mydishes.mydishes.Utils.ViewUtils;

import java.util.Collections;
import java.util.List;

// Главное окно приложения (отображение списка созданных блюд)
public class MainActivity extends AppCompatActivity {

    private DishesAdapter adapter;
    private DataRepository dataRepository;
    private TextView noDishesTextView;

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

        LinearLayout linear_layout = findViewById(R.id.linear_layout);
        ViewUtils.applyInsets(linear_layout, true, false, false, true);
        RecyclerView recyclerView = findViewById(R.id.add_products_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // обработка нажатия на элемент списка
        adapter = new DishesAdapter(dish -> {
            if (dish == null) return;

            // копия текущего элемента, на который нажали, чтобы предотвратить мутабельность
            Dish newDish = dish.clone();

            // нижний лист для отображения информации о блюде
            DishDetailsBottomSheet bottomSheet = new DishDetailsBottomSheet(newDish);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        // Слушатель для результата от DishDetailsBottomSheet
        getSupportFragmentManager().setFragmentResultListener(DishDetailsBottomSheet.REQUEST_KEY, this, (requestKey, bundle) -> {
            boolean dishUpdated = bundle.getBoolean(DishDetailsBottomSheet.BUNDLE_KEY_DISH_UPDATED);
            if (dishUpdated) {
                // обновляем список после изменения данных блюда
                loadDishesFromDb();
            }
        });

        // установка адаптера
        recyclerView.setAdapter(adapter);

        // обработка свайпа по элементу списка
        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // кнопка добавления нового блюда
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(this::startAddActivity);

        // элемент для сообщения об отсутствии блюд в БД
        noDishesTextView = findViewById(R.id.emptyStateText);

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
                        .setMessage(getString(R.string.delete_confirmation, dish.getName())) // Используем форматированную строку для подтверждения
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                            adapter.notifyItemChanged(position); // Возвращаем элемент на место
                        })
                        .setPositiveButton(R.string.ok, (dialog, which) -> dataRepository.deleteDishById(MainActivity.this, dish.getId(), new DataRepository.QueryCallBack<>() {
                            @Override
                            public void onSuccess(Void result) {
                                loadDishesFromDb(); // Обновляем список после успешного удаления
                            }

                            @Override
                            public void onError(Exception e) {
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_deleting) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                adapter.notifyItemChanged(position); // Возвращаем элемент на место в случае ошибки
                            }
                        })).create();
                alertDialog.show();
            }
        };

        // привязываем свайп к RecyclerView
        return new ItemTouchHelper(simpleCallback);
    }


    // загрузка всей информации о всех блюдах из БД
    private void loadDishesFromDb() {
        dataRepository.getAllDishesWithDetails(this, new DataRepository.QueryCallBack<>() {
            @Override
            public void onSuccess(List<Dish> result) {
                if (adapter != null) { // Добавлена проверка на null для adapter
                    Collections.reverse(result); // Разворачиваем список (новые сверху)
                    adapter.submitList(result);
                    noDishesTextView.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_loading_dishes) + " " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }


    // активити для добавления нового блюда
    private void startAddActivity(View v) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }
}
