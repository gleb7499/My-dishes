package com.mydishes.mydishes.Models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public class DishesManager {
    public static final List<Dish> dishes = new ArrayList<>();

    public static boolean add(Dish dish) {
        boolean res = dishes.add(dish);
        notifyChange();
        return res;
    }

    public static Dish remove(int i) {
        Dish dish = dishes.remove(i);
        notifyChange();
        return dish;
    }

    public static int size() {
        return dishes.size();
    }

    @NonNull
    @Contract(" -> new")
    public static List<Dish> getAll() {
        return new ArrayList<>(dishes);
    }

    // Логика подписок на изменение централизованного списка блюд
    public interface Action {
        void doAction();
    }

    private static final List<Action> actions = new ArrayList<>();

    public static void subscribe(Action action) {
        if (!actions.contains(action)) actions.add(action);
    }

    public static void removeSubscribe(Action action) {
        actions.remove(action);
    }

    private static void notifyChange() {
        for (Action action : actions) {
            action.doAction();
        }
    }
}
