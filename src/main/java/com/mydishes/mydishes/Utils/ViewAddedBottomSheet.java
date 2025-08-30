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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.Adapters.IngredientsAdapter;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Models.ProductsSelectedManager;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.databinding.BottomSheetAddedIngredientsBinding;

import java.util.List;

// Отображение нижнего листа со списком выбранных продуктов для текущего блюда
public class ViewAddedBottomSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "viewAddedBottomSheetRequestKey";
    public static final String BUNDLE_KEY_CONFIRMED = "confirmed";

    private BottomSheetAddedIngredientsBinding binding; // связывание XML макета нижнего листа
    private IngredientsAdapter adapter; // адаптер для списка выбранных продуктов

    public ViewAddedBottomSheet() {
        // пустой конструктор
    }

    /**
     * Вызывается при возобновлении фрагмента.
     * Обновляет список продуктов в адаптере.
     */
    @Override
    public void onResume() {
        super.onResume();
        // перед показом активити обновляем список
        adapter.submitList(ProductsSelectedManager.getAll());
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
     *
     * @param inflater           Объект LayoutInflater, который можно использовать для раздувания любых представлений во фрагменте.
     * @param container          Если не null, это родительское представление, к которому будет присоединено представление фрагмента.
     * @param savedInstanceState Если не null, этот фрагмент создается заново из предыдущего сохраненного состояния.
     * @return Возвращает View для пользовательского интерфейса фрагмента.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация ViewBinding
        binding = BottomSheetAddedIngredientsBinding.inflate(inflater, container, false);
        // Настройка RecyclerView
        binding.bottomSheetAddedIngredientsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Инициализация и установка адаптера
        adapter = new IngredientsAdapter(getParentFragmentManager());
        binding.bottomSheetAddedIngredientsRecycler.setAdapter(adapter);
        // Применение отступов для кнопки добавления продукта
        ViewUtils.applyInsets(binding.addProductButton, false, true, false, false);

        // Установка слушателя нажатия на кнопку добавления продукта
        binding.addProductButton.setOnClickListener(v -> {
            // Создание результата для родительского фрагмента/активности
            Bundle result = new Bundle();
            result.putBoolean(BUNDLE_KEY_CONFIRMED, true);
            // Отправка результата и закрытие BottomSheet
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss();
        });

        // Настройка ItemTouchHelper для обработки свайпов по элементам списка
        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(binding.bottomSheetAddedIngredientsRecycler);

        // Установка слушателя для результатов от IngredientsAdapter (обновление массы продукта)
        getParentFragmentManager().setFragmentResultListener(IngredientsAdapter.REQUEST_KEY, this, (requestKey, bundle) -> {
            // Проверка наличия необходимых данных в Bundle
            if (bundle.containsKey(IngredientsAdapter.BUNDLE_KEY_PRODUCT) && bundle.containsKey(IngredientsAdapter.BUNDLE_KEY_NEW_MASS)) {
                // Извлечение данных из Bundle
                Product productFromBundle = bundle.getParcelable(IngredientsAdapter.BUNDLE_KEY_PRODUCT);
                float newMass = bundle.getFloat(IngredientsAdapter.BUNDLE_KEY_NEW_MASS, 0f);

                List<Product> currentProducts = ProductsSelectedManager.getAll();
                // Обновление продукта в списке с использованием утилитного метода
                List<Product> updatedList = ProductListUpdater.updateProductInList(currentProducts, productFromBundle, newMass);

                // Обработка результата обновления
                if (updatedList != null) {
                    ProductsSelectedManager.setAll(updatedList); // Обновление списка в менеджере
                    adapter.submitList(updatedList); // Обновление списка в адаптере
                } else {
                    // Отображение сообщения об ошибке, если продукт не найден или произошла ошибка
                    if (productFromBundle != null) {
                        Snackbar.make(binding.getRoot(), getString(R.string.error_update_product) + " (продукт не найден для обновления): " + productFromBundle.getName(), Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(binding.getRoot(), getString(R.string.error_update_product), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        return binding.getRoot();
    }

    /**
     * Создает и настраивает ItemTouchHelper для обработки свайпов влево и вправо по элементам RecyclerView.
     * При свайпе отображается диалог подтверждения удаления продукта.
     *
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
                // Проверка валидности позиции элемента
                if (position == RecyclerView.NO_POSITION || position >= adapter.getCurrentList().size()) {
                    return;
                }
                final Product product = adapter.getCurrentList().get(position);

                // Отображение диалога подтверждения удаления
                AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete)
                        .setMessage(getString(R.string.delete_confirmation, product.getName()))
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            // Отмена удаления, обновление элемента в списке
                            dialog.dismiss();
                            adapter.notifyItemChanged(position);
                        })
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            // Подтверждение удаления
                            ProductsSelectedManager.remove(product); // Удаление продукта из менеджера
                            // Если список продуктов пуст, закрываем диалог и BottomSheet
                            if (ProductsSelectedManager.size() == 0) {
                                dialog.dismiss();
                                ViewAddedBottomSheet.this.dismiss();
                            }
                            adapter.submitList(ProductsSelectedManager.getAll()); // Обновление списка в адаптере
                        }).create();
                alertDialog.show();
            }
        };

        // Создание и возврат ItemTouchHelper
        return new ItemTouchHelper(simpleCallback);
    }

    /**
     * Вызывается при уничтожении представления фрагмента.
     * Освобождает ссылку на ViewBinding для предотвращения утечек памяти.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка ViewBinding
        binding = null;
    }
}
