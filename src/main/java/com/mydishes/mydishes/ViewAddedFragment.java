package com.mydishes.mydishes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mydishes.mydishes.Adapters.IngredientsAdapter;
import com.mydishes.mydishes.Models.ProductsSelectedManager;
import com.mydishes.mydishes.Utils.ViewUtils;
import com.mydishes.mydishes.databinding.FragmentViewAddedBinding;

// Отображение нижнего листа со списком выбранных продуктов для текущего блюда
public class ViewAddedFragment extends BottomSheetDialogFragment {

    // Интерфейс обратного вызова при завершении текущей активити (при нажатии кнопки добавления блюда -- выполнение onConfirmed())
    public interface OnConfirmListener {
        void onConfirmed();
    }

    private OnConfirmListener listener;

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.listener = listener; // Установка действия при нажатии кнопки
    }

    private FragmentViewAddedBinding binding; // связывание XML макета нижнего листа
    private IngredientsAdapter adapter; // адаптер для списка выбранных продуктов

    public ViewAddedFragment() {
        // пустой конструктор
    }

    @Override
    public void onResume() {
        super.onResume();
        // перед показом активити обновляем список
        adapter.submitList(ProductsSelectedManager.getAll());
    }

    // создание листа
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // раздули XML
        binding = FragmentViewAddedBinding.inflate(inflater, container, false);
        binding.selectedProductsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // при использовании FragmentViewAddedBinding можно обращаться к элементам макета напрямую
        // настроили и установили адаптер
        adapter = new IngredientsAdapter();
        binding.selectedProductsRecycler.setAdapter(adapter);
        ViewUtils.applyInsets(binding.addProductButton, false, true, false, false);

        // прослушка для кнопки добавления нового блюда из списка продуктов
        binding.addProductButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmed();  // выполняем действие
            }
            dismiss(); // закрываем bottom sheet
        });

        return binding.getRoot();
    }

    // перед уничтожением
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // сбрасываем биндинг против утечки памяти
        binding = null;
    }
}
