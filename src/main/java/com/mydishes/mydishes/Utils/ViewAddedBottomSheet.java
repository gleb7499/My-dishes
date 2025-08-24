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
import com.mydishes.mydishes.Adapters.IngredientsAdapter;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Models.ProductsSelectedManager;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.databinding.BottomSheetAddedIngredientsBinding;

// Отображение нижнего листа со списком выбранных продуктов для текущего блюда
public class ViewAddedBottomSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "viewAddedBottomSheetRequestKey";
    public static final String BUNDLE_KEY_CONFIRMED = "confirmed";

    private BottomSheetAddedIngredientsBinding binding; // связывание XML макета нижнего листа
    private IngredientsAdapter adapter; // адаптер для списка выбранных продуктов

    public ViewAddedBottomSheet() {
        // пустой конструктор
    }


    @Override
    public void onResume() {
        super.onResume();
        // перед показом активити обновляем список
        adapter.submitList(ProductsSelectedManager.getAll());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }


    // создание листа
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // раздули XML
        binding = BottomSheetAddedIngredientsBinding.inflate(inflater, container, false);
        binding.bottomSheetAddedIngredientsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // при использовании BottomSheetAddedIngredientsBinding можно обращаться к элементам макета напрямую
        // настроили и установили адаптер
        adapter = new IngredientsAdapter();
        binding.bottomSheetAddedIngredientsRecycler.setAdapter(adapter);
        ViewUtils.applyInsets(binding.addProductButton, false, true, false, false);

        // прослушка для кнопки добавления нового блюда из списка продуктов
        binding.addProductButton.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putBoolean(BUNDLE_KEY_CONFIRMED, true);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss(); // закрываем bottom sheet
        });

        // обработка свайпа по элементу списка
        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(binding.bottomSheetAddedIngredientsRecycler);

        return binding.getRoot();
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
                final Product product = adapter.getCurrentList().get(position);

                AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete)
                        .setMessage(getString(R.string.delete_confirmation, product.getName())) // Используем форматированную строку для подтверждения
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                            adapter.notifyItemChanged(position); // Возвращаем элемент на место
                        })
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            ProductsSelectedManager.remove(product); // Удаляем продукт из списка
                            if (ProductsSelectedManager.size() == 0) {
                                dialog.dismiss(); // Закрываем диалог
                                ViewAddedBottomSheet.this.dismiss(); // Закрываем фрагмент
                            }
                            adapter.submitList(ProductsSelectedManager.getAll()); // Обновляем список
                        }).create();
                alertDialog.show();
            }
        };

        // привязываем свайп к RecyclerView
        return new ItemTouchHelper(simpleCallback);
    }


    // перед уничтожением
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // сбрасываем биндинг против утечки памяти
        binding = null;
    }
}
