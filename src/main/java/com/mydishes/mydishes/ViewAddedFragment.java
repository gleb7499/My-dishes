package com.mydishes.mydishes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mydishes.mydishes.Adapters.ProductSelectedAdapter;
import com.mydishes.mydishes.Models.SelectedProductsManager;
import com.mydishes.mydishes.databinding.FragmentViewAddedBinding;
import com.mydishes.mydishes.utils.ViewUtils;

import java.util.ArrayList;

public class ViewAddedFragment extends BottomSheetDialogFragment {

    public interface OnConfirmListener {
        void onConfirmed();
    }

    private OnConfirmListener listener;

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.listener = listener;
    }

    private FragmentViewAddedBinding binding;
    private ProductSelectedAdapter adapter;

    public ViewAddedFragment() {
        // пустой конструктор обязательно
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.submitList(SelectedProductsManager.getAll());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentViewAddedBinding.inflate(inflater, container, false);
        binding.selectedProductsRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter = new ProductSelectedAdapter(requireContext(), new ArrayList<>());

        binding.selectedProductsRecycler.setAdapter(adapter);

        ViewUtils.applyInsets(binding.addProductButton, false, true, false, false);

        binding.addProductButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmed();  // 💡 Сообщаем активити
            }
            dismiss(); // закрываем bottom sheet
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
