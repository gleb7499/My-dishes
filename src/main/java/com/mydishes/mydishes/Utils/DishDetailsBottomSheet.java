package com.mydishes.mydishes.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.Adapters.IngredientsAdapter;
import com.mydishes.mydishes.Database.repository.DataRepository;
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.databinding.BottomSheetDishDetailsBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Нижний лист (BottomSheet) для отображения и редактирования деталей блюда.
 */
public class DishDetailsBottomSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "dishDetailsRequestKey";
    public static final String BUNDLE_KEY_DISH_UPDATED = "dishUpdated";
    private final Dish dish; // Отображаемое блюдо
    private BottomSheetDishDetailsBinding binding; // ViewBinding для макета
    private IngredientsAdapter adapter; // Адаптер для списка ингредиентов
    private DataRepository dataRepository; // Репозиторий для взаимодействия с базой данных

    /**
     * Конструктор для создания экземпляра DishDetailsBottomSheet.
     *
     * @param dish Блюдо, детали которого будут отображаться.
     */
    public DishDetailsBottomSheet(Dish dish) {
        this.dish = dish;
    }

    /**
     * Вызывается при создании фрагмента.
     * Инициализирует DataRepository.
     *
     * @param savedInstanceState Если не null, этот фрагмент создается заново из предыдущего сохраненного состояния.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализация DataRepository
        dataRepository = DataRepository.getInstance(requireContext().getApplicationContext());
    }

    /**
     * Вызывается при старте фрагмента.
     * Устанавливает режим отображения клавиатуры, чтобы она не перекрывала BottomSheet.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Настройка режима ввода, чтобы клавиатура не перекрывала BottomSheet
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    /**
     * Вызывается для создания и возвращения представления иерархии, связанной с фрагментом.
     * @param inflater Объект LayoutInflater, который можно использовать для раздувания любых представлений во фрагменте.
     * @param container Если не null, это родительское представление, к которому будет присоединено представление фрагмента.
     * @param savedInstanceState Если не null, этот фрагмент создается заново из предыдущего сохраненного состояния.
     * @return Возвращает View для пользовательского интерфейса фрагмента.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация ViewBinding
        binding = BottomSheetDishDetailsBinding.inflate(inflater, container, false);

        // Отображение имени блюда
        binding.bottomSheetDishName.setText(dish.getName());

        // Загрузка и отображение фото блюда с использованием Glide
        Glide.with(requireContext())
                .load(dish.getPhotoUri())
                .placeholder(R.drawable.placeholder) // Заглушка во время загрузки
                .error(R.drawable.error_image) // Изображение при ошибке загрузки
                .into(binding.bottomSheetDishImage);

        // Отображение списка ингредиентов
        List<Product> ingredients = dish.getProducts();
        if (ingredients != null && !ingredients.isEmpty()) {
            // Показ заголовка и RecyclerView для ингредиентов
            binding.bottomSheetDishDetailsIngredientsTitle.setVisibility(View.VISIBLE);
            binding.bottomSheetDishDetailsIngredientsRecycler.setVisibility(View.VISIBLE);
            // Настройка RecyclerView
            binding.bottomSheetDishDetailsIngredientsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new IngredientsAdapter(getParentFragmentManager());
            binding.bottomSheetDishDetailsIngredientsRecycler.setAdapter(adapter);
            adapter.submitList(new ArrayList<>(ingredients)); // Передача копии списка в адаптер
        } else {
            // Скрытие заголовка и RecyclerView, если ингредиентов нет
            binding.bottomSheetDishDetailsIngredientsTitle.setVisibility(View.GONE);
            binding.bottomSheetDishDetailsIngredientsRecycler.setVisibility(View.GONE);
        }

        // Установка слушателя для редактирования имени блюда
        binding.bottomSheetDishName.setOnClickListener(v -> DialogUtils.showEditDishNameDialog(requireContext(), dish.getName(), newName -> {
            // Обновление имени в объекте Dish
            dish.setName(newName);

            // Обновление блюда в базе данных
            dataRepository.updateDish(requireActivity(), dish, new DataRepository.QueryCallBack<>() {
                @Override
                public void onSuccess(Void result) {
                    // Обновление отображаемого имени
                    binding.bottomSheetDishName.setText(newName);
                    // Отправка результата родительскому компоненту об успешном обновлении
                    Bundle bundleResult = new Bundle();
                    bundleResult.putBoolean(BUNDLE_KEY_DISH_UPDATED, true);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, bundleResult);
                }

                @Override
                public void onError(Exception e) {
                    // Отображение сообщения об ошибке
                    Snackbar.make(binding.getRoot(), getString(R.string.error_update_dish) + ": " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        }));

        // Установка слушателя для результатов от IngredientsAdapter (обновление массы продукта)
        getParentFragmentManager().setFragmentResultListener(IngredientsAdapter.REQUEST_KEY, this, (requestKey, bundle) -> {
            // Проверка наличия необходимых данных в Bundle
            if (bundle.containsKey(IngredientsAdapter.BUNDLE_KEY_PRODUCT) && bundle.containsKey(IngredientsAdapter.BUNDLE_KEY_NEW_MASS)) {
                // Извлечение данных из Bundle
                Product productFromBundle = bundle.getParcelable(IngredientsAdapter.BUNDLE_KEY_PRODUCT);
                float newMass = bundle.getFloat(IngredientsAdapter.BUNDLE_KEY_NEW_MASS, 0f);

                List<Product> currentProducts = dish.getProducts();
                // Обновление продукта в списке с использованием утилитного метода
                List<Product> updatedProductList = ProductListUpdater.updateProductInList(currentProducts, productFromBundle, newMass);

                // Обработка результата обновления списка продуктов
                if (updatedProductList != null) {
                    dish.setProducts(updatedProductList); // Обновление списка продуктов в объекте Dish

                    // Пересчет КБЖУ для всего блюда
                    Nutrition newOverallNutrition = Nutrition.calculate(dish.getProducts());
                    dish.setNutrition(newOverallNutrition);

                    // Обновление блюда в базе данных
                    dataRepository.updateDish(requireActivity(), dish, new DataRepository.QueryCallBack<>() {
                        @Override
                        public void onSuccess(Void result) {
                            // Обновление списка в адаптере
                            adapter.submitList(dish.getProducts());
                            // Отправка результата родительскому компоненту об успешном обновлении
                            Bundle bundleResult = new Bundle();
                            bundleResult.putBoolean(BUNDLE_KEY_DISH_UPDATED, true);
                            getParentFragmentManager().setFragmentResult(REQUEST_KEY, bundleResult);
                        }

                        @Override
                        public void onError(Exception e) {
                            // Отображение сообщения об ошибке
                            Snackbar.make(binding.getRoot(), getString(R.string.error_update_dish) + ": " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                } else {
                    // Отображение сообщения об ошибке, если продукт не найден или произошла ошибка при обновлении
                    if (productFromBundle != null) {
                        Snackbar.make(binding.getRoot(), getString(R.string.error_update_product) + " (продукт не найден для обновления): " + productFromBundle.getName(), Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(binding.getRoot(), getString(R.string.error_update_product), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        // Настройка ItemTouchHelper для обработки свайпов по элементам списка ингредиентов
        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(binding.bottomSheetDishDetailsIngredientsRecycler);

        return binding.getRoot();
    }

    /**
     * Создает и настраивает ItemTouchHelper для обработки свайпов влево и вправо по элементам RecyclerView.
     * При свайпе отображается диалог подтверждения удаления продукта. Если это последний продукт в блюде, удаление не производится.
     * @return Настроенный экземпляр ItemTouchHelper.
     */
    @NonNull
    private ItemTouchHelper getItemTouchHelper() {
        // Создание SimpleCallback для ItemTouchHelper
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;  // Перетаскивание не используется
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                int adapterSize = adapter.getCurrentList().size();
                // Проверка валидности позиции элемента
                if (position == RecyclerView.NO_POSITION || position >= adapterSize) {
                    return;
                }
                final Product product = adapter.getCurrentList().get(position);

                // Отображение диалога подтверждения удаления
                AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete)
                        .setMessage(getString(R.string.delete_confirmation, product.getName()))
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            // Отмена удаления, обновление элемента в списке для возврата на место
                            dialog.dismiss();
                            adapter.notifyItemChanged(position);
                        })
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            // Проверка, не является ли удаляемый продукт последним в списке
                            if (adapterSize > 1) {
                                dish.getProducts().remove(position); // Удаление продукта из списка в объекте Dish
                                // Обновление блюда в базе данных
                                dataRepository.updateDish(requireActivity(), dish, new DataRepository.QueryCallBack<>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        // Обновление списка в адаптере
                                        adapter.submitList(new ArrayList<>(dish.getProducts()));
                                        // Отправка результата родительскому компоненту об успешном обновлении
                                        Bundle bundleResult = new Bundle();
                                        bundleResult.putBoolean(BUNDLE_KEY_DISH_UPDATED, true);
                                        getParentFragmentManager().setFragmentResult(REQUEST_KEY, bundleResult);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // Отображение сообщения об ошибке и возврат элемента на место
                                        Snackbar.make(binding.getRoot(), getString(R.string.error_update_dish) + ": " + e, Snackbar.LENGTH_LONG).show();
                                        adapter.notifyItemChanged(position);
                                    }
                                });
                            } else {
                                // Если это последний продукт, отменяем удаление и показываем сообщение
                                adapter.notifyItemChanged(position); // Возвращаем элемент на место
                                Snackbar.make(binding.getRoot(), getString(R.string.error_deleting_product_in_dish), Snackbar.LENGTH_LONG).show();
                            }
                        }).create();
                alertDialog.show();
            }
        };

        // Создание и возврат ItemTouchHelper
        return new ItemTouchHelper(simpleCallback);
    }
}
