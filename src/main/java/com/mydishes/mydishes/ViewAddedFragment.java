package com.mydishes.mydishes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mydishes.mydishes.Adapters.ProductSelectedAdapter;
import com.mydishes.mydishes.Models.SelectedProductsManager;
import com.mydishes.mydishes.databinding.FragmentViewAddedBinding;

import java.util.ArrayList;

public class ViewAddedFragment extends BottomSheetDialogFragment {

    private FragmentViewAddedBinding binding;
    private ProductSelectedAdapter adapter;

    public ViewAddedFragment() {
        // пустой конструктор обязательно
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentViewAddedBinding.inflate(inflater, container, false);
        adapter = new ProductSelectedAdapter(requireContext(), SelectedProductsManager.getAll());

        binding.selectedProductsRecycler.setAdapter(adapter);
        SelectedProductsManager.registerListener(adapter::submitList);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SelectedProductsManager.unregisterListener(adapter::submitList);
        binding = null;
    }
}
