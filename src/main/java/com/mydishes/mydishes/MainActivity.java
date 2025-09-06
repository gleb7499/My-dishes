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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.adapters.DishesAdapter;
import com.mydishes.mydishes.database.repository.DataRepository;
import com.mydishes.mydishes.models.Dish;
import com.mydishes.mydishes.utils.DishDetailsBottomSheet;
import com.mydishes.mydishes.utils.ViewUtils;

import java.util.Collections;
import java.util.List;

/**
 * Главная активность приложения.
 * Отображает список сохраненных блюд, позволяет добавлять новые блюда и удалять существующие.
 * Использует {@link DishesAdapter} для отображения списка в {@link RecyclerView},
 * {@link DataRepository} для взаимодействия с базой данных и {@link DishDetailsBottomSheet}
 * для отображения и редактирования деталей блюда.
 */
public class MainActivity extends AppCompatActivity {

    // Адаптер для RecyclerView, отображающего список блюд
    private DishesAdapter adapter;
    // Репозиторий для доступа к данным блюд
    private DataRepository dataRepository;
    // TextView для отображения сообщения об отсутствии блюд
    private TextView noDishesTextView;

    /**
     * Вызывается, когда активность становится видимой пользователю.
     * В этом методе происходит загрузка (или перезагрузка) списка блюд из базы данных.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Загрузка блюд из базы данных при возобновлении активности
        loadDishesFromDb();
    }

    /**
     * Вызывается при создании активности.
     * Инициализирует UI компоненты, настраивает RecyclerView, слушатели нажатий и свайпов,
     * а также получает экземпляр DataRepository.
     *
     * @param savedInstanceState Если активность пересоздается после предыдущего уничтожения,
     *                           этот Bundle содержит данные, которые она в последний раз предоставила
     *                           в {@link #onSaveInstanceState}. В противном случае это null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Включение отображения "от края до края" для современного вида
        EdgeToEdge.enable(this);
        // Установка прозрачного цвета для навигационной панели и строки состояния
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        // Установка макета для этой активности
        setContentView(R.layout.activity_main);

        // Получение корневого LinearLayout и применение системных отступов
        LinearLayout linearLayout = findViewById(R.id.linear_layout);
        ViewUtils.applyInsets(linearLayout, true, false, false, true);

        // Инициализация RecyclerView
        RecyclerView recyclerView = findViewById(R.id.add_products_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Инициализация адаптера и установка слушателя нажатия на элемент списка
        adapter = new DishesAdapter(dish -> {
            // Проверка, что объект блюда не null
            if (dish == null) return;

            // Создание копии объекта Dish для передачи в BottomSheet, чтобы избежать изменения оригинала
            Dish dishCopy = Dish.createDish(dish);

            // Создание и отображение DishDetailsBottomSheet для просмотра/редактирования деталей блюда
            DishDetailsBottomSheet bottomSheet = new DishDetailsBottomSheet(dishCopy);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        // Установка слушателя для получения результата от DishDetailsBottomSheet
        // (например, если блюдо было обновлено)
        getSupportFragmentManager().setFragmentResultListener(DishDetailsBottomSheet.REQUEST_KEY, this, (requestKey, bundle) -> {
            boolean dishUpdated = bundle.getBoolean(DishDetailsBottomSheet.BUNDLE_KEY_DISH_UPDATED);
            // Если блюдо было обновлено, перезагружаем список
            if (dishUpdated) {
                loadDishesFromDb();
            }
        });

        // Установка адаптера для RecyclerView
        recyclerView.setAdapter(adapter);

        // Настройка обработки свайпов для удаления элементов
        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Настройка кнопки добавления нового блюда
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(this::startAddActivity); // Вызов метода startAddActivity при нажатии

        // Инициализация TextView для сообщения об отсутствии блюд
        noDishesTextView = findViewById(R.id.emptyStateText);

        // Получение экземпляра DataRepository для работы с базой данных
        dataRepository = DataRepository.getInstance(getApplication());
    }


    /**
     * Создает и настраивает {@link ItemTouchHelper.SimpleCallback} для обработки свайпов по элементам RecyclerView.
     * Позволяет удалять блюда свайпом влево или вправо с диалогом подтверждения.
     *
     * @return Настроенный экземпляр {@link ItemTouchHelper}.
     */
    @NonNull
    private ItemTouchHelper getItemTouchHelper() {
        // Создание SimpleCallback для обработки свайпов
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // Перемещение элементов не поддерживается, возвращаем false
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Получение позиции элемента, по которому был сделан свайп
                final int position = viewHolder.getAdapterPosition();
                // Проверка валидности позиции
                if (position == RecyclerView.NO_POSITION || position >= adapter.getCurrentList().size()) {
                    return;
                }
                // Получение объекта Dish, соответствующего свайпнутому элементу
                final Dish dish = adapter.getCurrentList().get(position);

                // Создание диалога подтверждения удаления
                AlertDialog alertDialog = new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle(R.string.delete) // Заголовок "Удалить"
                        .setMessage(getString(R.string.delete_confirmation, dish.getName())) // Сообщение "Вы уверены, что хотите удалить [Название блюда]?"
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            // При отмене возвращаем элемент на место (отменяем визуальное удаление)
                            dialog.dismiss();
                            adapter.notifyItemChanged(position);
                        })
                        .setPositiveButton(R.string.ok, (dialog, which) ->
                                // При подтверждении удаляем блюдо из базы данных
                                dataRepository.deleteDishById(MainActivity.this, dish.getId(), new DataRepository.QueryCallBack<>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        // При успешном удалении перезагружаем список блюд
                                        loadDishesFromDb();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // При ошибке удаления показываем Snackbar и возвращаем элемент на место
                                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_deleting) + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
                                        adapter.notifyItemChanged(position);
                                    }
                                })
                        ).create();
                // Отображение диалога
                alertDialog.show();
            }
        };
        // Создание и возврат ItemTouchHelper с настроенным SimpleCallback
        return new ItemTouchHelper(simpleCallback);
    }


    /**
     * Загружает список всех блюд с их полной детализацией из базы данных.
     * Использует {@link DataRepository#getAllDishesWithDetails}.
     * В случае успеха обновляет адаптер RecyclerView, отображая блюда (новые сверху).
     * В случае ошибки отображает Snackbar с сообщением.
     * Также управляет видимостью {@link #noDishesTextView} в зависимости от того, пуст ли список.
     */
    private void loadDishesFromDb() {
        // Асинхронный запрос к репозиторию для получения всех блюд с деталями
        dataRepository.getAllDishesWithDetails(this, new DataRepository.QueryCallBack<>() {
            @Override
            public void onSuccess(List<Dish> result) {
                // Проверка, что адаптер еще существует (активность/фрагмент не уничтожены)
                if (adapter != null) {
                    // Разворачиваем список, чтобы новые блюда были сверху
                    Collections.reverse(result);
                    // Передаем обновленный список в адаптер
                    adapter.submitList(result);
                    // Управляем видимостью текстового поля "Нет блюд":
                    // показываем, если список пуст, иначе скрываем
                    noDishesTextView.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                // При ошибке загрузки показываем Snackbar с сообщением
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_loading_dishes) + " " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }


    /**
     * Запускает {@link AddActivity} для добавления нового блюда.
     * Вызывается при нажатии на кнопку добавления.
     *
     * @param v View, инициировавшая вызов (кнопка добавления).
     */
    private void startAddActivity(View v) {
        // Создание Intent для запуска AddActivity
        Intent intent = new Intent(this, AddActivity.class);
        // Запуск активности
        startActivity(intent);
    }
}
