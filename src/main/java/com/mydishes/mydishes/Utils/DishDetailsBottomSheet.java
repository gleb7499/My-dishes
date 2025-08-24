package com.mydishes.mydishes.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.mydishes.mydishes.Adapters.IngredientsAdapter;
import com.mydishes.mydishes.Database.repository.DataRepository;
import com.mydishes.mydishes.Models.Dish;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.R;
import com.mydishes.mydishes.databinding.BottomSheetDishDetailsBinding;

import java.util.ArrayList;
import java.util.List;

public class DishDetailsBottomSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "dishDetailsRequestKey";
    public static final String BUNDLE_KEY_DISH_UPDATED = "dishUpdated";
    private final Dish dish;
    private BottomSheetDishDetailsBinding binding; // связывание XML макета нижнего листа
    private DataRepository dataRepository;

    public DishDetailsBottomSheet(Dish dish) {
        this.dish = dish;
        // dataRepository initialization moved to onCreate
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataRepository = DataRepository.getInstance(requireContext().getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetDishDetailsBinding.inflate(inflater, container, false);

        // установили имя продукта
        binding.bottomSheetDishName.setText(dish.getName());

        // устанавливаем фото блюда
        Glide.with(requireContext())
                .load(dish.getPhotoUri())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(binding.bottomSheetDishImage);

        // устанавливаем список продуктов (ингредиентов) в recycler view
        List<Product> ingredients = dish.getProducts();
        if (ingredients != null && !ingredients.isEmpty()) {
            binding.bottomSheetDishDetailsIngredientsTitle.setVisibility(View.VISIBLE);
            binding.bottomSheetDishDetailsIngredientsRecycler.setVisibility(View.VISIBLE);
            IngredientsAdapter ingredientsAdapter = new IngredientsAdapter();
            binding.bottomSheetDishDetailsIngredientsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.bottomSheetDishDetailsIngredientsRecycler.setAdapter(ingredientsAdapter);
            ingredientsAdapter.submitList(new ArrayList<>(ingredients));
        } else {
            binding.bottomSheetDishDetailsIngredientsTitle.setVisibility(View.GONE);
            binding.bottomSheetDishDetailsIngredientsRecycler.setVisibility(View.GONE);
        }

        // устанавливаем слушатель на название блюда чтобы поменять его
        binding.bottomSheetDishName.setOnClickListener(v -> DialogUtils.showEditDishNameDialog(requireContext(), dish.getName(), newName -> {
            // Обновляем имя в объекте Dish
            dish.setName(newName);

            // Вызываем метод репозитория для обновления блюда в БД
            dataRepository.updateDish(requireActivity(), dish, new DataRepository.QueryCallBack<>() {
                @Override
                public void onSuccess(Void result) {
                    // Обновляем текст в TextView
                    binding.bottomSheetDishName.setText(newName);
                    // Отправляем результат родительскому компоненту
                    Bundle bundleResult = new Bundle();
                    bundleResult.putBoolean(BUNDLE_KEY_DISH_UPDATED, true);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, bundleResult);
                }

                @Override
                public void onError(Exception e) {
                    Snackbar.make(binding.getRoot(), getString(R.string.error_update_name_dish) + ": " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        }));

        return binding.getRoot();
    }
}
