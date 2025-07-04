package com.mydishes.mydishes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mydishes.mydishes.Models.Dishes;
import com.mydishes.mydishes.Parser.EdostavkaParser;

import org.junit.Test;

import java.util.List;

public class EdostavkaParserTest {
    @Test
    public void testFindReturnsResults() {
        // Подготовка запроса
        String query = "колбаса мортаделла";

        long start = System.nanoTime();
        EdostavkaParser.find(query);
        long end = System.nanoTime();

        double seconds = (end - start) / 1_000_000_000.0;
        System.out.printf("Время запроса -> %.3f секунд%n", seconds);

        // Получаем результаты
        List<Dishes> results = EdostavkaParser.getDishesList();

        // Проверяем, что результаты не пустые (если на сайте есть такие продукты)
        assertNotNull(results);
        assertFalse(results.isEmpty());

        System.out.println("Размер массива -> " + results.size());

        // Выводим первый результат для наглядности
        for (Dishes dish : results) {
            System.out.println("Название: " + dish.getName());
            System.out.println("Ссылка: " + dish.getUrl());
            System.out.println("Изображение: " + dish.getImage());
        }
    }
}